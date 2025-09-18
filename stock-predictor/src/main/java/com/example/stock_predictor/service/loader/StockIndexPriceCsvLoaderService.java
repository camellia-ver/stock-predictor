package com.example.stock_predictor.service.loader;

import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockIndexPriceCsvLoaderService {
    private static final int BATCH_SIZE = 1000;
    private final StockIndexPriceRepository repository;
    private final EntityManager em;

    @Transactional
    public void load(String filePath) throws IOException, CsvValidationException {
        if (!CsvUtils.fileExists(filePath)) {
            log.warn("CSV 파일이 존재하지 않습니다: {}", filePath);
            return;
        }

        List<StockIndexPrice> buffer = new ArrayList<>(BATCH_SIZE);
        int totalCount = 0;

        try (CSVReader reader = CsvUtils.openCsvReader(filePath)) {
            CsvUtils.skipHeader(reader);
            String[] cols;

            while ((cols = reader.readNext()) != null) {
                if (cols.length < 9) {
                    log.warn("컬럼 부족 row: {}", Arrays.toString(cols));
                    continue;
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(cols[0]);
                } catch (DateTimeParseException e) {
                    log.warn("날짜 파싱 실패: {}", cols[0]);
                    continue;
                }

                log.info("추가 대상 row: {}", Arrays.toString(cols));
                buffer.add(StockIndexPrice.builder()
                        .indexName(cols[8])
                        .date(date)
                        .openPrice(NumberParseUtils.parseBigDecimalOrNull(cols[1]))
                        .highPrice(NumberParseUtils.parseBigDecimalOrNull(cols[2]))
                        .lowPrice(NumberParseUtils.parseBigDecimalOrNull(cols[3]))
                        .closePrice(NumberParseUtils.parseBigDecimalOrNull(cols[4]))
                        .volume(NumberParseUtils.parseLongOrNull(cols[5]))
                        .value(NumberParseUtils.parseLongOrNull(cols[6]))
                        .marketCap(NumberParseUtils.parseLongOrNull(cols[7]))
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
