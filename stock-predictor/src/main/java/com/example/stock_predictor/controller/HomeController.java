package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.service.StockIndexPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final StockIndexPriceService stockIndexPriceService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            StockIndexPrice kospi = stockIndexPriceService.getLatestIndex("KOSPI");
            StockIndexPrice kosdaq = stockIndexPriceService.getLatestIndex("KOSDAQ");

            model.addAttribute("kospi", kospi);
            model.addAttribute("kosdaq", kosdaq);

            // KOSPI 계산
            BigDecimal kospiClose = kospi.getClosePrice();
            BigDecimal kospiOpen = kospi.getOpenPrice();
            boolean kospiIsRising = false;
            BigDecimal kospiRate = BigDecimal.ZERO;

            if (kospiClose != null && kospiOpen != null) {
                kospiIsRising = kospiClose.compareTo(kospiOpen) > 0;
                kospiRate = kospiClose.subtract(kospiOpen)
                        .divide(kospiOpen, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            model.addAttribute("kospiIsRising", kospiIsRising);
            model.addAttribute("kospiRate", kospiRate);

            // KOSDAQ 계산
            BigDecimal kosdaqClose = kosdaq.getClosePrice();
            BigDecimal kosdaqOpen = kosdaq.getOpenPrice();
            boolean kosdaqIsRising = false;
            BigDecimal kosdaqRate = BigDecimal.ZERO;

            if (kosdaqClose != null && kosdaqOpen != null) {
                kosdaqIsRising = kosdaqClose.compareTo(kosdaqOpen) > 0;
                kosdaqRate = kosdaqClose.subtract(kosdaqOpen)
                        .divide(kosdaqOpen, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            model.addAttribute("kosdaqIsRising", kosdaqIsRising);
            model.addAttribute("kosdaqRate", kosdaqRate);

            return "dashboard";
        }
        return "intro";
    }
}
