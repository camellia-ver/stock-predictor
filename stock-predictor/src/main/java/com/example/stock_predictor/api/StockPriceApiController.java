package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.StockPriceByTickerDTO;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.model.enums.Period;
import com.example.stock_predictor.service.StockPriceService;
import com.example.stock_predictor.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/stock-prices")
@RequiredArgsConstructor
public class StockPriceApiController {
    private final StockService stockService;
    private final StockPriceService stockPriceService;

    @GetMapping("/{ticker}/prices")
    public List<StockPriceByTickerDTO> getStockPrice(@PathVariable String ticker,
                                                     @RequestParam String period){
        Stock stock = stockService.getStockByTicker(ticker);

        Period periodEnum = Period.fromString(period);
        LocalDate fromDate = periodEnum.calculateStartDate(LocalDate.now());

        List<StockPrice> prices = stockPriceService.getPrice(stock, fromDate);

        return prices.stream()
                .filter(Objects::nonNull)
                .map(sp -> new StockPriceByTickerDTO(
                        sp.getDate(),
                        sp.getOpenPrice(),
                        sp.getHighPrice(),
                        sp.getLowPrice(),
                        sp.getClosePrice(),
                        sp.getVolume(),
                        sp.getChangeRate(),
                        stock.getName()
                ))
                .toList();
    }
}
