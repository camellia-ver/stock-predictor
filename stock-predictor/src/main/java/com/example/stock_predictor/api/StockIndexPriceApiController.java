package com.example.stock_predictor.api;

import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class StockIndexPriceApiController {
    private final StockIndexPriceRepository stockIndexPriceRepository;

    // 모든 지수 이름 가져오기
    @GetMapping("/names")
    public List<String> getIndexNames(){
        return stockIndexPriceRepository.findDistinctIndexNames();
    }

    // 특정 지수의 기간별 데이터 가져오기
    @GetMapping("/{indexName}")
    public List<StockIndexPrice> getIndexData(
            @PathVariable String indexName,
            @RequestParam String period // week, month, year
    ) {
        LocalDate end = LocalDate.now();
        LocalDate start;

        switch (period) {
            case "week" -> start = end.minusWeeks(1);
            case "month" -> start = end.minusMonths(1);
            case "year" -> start = end.minusYears(1);
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        }

        return stockIndexPriceRepository.findByIndexNameAndDateBetween(indexName, start, end);
    }
}
