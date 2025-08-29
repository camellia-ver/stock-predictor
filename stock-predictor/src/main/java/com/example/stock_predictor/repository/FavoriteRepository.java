package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUser_UserNameAndStock_Ticker(String userName, String ticker);
}
