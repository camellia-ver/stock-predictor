package com.example.stock_predictor.service.loader;

import com.example.stock_predictor.model.Prediction;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.PredictionRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.util.NumberParseUtils;
import com.example.stock_predictor.util.CsvUtils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionCsvLoaderService {

    private static final int BATCH_SIZE = 1000;
    private final PredictionRepository repository;
    private final StockRepository stockRepository;
    private final EntityManager em;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void load(String filePath) throws IOException, CsvValidationException {
        if (!CsvUtils.fileExists(filePath)) {
            log.warn("CSV 파일이 존재하지 않습니다: {}", filePath);
            return;
        }

        List<Prediction> buffer = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);
            String[] cols;

            while ((cols = reader.readNext()) != null) {
                if (cols.length < 7) continue;

                LocalDate predictionDate;
                LocalDate targetDate;
                LocalDateTime createdAt;

                try {
                    predictionDate = LocalDate.parse(cols[1]);
                    targetDate = LocalDate.parse(cols[2]);
                    createdAt = LocalDateTime.parse(cols[6], DATETIME_FORMATTER);
                } catch (DateTimeParseException e) { continue; }

                Long stockId;
                try { stockId = Long.parseLong(cols[0].trim()); } catch (NumberFormatException e) {
                    log.warn("예측 CSV: 주식 ID 파싱 실패 — 행 스킵: {}", Arrays.toString(cols));
                    continue;
                }

                Stock stock = stockRepository.findById(stockId)
                        .orElseThrow(() -> new RuntimeException("해당 주식이 존재하지 않습니다. id=" + stockId));

                buffer.add(Prediction.builder()
                        .stock(stock)
                        .predictionDate(predictionDate)
                        .targetDate(targetDate)
                        .modelName(cols[3])
                        .upProb(NumberParseUtils.parseBigDecimalOrNull(cols[4]))
                        .downProb(NumberParseUtils.parseBigDecimalOrNull(cols[5]))
                        .createdAt(createdAt)
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
