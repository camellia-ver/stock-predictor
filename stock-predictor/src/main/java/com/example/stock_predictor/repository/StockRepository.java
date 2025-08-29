package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByTicker(String ticker);
    List<Stock> findByTickerIn(Collection<String> tickers);

    // 이름(name) 또는 심볼(symbol)에 query가 포함된 경우, 대소문자 구분 없이 검색
    List<Stock> findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(String nameQuery, String TickerQuery);
}
