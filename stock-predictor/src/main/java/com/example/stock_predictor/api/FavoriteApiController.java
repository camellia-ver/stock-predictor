package com.example.stock_predictor.api;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.service.FavoriteService;
import com.example.stock_predictor.service.StockService;
import com.example.stock_predictor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteApiController {
    private final UserService userService;
    private final StockService stockService;
    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam String ticker,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        if (userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByEmail(userDetails.getUsername());
        Stock stock = stockService.getStockByTicker(ticker);

        boolean isNowFavorite = favoriteService.toggleFavorite(user, stock);

        Map<String, Object> response = new HashMap<>();
        response.put("isFavorite", isNowFavorite);
        return ResponseEntity.ok(response);
    }
}
