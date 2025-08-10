package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.RealtimePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealTimePriceRepository extends JpaRepository<RealtimePrice, Long> {
}
