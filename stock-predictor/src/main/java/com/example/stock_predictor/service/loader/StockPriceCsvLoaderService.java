package com.example.stock_predictor.service.loader;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.util.CsvUtils;
import com.example.stock_predictor.util.NumberParseUtils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceCsvLoaderService {

    private static final int BATCH_SIZE = 1000;
    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;

    public void load(String filePath) throws IOException, CsvValidationException {
        if (!CsvUtils.fileExists(filePath)) {
            log.warn("CSV 파일이 존재하지 않습니다: {}", filePath);
            return;
        }

        Map<String, Stock> stockCache = loadStockCache(filePath);
        List<StockPrice> buffer = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);
            String[] cols;

            while ((cols = reader.readNext()) != null) {
                if (cols.length < 8) continue;

                LocalDate date;
                try { date = LocalDate.parse(cols[0]); } catch (DateTimeParseException e) { continue; }

                Stock stock = stockCache.get(cols[7]);
                if (stock == null) continue;

                buffer.add(StockPrice.builder()
                        .stock(stock)
                        .date(date)
                        .openPrice(NumberParseUtils.parseBigDecimalOrNull(cols[1]))
                        .closePrice(NumberParseUtils.parseBigDecimalOrNull(cols[2]))
                        .highPrice(NumberParseUtils.parseBigDecimalOrNull(cols[3]))
                        .lowPrice(NumberParseUtils.parseBigDecimalOrNull(cols[4]))
                        .volume(NumberParseUtils.parseLongOrNull(cols[5]))
                        .changeRate(NumberParseUtils.parseBigDecimalOrNull(cols[6]))
                        .build());

                saveBatchIfNeeded(buffer);
            }
        }

        saveBatchIfNeeded(buffer);
    }

    private Map<String, Stock> loadStockCache(String filePath) throws IOException, CsvValidationException {
        Set<String> tickers = new HashSet<>();

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);
            String[] cols;
            while ((cols = reader.readNext()) != null) {
                if (cols.length >= 8) tickers.add(cols[7]);
            }
        }

        Map<String, Stock> stockMap = new HashMap<>();
        stockRepository.findByTickerIn(new ArrayList<>(tickers))
                .forEach(stock -> stockMap.put(stock.getTicker(), stock));
        return stockMap;
    }

    private void saveBatchIfNeeded(List<StockPrice> buffer) {
        if (!buffer.isEmpty() && buffer.size() >= BATCH_SIZE) {
            stockPriceRepository.saveAll(buffer);
            buffer.clear();
        }
    }
}
