package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.model.ValuationMetric;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.repository.ValuationMetricRepository;
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
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StockDataLoader {

    private static final int BATCH_SIZE = 1000;

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndexPriceRepository stockIndexPriceRepository;
    private final ValuationMetricRepository valuationMetricRepository;
    private final EntityManager em;

    public List<Stock> loadStockListCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return null;

        List<Stock> stockList = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator(); // 헤더 스킵
            int count = 0;
            while (it.hasNext()) {
                String[] cols = it.next().split(",", -1);
                if (cols.length < 5) continue;
                LocalDate date;
                try { date = LocalDate.parse(cols[4]); } catch (DateTimeParseException e) { continue; }

                stockList.add(Stock.builder()
                        .ticker(cols[0])
                        .name(cols[1])
                        .market(cols[2])
                        .sector(cols[3])
                        .date(date)
                        .build());

                count++;
                if (count % 1000 == 0) System.out.println("StockList 처리: " + count + "줄");
            }
        }
        return stockList;
    }

    @Transactional
    public void loadStockIndexPriceCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return;

        List<StockIndexPrice> buffer = new ArrayList<>(BATCH_SIZE);
        int count = 0;

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

                count++;
                if (buffer.size() >= BATCH_SIZE) {
                    stockIndexPriceRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                    System.out.println("StockIndexPrice 처리: " + count + "줄");
                }
            }
        }
        if (!buffer.isEmpty()) {
            stockIndexPriceRepository.saveAll(buffer);
            em.flush();
            em.clear();
            System.out.println("StockIndexPrice 최종 처리: " + count + "줄");
        }
    }

    @Transactional
    public void loadStockPriceCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) return;

        Map<String, Stock> stockCache = loadStockCache(filePath);
        List<StockPrice> buffer = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator();
            while (it.hasNext()) {
                String[] cols = it.next().split(",", -1);
                if (cols.length < 8) continue;

                LocalDate date;
                try { date = LocalDate.parse(cols[0]); } catch (DateTimeParseException e) { continue; }

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

                count++;
                if (buffer.size() >= BATCH_SIZE) {
                    stockPriceRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                    System.out.println("StockPrice 처리: " + count + "줄");
                }
            }
        }
        if (!buffer.isEmpty()) {
            stockPriceRepository.saveAll(buffer);
            em.flush();
            em.clear();
            System.out.println("StockPrice 최종 처리: " + count + "줄");
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
                    System.out.println("ValuationMetric 처리: " + count + "줄");
                }
            }
        }
        if (!buffer.isEmpty()) {
            valuationMetricRepository.saveAll(buffer);
            em.flush();
            em.clear();
            System.out.println("ValuationMetric 최종 처리: " + count + "줄");
        }
    }

    private boolean checkFileExistsOrSkip(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.out.println("CSV 파일이 존재하지 않아 스킵합니다: " + filePath);
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
