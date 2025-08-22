package com.example.stock_predictor.scheduler;

import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.service.StockPriceLoader;
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

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockPriceScheduler {
    @Value("${stockPrice.files.path}")
    private String stockPriceFilesPath;
    private final StockPriceLoader stockPriceLoader;

    @Scheduled(cron = "0 55 8 ? * TUE-SAT")
    public void updateDailyStockPrice() throws IOException {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        String formattedDate = today.format(formatter);

        Path path = Paths.get(stockPriceFilesPath, "new_korea_stock_price_" + formattedDate + ".csv");
        stockPriceLoader.loadCsv(path.toString());
    }
}
