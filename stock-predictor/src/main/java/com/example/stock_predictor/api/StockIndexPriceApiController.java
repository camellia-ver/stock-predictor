package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.StockIndexPriceDTO;
import com.example.stock_predictor.model.enums.Period;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import com.example.stock_predictor.service.StockIndexPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class StockIndexPriceApiController {
    private final StockIndexPriceService stockIndexPriceService;

    // 모든 지수 이름 가져오기
    @GetMapping("/names")
    public List<String> getIndexNames() {
        return stockIndexPriceService.getIndexNames();
    }

    // 특정 지수의 기간별 데이터 가져오기
    @GetMapping("/{indexName}")
    public List<StockIndexPriceDTO> getIndexData(
            @PathVariable String indexName,
            @RequestParam String period // week, month, year
    ) {
        LocalDate end = LocalDate.now();
        Period periodEnum = Period.fromString(period);

        return stockIndexPriceService.getIndexData(indexName, periodEnum, end);
    }
}
