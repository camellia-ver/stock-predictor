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
    private final FavoriteService favoriteService;
    private final Calculator calculator;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null){
            return "intro";
        }

        StockIndexPrice kospiPrice = stockIndexPriceService.getLatestIndex("KOSPI");
        StockIndexPrice kosdaqPrice = stockIndexPriceService.getLatestIndex("KOSDAQ");

        model.addAttribute("kospi", kospiPrice);
        model.addAttribute("kosdaq", kosdaqPrice);

        var kospiStatus = calculator.calculateIndex(kospiPrice.getClosePrice(), kospiPrice.getOpenPrice());
        model.addAttribute("kospiIsRising", kospiStatus.isRising());
        model.addAttribute("kospiRate", kospiStatus.rate());

        var kosdaqStatus = calculator.calculateIndex(kosdaqPrice.getClosePrice(), kosdaqPrice.getOpenPrice());
        model.addAttribute("kosdaqIsRising", kosdaqStatus.isRising());
        model.addAttribute("kosdaqRate", kosdaqStatus.rate());

        List<StockWithPriceDTO> favoritesDTO = favoriteService.getFavoriteDTOs(userDetails.getUsername(), 5);
        model.addAttribute("favorites", favoritesDTO);

        return "dashboard";
    }
}
