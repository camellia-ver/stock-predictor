package com.example.stock_predictor.api;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.service.StockPriceService;
import com.example.stock_predictor.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stock-prices")
@RequiredArgsConstructor
public class StockPriceApiController {
    private final StockService stockService;
    private final StockPriceService stockPriceService;

    @GetMapping("/{ticker}/prices")
    public List<StockPrice> getStockPrice(@PathVariable String ticker,
                                          @RequestParam String period){
        Stock stock = stockService.getStockByTicker(ticker);

        LocalDate fromDate = switch (period){
            case "month" -> LocalDate.now().minusMonths(1);
            case "year" -> LocalDate.now().minusYears(1);
            default -> LocalDate.now().minusWeeks(1);
        };

        return stockPriceService.getPrice(stock, fromDate);
    }
}
