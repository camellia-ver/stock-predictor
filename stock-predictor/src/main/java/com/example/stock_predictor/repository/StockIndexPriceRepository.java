package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.StockIndexPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockIndexPriceRepository extends JpaRepository<StockIndexPrice, Long> {
}
