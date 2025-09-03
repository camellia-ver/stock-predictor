package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.ValuationMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValuationMetricRepository extends JpaRepository<ValuationMetric, Long> {
    Optional<ValuationMetric> findTopByStockOrderByDateDesc(Stock stock);
}
