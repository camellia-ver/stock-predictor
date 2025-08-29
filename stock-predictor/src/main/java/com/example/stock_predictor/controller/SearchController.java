package com.example.stock_predictor.controller;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model){
        List<Stock> stocks = searchService.searchStock(query);

        List<StockWithPriceDTO> result = stocks.stream().map(stock -> {
            Optional<StockPrice> latestPriceOpt = searchService.searchStockPrice(stock);
            BigDecimal price = null, change = null, changePercent = null;

            if (latestPriceOpt.isPresent()){
                StockPrice latestPrice = latestPriceOpt.get();
                price = latestPrice.getClosePrice();
                changePercent = latestPrice.getChangeRate();
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

        model.addAttribute("results",result);
        model.addAttribute("query",query);

        return  "search-results";
    }
}
