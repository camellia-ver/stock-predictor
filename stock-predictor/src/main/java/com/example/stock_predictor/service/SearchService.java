package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    public List<Stock> searchStock(String query){
        return stockRepository.findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(query, query);
    }

    public Optional<StockPrice> searchStockPrice(Stock stock){
        return stockPriceRepository.findTopByStockOrderByDateDesc(stock);
    }
}
