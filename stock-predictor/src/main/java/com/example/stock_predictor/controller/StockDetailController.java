package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.model.ValuationMetric;
import com.example.stock_predictor.service.StockService;
import com.example.stock_predictor.service.UserService;
import com.example.stock_predictor.service.ValuationMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StockDetailController {
    private final StockService stockService;
    private final UserService userService;
    private final ValuationMetricService valuationMetricService;

    @GetMapping("/stock-detail")
    public String stockDetail(@RequestParam String ticker, Model model,
                              @AuthenticationPrincipal UserDetails userDetails
                              ){
        Stock stock = stockService.getStockByTicker(ticker);
        model.addAttribute("stock",stock);

        ValuationMetric latestMetric = valuationMetricService.getLatestByStock(stock);
        model.addAttribute("valuationMetric",latestMetric);

        boolean isFavorite = false;

        if (userDetails != null){
            User user = userService.findByEmail(userDetails.getUsername());
            isFavorite = user.getFavorites().stream()
                    .anyMatch(fav -> fav.getStock().getTicker().equals(ticker));
        }

        model.addAttribute("isFavorite",isFavorite);

        return "stock-detail";
    }
}
