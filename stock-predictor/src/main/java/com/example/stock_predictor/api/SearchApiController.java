package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.service.SearchService;
import com.example.stock_predictor.service.StockIndexPriceService;
import com.example.stock_predictor.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchApiController {
    private final SearchService searchService;

    @GetMapping("/stocks")
    public  List<StockWithPriceDTO> autocomplete(@RequestParam("query") String query){
        // 이름 또는 티커로 Stock 검색
        List<Stock> stocks = searchService.searchStock(query);

        return stocks.stream().map(stock -> {
            Optional<StockPrice> latestPriceOpt = searchService.searchStockPrice(stock);
            BigDecimal price = null, change = null, changePercent = null;

            if (latestPriceOpt.isPresent()){
                StockPrice laestPrice = latestPriceOpt.get();
                price = laestPrice.getClosePrice();
                changePercent = laestPrice.getChangeRate();
                if (price != null && changePercent != null){
                    change = price.multiply(changePercent).divide(BigDecimal.valueOf(100));
                }
            }

            return new StockWithPriceDTO(
                    stock.getName(),
                    stock.getTicker(),
                    stock.getSector(),
                    stock.getMarket(),
                    price,
                    change,
                    changePercent
            );
        }).collect(Collectors.toList());
    }
}
