package com.example.stock_predictor.service.loader;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.ValuationMetric;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.repository.ValuationMetricRepository;
import com.example.stock_predictor.util.CsvUtils;
import com.example.stock_predictor.util.NumberParseUtils;
import com.example.stock_predictor.utill.CsvUtils;
import com.example.stock_predictor.utill.StockCacheLoader;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityManager;
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
public class ValuationMetricCsvLoaderService {
    private static final int BATCH_SIZE = 1000;
    private final ValuationMetricRepository repository;
    private final StockCacheLoader stockCacheLoader;
    private final EntityManager em;

    public void load(String filePath) throws IOException, CsvValidationException {
        if (!CsvUtils.fileExists(filePath)) {
            log.warn("CSV 파일이 존재하지 않습니다: {}", filePath);
            return;
        }

        Map<String, Stock> stockCache = stockCacheLoader.loadStockCache(filePath);
        List<ValuationMetric> buffer = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);
            String[] cols;

            while ((cols = reader.readNext()) != null) {
                if (cols.length < 9) continue;

                LocalDate date;
                try { date = LocalDate.parse(cols[0]); } catch (DateTimeParseException e) { continue; }

                Stock stock = stockCache.get(cols[7]);
                if (stock == null) continue;

                buffer.add(ValuationMetric.builder()
                        .stock(stock)
                        .date(date)
                        .roe(NumberParseUtils.parseBigDecimalOrNull(cols[8]))
                        .per(NumberParseUtils.parseBigDecimalOrNull(cols[2]))
                        .pbr(NumberParseUtils.parseBigDecimalOrNull(cols[3]))
                        .eps(NumberParseUtils.parseBigDecimalOrNull(cols[4]))
                        .bps(NumberParseUtils.parseBigDecimalOrNull(cols[1]))
                        .dps(NumberParseUtils.parseBigDecimalOrNull(cols[6]))
                        .dividendYield(NumberParseUtils.parseBigDecimalOrNull(cols[5]))
                        .build());

                if (buffer.size() >= BATCH_SIZE) {
                    repository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            repository.saveAll(buffer);
            em.flush();
            em.clear();
            buffer.clear();
        }
    }
}
