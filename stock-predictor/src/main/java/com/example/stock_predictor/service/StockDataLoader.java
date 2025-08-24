package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockDataLoader {
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    public void loadCsv(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        lines.remove(0); // 헤더 제거

        for (String line : lines){
            String[] cols = line.split(",");
            if (cols.length < 9) continue;

            String ticker = cols[8];
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
