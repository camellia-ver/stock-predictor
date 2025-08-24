package com.example.stock_predictor.initializer;

import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.service.StockDataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockDataInitializer implements ApplicationRunner {
    @Value("${stockPrice.files.path}")
    private String stockPriceFilesPath;
    private final StockDataLoader stockDataLoader;
    private final StockPriceRepository stockPriceRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(stockPriceRepository.count() == 0){
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
            String formattedDate = today.format(formatter);

            Path path = Paths.get(stockPriceFilesPath, "all_korea_stock_price_" + formattedDate + ".csv");
            stockDataLoader.loadStockPriceCsv(path.toString());
        }
    }
}
