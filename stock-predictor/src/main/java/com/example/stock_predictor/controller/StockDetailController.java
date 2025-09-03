package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.model.ValuationMetric;
import com.example.stock_predictor.service.MemoService;
import com.example.stock_predictor.service.StockService;
import com.example.stock_predictor.service.UserService;
import com.example.stock_predictor.service.ValuationMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockDetailController {
    private final StockService stockService;
    private final UserService userService;
    private final ValuationMetricService valuationMetricService;
    private final MemoService memoService;

    @GetMapping("/stock-detail")
    public String stockDetail(@RequestParam String ticker,
                              @RequestParam(defaultValue = "0") int page,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails
                              ){
        Stock stock = stockService.getStockByTicker(ticker);
        model.addAttribute("stock",stock);

        ValuationMetric latestMetric = valuationMetricService.getLatestByStock(stock);
        model.addAttribute("valuationMetric",latestMetric);

        boolean isFavorite = false;
        Page<Memo> memoPage = Page.empty();

        if (userDetails != null){
            User user = userService.findByEmail(userDetails.getUsername());
            isFavorite = user.getFavorites().stream()
                    .anyMatch(fav -> fav.getStock().getTicker().equals(ticker));

            Pageable pageable = PageRequest.of(page, 6);
            memoPage = memoService.getUserMemosByStock(stock.getId(), user.getId() ,pageable);
        }

        model.addAttribute("isFavorite",isFavorite);
        model.addAttribute("memoPage",memoPage);

        return "stock-detail";
    }
}
