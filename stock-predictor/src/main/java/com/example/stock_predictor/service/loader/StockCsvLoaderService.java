package com.example.stock_predictor.service.loader;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.util.CsvUtils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCsvLoaderService {

    private static final int BATCH_SIZE = 1000;
    private final StockRepository stockRepository;

    public List<Stock> load(String filePath) throws IOException, CsvValidationException {
        if (!CsvUtils.fileExists(filePath)) {
            log.warn("CSV 파일이 존재하지 않습니다: {}", filePath);
            return Collections.emptyList();
        }

        List<Stock> buffer = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);

            String[] cols;
            while ((cols = reader.readNext()) != null) {
                if (cols.length < 5) continue;

                LocalDate date;
                try {
                    date = LocalDate.parse(cols[4]);
                } catch (DateTimeParseException e) {
                    continue;
                }

                buffer.add(Stock.builder()
                        .ticker(cols[0])
                        .name(cols[1])
                        .market(cols[2])
                        .sector(cols[3])
                        .date(date)
                        .build());

                if (buffer.size() >= BATCH_SIZE) {
                    stockRepository.saveAll(buffer);
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            stockRepository.saveAll(buffer);
        }

        return buffer;
    }
}
