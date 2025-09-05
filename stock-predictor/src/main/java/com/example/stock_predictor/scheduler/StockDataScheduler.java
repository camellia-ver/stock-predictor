package com.example.stock_predictor.scheduler;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.service.StockDataLoaderService;
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
import java.util.List;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockDataScheduler {
    @Value("${stock.files.path}")
    private String stockFilesPath;

    @Value("${stockPrediction.files.path}")
    private String stockPredictionPath;

    private final StockDataLoaderService dataLoaderService;
    private final StockService stockService;

    @Scheduled(cron = "0 55 8 ? * TUE-SAT")
    public void updateDailyStockData() throws IOException {
        Formatter formatter = new Formatter();
        String formattedDate = formatter.formattingDate();

        Path path = Paths.get(stockFilesPath, "new_korea_stock_price_" + formattedDate + ".csv");
        dataLoaderService.loadStockPriceCsv(path.toString());

        path = Paths.get(stockFilesPath, "new_korea_stock_index_price_" + formattedDate + ".csv");
        dataLoaderService.loadStockIndexPriceCsv(path.toString());

        path = Paths.get(stockFilesPath, "new_korea_valuation_" + formattedDate + ".csv");
        dataLoaderService.loadValuationMetricCsv(path.toString());

        path = Paths.get(stockPredictionPath,"predictions_" + formattedDate + ".csv");
        dataLoaderService.loadPredictionCsv(path.toString());
    }

    @Scheduled(cron = "0 55 8 1 * ?")
    public void updateMonthlyStockList() throws IOException{
        Path path = Paths.get(stockFilesPath, "stock_list.csv");
        List<Stock> stockList = dataLoaderService.loadStockListCsv(path.toString());
        stockService.syncWithCsv(stockList);
    }
}
