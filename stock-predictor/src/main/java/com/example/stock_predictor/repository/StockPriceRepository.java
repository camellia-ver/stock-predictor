package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    // ticker 기준 최신 가격 1건 조회
    Optional<StockPrice> findTopByStockOrderByDateDesc(Stock stock);
}
