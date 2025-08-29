package com.example.stock_predictor.controller;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Favorite;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.service.FavoriteService;
import com.example.stock_predictor.service.StockPriceService;
import lombok.RequiredArgsConstructor;
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
public class FavoritesController {
    private final FavoriteService favoriteService;
    private final StockPriceService stockPriceService;

    @GetMapping("/favorites")
    public String favorites(Model model, @AuthenticationPrincipal UserDetails userDetails){
        List<Favorite> favorites = favoriteService.getFavorites(userDetails.getUsername());
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

        return "favorites";
    }
}
