package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Memo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    Page<Memo> findByStockIdAndUserIdOrderByCreatedAtDesc(Long stockId, Long userId, Pageable pageable);
    Page<Memo> findByUserId(Long userId, Pageable pageable);
    Page<Memo> findByUserIdAndStock_Ticker(Long userId, String ticker, Pageable pageable);
}
