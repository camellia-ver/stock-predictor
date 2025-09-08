package com.example.stock_predictor.service;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Favorite;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.repository.FavoriteRepository;
import com.example.stock_predictor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;

    public Page<Favorite> getFavoritesPage(String email, int page, int size){
        User currentUser = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(page,size);

        return favoriteRepository.findByUser(currentUser, pageable);
    }

    public List<Favorite> getFavoritesLimited(String email, boolean limit){
        User currentUser = userService.getUserByEmail(email);

        if (limit){
            return favoriteRepository.findTop5ByUserOrderByCreatedAtDesc(currentUser);
        }

        return currentUser.getFavorites();
    }

    @Transactional
    public boolean toggleFavorite(User user, Stock stock){
        return favoriteRepository.findByUser_UserNameAndStock_Ticker(user.getUserName(), stock.getTicker())
                .map(fav -> {
                    favoriteRepository.delete(fav);
                    return false;
                }).orElseGet(() -> {
                    Favorite newFav = Favorite.builder()
                            .user(user)
                            .stock(stock)
                            .createdAt(LocalDateTime.now())
                            .build();
                    favoriteRepository.save(newFav);
                    return true;
                });
    }

    public StockWithPriceDTO toStockWithPriceDTO(Favorite favorite, StockPrice latestPrice){
        BigDecimal price = null, change = null, changePercent = null;

        if (latestPrice != null){
            price = latestPrice.getClosePrice();
            changePercent = latestPrice.getChangeRate();
            if (price != null && changePercent != null){
                change = price.multiply(changePercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        return new StockWithPriceDTO(
                favorite.getStock().getName(),
                favorite.getStock().getTicker(),
                favorite.getStock().getSector(),
                favorite.getStock().getMarket(),
                price,
                change,
                changePercent
        );
    }
}
