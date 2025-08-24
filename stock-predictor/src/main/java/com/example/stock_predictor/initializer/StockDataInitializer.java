package com.example.stock_predictor.initializer;

import com.example.stock_predictor.repository.StockIndexPriceRepository;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.service.StockDataLoader;
import com.example.stock_predictor.utills.Formatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockDataInitializer implements ApplicationRunner {
    @Value("${stockPrice.files.path}")
    private String stockPriceFilesPath;
    private final StockDataLoader stockDataLoader;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndexPriceRepository stockIndexPriceRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Formatter formatter = new Formatter();
        String formattedDate = formatter.formattingDate();

        if(stockPriceRepository.count() == 0){
            Path path = Paths.get(stockPriceFilesPath, "all_korea_stock_price_" + formattedDate + ".csv");
            stockDataLoader.loadStockPriceCsv(path.toString());
        }

        if (stockIndexPriceRepository.count() == 0){
            Path path = Paths.get(stockPriceFilesPath, "all_korea_stock_index_price_" + formattedDate + ".csv");
            stockDataLoader.loadStockIndexPriceCsv(path.toString());
        }
    }
}
