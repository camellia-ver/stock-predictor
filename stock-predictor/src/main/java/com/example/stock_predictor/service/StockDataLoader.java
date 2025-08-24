package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockDataLoader {
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndexPriceRepository stockIndexPriceRepository;

    public List<Stock> loadStockListCsv(String filePath) throws IOException {
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

            BigDecimal openPrice = cols[0].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[0]);
            BigDecimal highPrice = cols[1].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[1]);
            BigDecimal lowPrice = cols[2].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[2]);
            BigDecimal closePrice = cols[3].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[3]);
            Long volume = cols[4].isEmpty() ? 0L : Long.parseLong(cols[4]);
            Long value = cols[5].isEmpty() ? 0L : Long.parseLong(cols[5]);
            Long marketCap = cols[6].isEmpty() ? 0L : Long.parseLong(cols[5]);

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

    public void loadStockPriceCsv(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        lines.remove(0); // 헤더 제거

        for (String line : lines){
            String[] cols = line.split(",");
            if (cols.length < 8) continue;

            String ticker = cols[7];
            LocalDate date;
            try {
                date = LocalDate.parse(cols[0]);
            } catch (DateTimeParseException e){
                continue;
            }
            BigDecimal openPrice = cols[1].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[1]);
            BigDecimal closePrice = cols[2].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[2]);
            BigDecimal highPrice = cols[3].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[3]);
            BigDecimal lowPrice = cols[4].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[4]);
            Long volume = cols[5].isEmpty() ? 0L : Long.parseLong(cols[5]);
            BigDecimal changeRate = cols[6].isEmpty() ? BigDecimal.ZERO : new BigDecimal(cols[6]);

            Stock stock = stockRepository.findByTicker(ticker)
                    .orElseThrow(() -> new RuntimeException("Stock not found: " + ticker));

            StockPrice stockPrice = StockPrice.builder()
                    .stock(stock)
                    .date(date)
                    .openPrice(openPrice)
                    .closePrice(closePrice)
                    .highPrice(highPrice)
                    .lowPrice(lowPrice)
                    .volume(volume)
                    .changeRate(changeRate)
                    .build();

            stockPriceRepository.save(stockPrice);
        }
    }
}
