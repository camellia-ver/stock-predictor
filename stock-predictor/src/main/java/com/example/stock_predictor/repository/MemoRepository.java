package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByUserIdAndStockId(Long userId, Long stockId);
}
