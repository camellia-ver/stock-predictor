package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.StockIndexPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockIndexPriceRepository extends JpaRepository<StockIndexPrice, Long> {
    Optional<StockIndexPrice> findTopByIndexNameOrderByDateDesc(String indexName);
}
