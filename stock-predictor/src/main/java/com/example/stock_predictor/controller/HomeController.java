package com.example.stock_predictor.controller;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Favorite;
import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.service.FavoriteService;
import com.example.stock_predictor.service.StockIndexPriceService;
import com.example.stock_predictor.service.StockPriceService;
import com.example.stock_predictor.util.Calculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final StockIndexPriceService stockIndexPriceService;
    private final StockPriceService stockPriceService;
    private final FavoriteService favoriteService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication,
                       @AuthenticationPrincipal UserDetails userDetails) {
        if (authentication != null && authentication.isAuthenticated() && userDetails != null) {
            StockIndexPrice kospiPrice = stockIndexPriceService.getLatestIndex("KOSPI");
            StockIndexPrice kosdaqPrice = stockIndexPriceService.getLatestIndex("KOSDAQ");

            model.addAttribute("kospi", kospiPrice);
            model.addAttribute("kosdaq", kosdaqPrice);

            Calculator calculator = new Calculator();

            // KOSPI 계산
            Calculator.IndexStatus kospi = calculator.calculateIndex(kospiPrice.getClosePrice(),kospiPrice.getOpenPrice());
            model.addAttribute("kospiIsRising", kospi.isRising());
            model.addAttribute("kospiRate", kospi.rate());

            // KOSDAQ 계산
            Calculator.IndexStatus kosdaq = calculator.calculateIndex(kosdaqPrice.getClosePrice(),kosdaqPrice.getOpenPrice());
            model.addAttribute("kosdaqIsRising", kosdaq.isRising());
            model.addAttribute("kosdaqRate", kosdaq.rate());

            List<Favorite> favorites = favoriteService.getFavoritesLimited(userDetails.getUsername(), true);
            List<StockWithPriceDTO> favoritesDTO = favorites.stream()
                    .map(f -> {
                        Optional<StockPrice> latestPriceOpt = stockPriceService.getLatestPrice(f.getStock());
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
                                f.getStock().getName(),
                                f.getStock().getTicker(),
                                f.getStock().getSector(),
                                f.getStock().getMarket(),
                                price,
                                change,
                                changePercent
                        );
                    })
                    .collect(Collectors.toList());

            model.addAttribute("favorites", favoritesDTO);

            return "dashboard";
        }
        return "intro";
    }
}
