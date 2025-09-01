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

import java.io.File;
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
        if (!checkFileExistsOrSkip(filePath)) {
            return null; // 파일 없으면 바로 종료
        }

        List<Stock> readStockList = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        lines.remove(0);

        for (String line : lines) {
            String[] cols = line.split(",");
            if (cols.length < 5) continue;

            String ticker = cols[0];
            String name = cols[1];
            String market = cols[2];
            String sector = cols[3];
            LocalDate date;
            try {
                date = LocalDate.parse(cols[4]);
            } catch (DateTimeParseException e) {
                continue;
            }

            Stock stock = Stock.builder()
                    .ticker(ticker)
                    .name(name)
                    .market(market)
                    .sector(sector)
                    .date(date)
                    .build();

            readStockList.add(stock);
        }

        return readStockList;
    }

    public void loadStockIndexPriceCsv(String filePath) throws IOException{
        if (!checkFileExistsOrSkip(filePath)) {
            return; // 파일 없으면 바로 종료
        }

        List<String> lines = Files.readAllLines(Paths.get(filePath));
        lines.remove(0);

        for (String line : lines) {
            String[] cols = line.split(",");
            if (cols.length < 9) continue;

            String indexName = cols[7];
            LocalDate date;
            try {
                date = LocalDate.parse(cols[8]);
            }catch (DateTimeParseException e){
                continue;
            }

            BigDecimal openPrice = parseBigDecimal(cols[0]);
            BigDecimal highPrice = parseBigDecimal(cols[1]);
            BigDecimal lowPrice = parseBigDecimal(cols[2]);
            BigDecimal closePrice = parseBigDecimal(cols[3]);
            Long volume = parseLong(cols[4]);
            Long value = parseLong(cols[5]);
            Long marketCap = parseLong(cols[5]);

            StockIndexPrice stockIndexPrice = StockIndexPrice.builder()
                    .indexName(indexName)
                    .date(date)
                    .openPrice(openPrice)
                    .highPrice(highPrice)
                    .lowPrice(lowPrice)
                    .closePrice(closePrice)
                    .volume(volume)
                    .value(value)
                    .marketCap(marketCap)
                    .build();

            stockIndexPriceRepository.save(stockIndexPrice);
        }
    }

    @Transactional
    public void loadStockPriceCsv(String filePath) throws IOException {
        if (!checkFileExistsOrSkip(filePath)) {
            return; // 파일 없으면 바로 종료
        }

        // 1) 필요한 티커만 캐시
        Map<String, Stock> stockCache = loadStockCache(filePath);

        List<StockPrice> buffer = new ArrayList<>(BATCH_SIZE);
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            final Iterator<String> it = lines.skip(1).iterator(); // 헤더 스킵
            while (it.hasNext()) {
                String line = it.next();
                String[] cols = line.split(",", -1);
                if (cols.length < 8) continue;

                // 파싱
                LocalDate date;
                try {
                    date = LocalDate.parse(cols[0]); // yyyy-MM-dd 가정
                } catch (DateTimeParseException e) { continue; }

                String ticker = cols[7];
                Stock stock = stockCache.get(ticker);
                if (stock == null) continue;

                BigDecimal open  = parseBigDecimal(cols[1]);
                BigDecimal close = parseBigDecimal(cols[2]);
                BigDecimal high  = parseBigDecimal(cols[3]);
                BigDecimal low   = parseBigDecimal(cols[4]);
                Long volume      = parseLong(cols[5]);
                BigDecimal chgRt = parseBigDecimal(cols[6]);

                buffer.add(StockPrice.builder()
                        .stock(stock)
                        .date(date)
                        .openPrice(open)
                        .closePrice(close)
                        .highPrice(high)
                        .lowPrice(low)
                        .volume(volume)
                        .changeRate(chgRt)
                        .build());

                if (buffer.size() >= BATCH_SIZE) {
                    stockPriceRepository.saveAll(buffer);
                    em.flush();   // DB로 밀어넣기
                    em.clear();   // 1차 캐시 비움 (메모리 사용량 안정화)
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
        return stockRepository.findByTickerIn(tickers).stream()
                .collect(Collectors.toMap(Stock::getTicker, Function.identity()));
    }

    @Transactional
    public void loadValuationMetricCsv(String filePath) throws IOException{
        if (!checkFileExistsOrSkip(filePath)){
            return;
        }

        Map<String, Stock> stockCache = loadStockCache(filePath);

        List<ValuationMetric> buffer = new ArrayList<>(BATCH_SIZE);
        try (Stream<String> lines = Files.lines(Paths.get(filePath))){
            final Iterator<String> it = lines.skip(1).iterator();
            while(it.hasNext()){
                String line = it.next();
                String[] cols = line.split(",",-1);
                if (cols.length < 9) continue;

                LocalDate date;
                try{
                    date = LocalDate.parse(cols[0]);
                }catch (DateTimeParseException e){continue;}

                String ticker = cols[1];
                Stock stock = stockCache.get(ticker);
                if (stock == null) continue;

                BigDecimal roe = parseBigDecimal(cols[2]);
                BigDecimal per = parseBigDecimal(cols[3]);
                BigDecimal pbr = parseBigDecimal(cols[4]);
                BigDecimal eps = parseBigDecimal(cols[5]);
                BigDecimal bps = parseBigDecimal(cols[6]);
                BigDecimal dividendYield = parseBigDecimal(cols[7]);

                buffer.add(ValuationMetric.builder()
                        .stock(stock)
                        .date(date)
                        .roe(roe)
                        .per(per)
                        .pbr(pbr)
                        .eps(eps)
                        .bps(bps)
                        .dividendYield(dividendYield)
                        .build());

                if (buffer.size() >= BATCH_SIZE){
                    valuationMetricRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()){
            valuationMetricRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }

    private boolean checkFileExistsOrSkip(String filePath){
        Path path = Paths.get(filePath);
        if (!Files.exists(path)){
            System.out.println("CSV 파일이 존재하지 않아 스킵합니다: "+ filePath);
            return false;
        }
        return true;
    }

    private BigDecimal parseBigDecimal(String value) {
        return (value == null || value.isEmpty()) ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private Long parseLong(String value) {
        return (value == null || value.isEmpty()) ? 0L : Long.parseLong(value);
    }

}
