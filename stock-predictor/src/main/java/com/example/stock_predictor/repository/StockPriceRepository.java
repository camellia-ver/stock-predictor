package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    Optional<StockPrice> findTopByStockOrderByDateDesc(Stock stock);
    List<StockPrice> findByStockAndDateAfterOrderByDateAsc(Stock stock, LocalDate fromDate);

    @Query("""
        SELECT sp FROM StockPrice sp
        WHERE sp.stock.ticker IN :tickers
          AND sp.date = (
              SELECT MAX(sp2.date)
              FROM StockPrice sp2
              WHERE sp2.stock = sp.stock
          )
    """)
    List<StockPrice> findLatestByTickersUsingWindow(@Param("tickers") List<String> tickers);
}
