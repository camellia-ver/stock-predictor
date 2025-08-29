package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Favorite;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.repository.FavoriteRepository;
import com.example.stock_predictor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public List<Favorite> getFavorites(String email){
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return currentUser.getFavorites();
    }

    public boolean toggleFavorite(User user, Stock stock){
        Optional<Favorite> existing = favoriteRepository.findByUser_UserNameAndStock_Ticker(user.getUserName(), stock.getTicker());

        if (existing.isPresent()){
            favoriteRepository.delete(existing.get());
            return false;
        }else {
            Favorite fav = Favorite.builder()
                    .user(user)
                    .stock(stock)
                    .createdAt(LocalDateTime.now())
                    .build();
            favoriteRepository.save(fav);
            return true;
        }
    }
}
