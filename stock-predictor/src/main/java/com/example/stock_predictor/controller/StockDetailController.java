package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.*;
import com.example.stock_predictor.service.*;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StockDetailController {
    private final StockService stockService;
    private final UserService userService;
    private final ValuationMetricService valuationMetricService;
    private final MemoService memoService;
    private final PredictionService predictionService;

    @GetMapping("/stock-detail")
    public String stockDetail(@RequestParam String ticker,
                              @RequestParam(defaultValue = "0") int page,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails
                              ){
        Stock stock = stockService.getStockByTicker(ticker);
        if (stock == null) return "error/404";

        model.addAttribute("stock",stock);
        model.addAttribute("valuationMetric", valuationMetricService.getLatestByStock(stock));

        boolean isFavorite = false;
        Page<Memo> memoPage = Page.empty();

        if (userDetails != null){
            User user = userService.findByEmail(userDetails.getUsername());
            isFavorite = user.getFavorites().stream()
                    .anyMatch(fav -> fav.getStock().getTicker().equals(ticker));

            memoPage = memoService.getUserMemosByStock(stock.getId(), user.getId(),
                    PageRequest.of(page, 6));
        }

        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("memoPage", memoPage);
        model.addAttribute("predictionsByDateAndModel",
                predictionService.getPredictionsGroupedByDateAndModel(stock));

        return "stock-detail";
    }
}
