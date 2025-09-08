package com.example.stock_predictor.util;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class StockCacheLoader {
    private static final int TICKER_COLUMN_INDEX = 7;
    private final StockRepository stockRepository;

    public Map<String, Stock> loadStockCache(String filePath) throws IOException, CsvValidationException{
        Set<String> tickers = extractTickersFromCsv(filePath);

        return fetchStocksByTickers(tickers);
    }

    private Set<String> extractTickersFromCsv(String filePath) throws IOException, CsvValidationException{
        Set<String> tickers = new HashSet<>();

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)){
            CsvUtils.skipHeader(reader);
            String[] cols;

            while ((cols = reader.readNext()) != null){
                if (cols.length > TICKER_COLUMN_INDEX){
                    tickers.add(cols[TICKER_COLUMN_INDEX]);
                }
            }
        }

        return tickers;
    }

    private Map<String, Stock> fetchStocksByTickers(Set<String> tickers){
        Map<String, Stock> stockMap = new HashMap<>();

        stockRepository.findByTickerIn(new ArrayList<>(tickers))
                .forEach(stock -> stockMap.put(stock.getTicker(), stock));

        return stockMap;
    }
}
