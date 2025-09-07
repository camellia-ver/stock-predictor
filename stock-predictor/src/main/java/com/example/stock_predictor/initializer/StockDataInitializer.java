package com.example.stock_predictor.initializer;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.repository.ValuationMetricRepository;
import com.example.stock_predictor.service.StockDataLoadService;
import com.example.stock_predictor.service.loader.StockCsvLoaderService;
import com.example.stock_predictor.service.loader.StockIndexPriceCsvLoaderService;
import com.example.stock_predictor.service.loader.StockPriceCsvLoaderService;
import com.example.stock_predictor.service.loader.ValuationMetricCsvLoaderService;
import com.example.stock_predictor.utill.DateFormatterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:paths.properties")
public class StockDataInitializer implements ApplicationRunner {
    @Value("${stock.files.path}")
    private String stockFilesPath;

    private final StockCsvLoaderService stockCsvLoaderService;
    private final StockPriceCsvLoaderService stockPriceCsvLoaderService;
    private final StockIndexPriceCsvLoaderService stockIndexPriceCsvLoaderService;
    private final ValuationMetricCsvLoaderService valuationMetricCsvLoaderService;

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndexPriceRepository stockIndexPriceRepository;
    private final ValuationMetricRepository valuationMetricRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DateFormatterUtil dateFormatterUtil = new DateFormatterUtil();
        String formattedDate = dateFormatterUtil.formattingDate();
        Path path = null;

        // 1. 주식 리스트 초기화
        if (stockRepository.count() == 0) {
            path = Paths.get(stockFilesPath, "stock_list.csv");
            List<Stock> stocks = stockCsvLoaderService.load(path.toString());
            stockRepository.saveAll(stocks);
        }

        // 2. 주식 가격 데이터 초기화
        if (stockPriceRepository.count() == 0) {
            path = Paths.get(stockFilesPath, "all_korea_stock_price_" + formattedDate + ".csv");
            stockPriceCsvLoaderService.load(path.toString());
        }

        // 3. 주가지수 가격 데이터 초기화
        if (stockIndexPriceRepository.count() == 0) {
            path = Paths.get(stockFilesPath, "all_korea_stock_index_price_" + formattedDate + ".csv");
            stockIndexPriceCsvLoaderService.load(path.toString());
        }

        // 4. 밸류에이션 메트릭 초기화
        if (valuationMetricRepository.count() == 0) {
            path = Paths.get(stockFilesPath, "all_korea_valuation_" + formattedDate + ".csv");
            valuationMetricCsvLoaderService.load(path.toString());
        }
    }
}
