package com.example.stock_predictor.scheduler;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.service.StockDataLoader;
import com.example.stock_predictor.service.StockService;
import com.example.stock_predictor.utills.Formatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockDataScheduler {
    @Value("${stockPrice.files.path}")
    private String stockPriceFilesPath;
    private final StockDataLoader stockDataLoader;
    private final StockService stockService;

    @Scheduled(cron = "0 55 8 ? * TUE-SAT")
    public void updateDailyStockData() throws IOException {
        Formatter formatter = new Formatter();
        String formattedDate = formatter.formattingDate();

        Path path = Paths.get(stockPriceFilesPath, "new_korea_stock_price_" + formattedDate + ".csv");
        stockDataLoader.loadStockPriceCsv(path.toString());

        path = Paths.get(stockPriceFilesPath, "new_korea_stock_index_price_" + formattedDate + ".csv");
        stockDataLoader.loadStockIndexPriceCsv(path.toString());

        path = Paths.get(stockPriceFilesPath, "stock_list.csv");
        List<Stock> stockList = stockDataLoader.loadStockListCsv(path.toString());
        stockService.syncWithCsv(stockList);

        path = Paths.get(stockPriceFilesPath, "new_korea_valuation_" + formattedDate + ".csv");
        stockDataLoader.loadValuationMetricCsv(path.toString());
    }
}
