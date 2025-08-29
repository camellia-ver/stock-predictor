package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StockDetailController {
    private final StockService stockService;

    @GetMapping("/stock-detail")
    public String stockDetail(@RequestParam String ticker, Model model){
        // ticker에 맞는 종목 정보 조회
        Stock stock = stockService.getStockByTicker(ticker);
        model.addAttribute("stock",stock);
        return "stock-detail";
    }
}
