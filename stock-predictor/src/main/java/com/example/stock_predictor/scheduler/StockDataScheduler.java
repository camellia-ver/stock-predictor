package com.example.stock_predictor.scheduler;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.service.StockDataLoadService;
import com.example.stock_predictor.service.StockService;
import com.example.stock_predictor.service.loader.*;
import com.example.stock_predictor.utill.DateFormatterUtil;
import com.opencsv.exceptions.CsvValidationException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockDataScheduler {
    @Value("${stock.files.path}")
    private String stockFilesPath;

    @Value("${stockPrediction.files.path}")
    private String stockPredictionPath;

    private final StockCsvLoaderService stockCsvLoaderService;
    private final StockPriceCsvLoaderService stockPriceCsvLoaderService;
    private final StockIndexPriceCsvLoaderService stockIndexPriceCsvLoaderService;
    private final ValuationMetricCsvLoaderService valuationMetricCsvLoaderService;
    private final PredictionCsvLoaderService predictionCsvLoaderService;
    private final StockService stockService;

    @Scheduled(cron = "0 55 8 ? * TUE-SAT")
    public void updateDailyStockData() throws IOException, CsvValidationException {
        DateFormatterUtil dateFormatterUtil = new DateFormatterUtil();
        String formattedDate = dateFormatterUtil.formattingDate();

        Path path = Paths.get(stockFilesPath, "new_korea_stock_price_" + formattedDate + ".csv");
        stockPriceCsvLoaderService.load(path.toString());

        path = Paths.get(stockFilesPath, "new_korea_stock_index_price_" + formattedDate + ".csv");
        stockIndexPriceCsvLoaderService.load(path.toString());

        path = Paths.get(stockFilesPath, "new_korea_valuation_" + formattedDate + ".csv");
        valuationMetricCsvLoaderService.load(path.toString());

        path = Paths.get(stockPredictionPath,"predictions_" + formattedDate + ".csv");
        predictionCsvLoaderService.load(path.toString());
    }

    @Scheduled(cron = "0 55 8 1 * ?")
    public void updateMonthlyStockList() throws IOException, CsvValidationException {
        Path path = Paths.get(stockFilesPath, "stock_list.csv");
        List<Stock> stockList = stockCsvLoaderService.load(path.toString());
        stockService.syncWithCsv(stockList);
    }
}
