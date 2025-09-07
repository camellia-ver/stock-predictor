package com.example.stock_predictor.service;

import com.example.stock_predictor.model.*;
import com.example.stock_predictor.repository.*;
import com.example.stock_predictor.service.loader.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataLoadService {

    private final StockCsvLoaderService stockLoader;
    private final StockPriceCsvLoaderService priceLoader;
    private final StockIndexPriceCsvLoaderService indexLoader;
    private final ValuationMetricCsvLoaderService valuationLoader;
    private final PredictionCsvLoaderService predictionLoader;

    @Transactional
    public void loadAllCsvData(Map<String, String> csvFilePaths) throws IOException, CsvValidationException {
        log.info("Stock CSV 로딩 시작");
        stockLoader.load(csvFilePaths.get("stock"));

        log.info("StockPrice CSV 로딩 시작");
        priceLoader.load(csvFilePaths.get("stockPrice"));

        log.info("StockIndexPrice CSV 로딩 시작");
        indexLoader.load(csvFilePaths.get("stockIndexPrice"));

        log.info("ValuationMetric CSV 로딩 시작");
        valuationLoader.load(csvFilePaths.get("valuationMetric"));

        log.info("Prediction CSV 로딩 시작");
        predictionLoader.load(csvFilePaths.get("prediction"));

        log.info("모든 CSV 데이터 로딩 완료");
    }
}

