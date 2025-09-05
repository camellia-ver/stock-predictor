package com.example.stock_predictor.service;

import com.example.stock_predictor.model.*;
import com.example.stock_predictor.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StockDataLoaderService {
    private static final int BATCH_SIZE = 1000;

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndexPriceRepository stockIndexPriceRepository;
    private final ValuationMetricRepository valuationMetricRepository;
    private final PredictionRepository predictionRepository;
    private final EntityManager em;

    @Transactional
    public void loadPredictionCsv(String filePath) throws IOException{
        if (!checkFileExistsOrSkip(filePath)) return;

        List<Prediction> buffer = new ArrayList<>(BATCH_SIZE);

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator();
            while (it.hasNext()){
                String[] cols = it.next().split(",",-1);

                if (cols.length < 7) continue;

                LocalDate predictionDate;
                LocalDate targetDate;
                LocalDateTime createdAt;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                try {
                    predictionDate = LocalDate.parse(cols[1]);
                    targetDate = LocalDate.parse(cols[2]);
                    createdAt = LocalDateTime.parse(cols[6], formatter);
                }catch (DateTimeParseException e){
                    continue;
                }

                Stock stock = stockRepository.findById(parseLong(cols[0]))
                        .orElseThrow(() -> new RuntimeException("해당 주식이 존재하지 않습니다."));

                buffer.add(Prediction.builder()
                        .stock(stock)
                        .predictionDate(predictionDate)
                        .targetDate(targetDate)
                        .modelName(cols[3])
                        .upProb(parseBigDecimal(cols[4]))
                        .downProb(parseBigDecimal(cols[5]))
                        .createdAt(createdAt)
                        .build());

                if (buffer.size() >= BATCH_SIZE){
                    predictionRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }
        if (!buffer.isEmpty()){
            predictionRepository.saveAll(buffer);
            em.flush();
            em.clear();
            buffer.clear();
        }
    }

    public List<Stock> loadStockListCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return null;

        List<Stock> stockList = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator();
            while (it.hasNext()) {
                String[] cols = it.next().split(",", -1);

                if (cols.length < 5) continue;

                LocalDate date;
                try {
                    date = LocalDate.parse(cols[4]);
                } catch (DateTimeParseException e) {
                    continue;
                }

                stockList.add(Stock.builder()
                        .ticker(cols[0])
                        .name(cols[1])
                        .market(cols[2])
                        .sector(cols[3])
                        .date(date)
                        .build());
            }
        }
        return stockList;
    }

    @Transactional
    public void loadStockIndexPriceCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return;

        List<StockIndexPrice> buffer = new ArrayList<>(BATCH_SIZE);

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator();
            while (it.hasNext()) {
                String[] cols = it.next().split(",", -1);
                if (cols.length < 9) continue;

                LocalDate date;
                try { date = LocalDate.parse(cols[8]); } catch (DateTimeParseException e) { continue; }

                buffer.add(StockIndexPrice.builder()
                        .indexName(cols[7])
                        .date(date)
                        .openPrice(parseBigDecimal(cols[0]))
                        .highPrice(parseBigDecimal(cols[1]))
                        .lowPrice(parseBigDecimal(cols[2]))
                        .closePrice(parseBigDecimal(cols[3]))
                        .volume(parseLong(cols[4]))
                        .value(parseLong(cols[5]))
                        .marketCap(parseLong(cols[5]))
                        .build());

                if (buffer.size() >= BATCH_SIZE) {
                    stockIndexPriceRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }
        if (!buffer.isEmpty()) {
            stockIndexPriceRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }

    @Transactional
    public void loadStockPriceCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return;

        Map<String, Stock> stockCache = loadStockCache(filePath);
        List<StockPrice> buffer = new ArrayList<>(BATCH_SIZE);

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator();
            while (it.hasNext()) {
                String[] cols = it.next().split(",", -1);
                if (cols.length < 8) continue;

                LocalDate date;
                try {
                    date = LocalDate.parse(cols[0]);
                } catch (DateTimeParseException e) { continue; }

                Stock stock = stockCache.get(cols[7]);
                if (stock == null) continue;

                buffer.add(StockPrice.builder()
                        .stock(stock)
                        .date(date)
                        .openPrice(parseBigDecimal(cols[1]))
                        .closePrice(parseBigDecimal(cols[2]))
                        .highPrice(parseBigDecimal(cols[3]))
                        .lowPrice(parseBigDecimal(cols[4]))
                        .volume(parseLong(cols[5]))
                        .changeRate(parseBigDecimal(cols[6]))
                        .build());

                if (buffer.size() >= BATCH_SIZE) {
                    stockPriceRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }
        if (!buffer.isEmpty()) {
            stockPriceRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }

    private Map<String, Stock> loadStockCache(String filePath) throws IOException {
        Set<String> tickers = new HashSet<>();

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.skip(1).forEach(line -> {
                String[] cols = line.split(",", -1);
                if (cols.length >= 8) tickers.add(cols[7]);
            });
        }

        Map<String ,Stock> stockMap = new HashMap<>();
        List<String> tickerList = new ArrayList<>(tickers);

        final int checkSize = 1000;
        for (int i = 0; i < tickerList.size(); i += checkSize){
            int end = Math.min(i + checkSize, tickerList.size());
            List<String> chunk = tickerList.subList(i ,end);

            stockRepository.findByTickerIn(chunk)
                    .forEach(stock -> stockMap.put(stock.getTicker(), stock));
        }

        return stockMap;
    }

    @Transactional
    public void loadValuationMetricCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return;

        Map<String, Stock> stockCache = loadStockCache(filePath);
        List<ValuationMetric> buffer = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator();
            while (it.hasNext()) {
                String[] cols = it.next().split(",", -1);
                if (cols.length < 9) continue;

                LocalDate date;
                try { date = LocalDate.parse(cols[0]); } catch (DateTimeParseException e) { continue; }

                Stock stock = stockCache.get(cols[7]);
                if (stock == null) continue;

                buffer.add(ValuationMetric.builder()
                        .stock(stock)
                        .date(date)
                        .roe(parseBigDecimal(cols[8]))
                        .per(parseBigDecimal(cols[2]))
                        .pbr(parseBigDecimal(cols[3]))
                        .eps(parseBigDecimal(cols[4]))
                        .bps(parseBigDecimal(cols[1]))
                        .dps(parseBigDecimal(cols[6]))
                        .dividendYield(parseBigDecimal(cols[5]))
                        .build());

                count++;
                if (buffer.size() >= BATCH_SIZE) {
                    valuationMetricRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }
        if (!buffer.isEmpty()) {
            valuationMetricRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }

    private boolean checkFileExistsOrSkip(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.out.println("CSV 파일이 존재하지 않습니다 : " + filePath);
            return false;
        }
        return true;
    }

    private BigDecimal parseBigDecimal(String s) {
        if (s == null || s.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(s.trim()); }
        catch (NumberFormatException e) {
            System.err.println("숫자로 변환 불가: " + s);
            return null;
        }
    }

    private Long parseLong(String s) {
        if (s == null || s.isEmpty()) return 0L;
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return 0L; }
    }
}
