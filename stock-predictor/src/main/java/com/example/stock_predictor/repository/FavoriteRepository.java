package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Favorite;
import com.example.stock_predictor.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUser_UserNameAndStock_Ticker(String userName, String ticker);
    List<Favorite> findTop5ByUserOrderByCreatedAtDesc(User user);
    Page<Favorite> findByUser(User user, Pageable pageable);
}
