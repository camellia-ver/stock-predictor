package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.ValuationMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValuationMetricRepository extends JpaRepository<ValuationMetric, Long> {
}
