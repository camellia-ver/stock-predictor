package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.StockIndexPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockIndexPriceRepository extends JpaRepository<StockIndexPrice, Long> {
    Optional<StockIndexPrice> findTopByIndexNameOrderByDateDesc(String indexName);
    @Query("SELECT DISTINCT s.indexName FROM StockIndexPrice s")
    List<String> findDistinctIndexNames();

    List<StockIndexPrice> findByIndexNameAndDateBetween(
            String indexName, LocalDate start, LocalDate end);
}
