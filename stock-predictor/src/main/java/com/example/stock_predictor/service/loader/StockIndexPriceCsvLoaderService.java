package com.example.stock_predictor.service.loader;

import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import com.example.stock_predictor.util.CsvUtils;
import com.example.stock_predictor.util.NumberParseUtils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockIndexPriceCsvLoaderService {

    private static final int BATCH_SIZE = 1000;
    private final StockIndexPriceRepository repository;

    public void load(String filePath) throws IOException, CsvValidationException {
        if (!CsvUtils.fileExists(filePath)) {
            log.warn("CSV 파일이 존재하지 않습니다: {}", filePath);
            return;
        }

        List<StockIndexPrice> buffer = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);
            String[] cols;

            while ((cols = reader.readNext()) != null) {
                if (cols.length < 9) continue;

                LocalDate date;
                try { date = LocalDate.parse(cols[8]); } catch (DateTimeParseException e) { continue; }

                buffer.add(StockIndexPrice.builder()
                        .indexName(cols[7])
                        .date(date)
                        .openPrice(NumberParseUtils.parseBigDecimalOrNull(cols[0]))
                        .highPrice(NumberParseUtils.parseBigDecimalOrNull(cols[1]))
                        .lowPrice(NumberParseUtils.parseBigDecimalOrNull(cols[2]))
                        .closePrice(NumberParseUtils.parseBigDecimalOrNull(cols[3]))
                        .volume(NumberParseUtils.parseLongOrNull(cols[4]))
                        .value(NumberParseUtils.parseLongOrNull(cols[5]))
                        .marketCap(NumberParseUtils.parseLongOrNull(cols[6]))
                        .build());

                saveBatchIfNeeded(buffer);
            }
        }

        saveBatchIfNeeded(buffer);
    }

    private void saveBatchIfNeeded(List<StockIndexPrice> buffer) {
        if (!buffer.isEmpty() && buffer.size() >= BATCH_SIZE) {
            repository.saveAll(buffer);
            buffer.clear();
        }
    }
}
