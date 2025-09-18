package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Prediction;
import com.example.stock_predictor.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    @Query("SELECT p FROM Prediction p " +
            "WHERE p.stock = :stock " +
            "AND p.predictionDate = (SELECT MAX(p2.predictionDate) FROM Prediction p2 WHERE p2.stock = :stock AND p2.targetDate = p.targetDate AND p2.modelName = p.modelName) " +
            "ORDER BY p.targetDate ASC")
    List<Prediction> findByStockOrderByTargetDateAsc(Stock stock);
}
