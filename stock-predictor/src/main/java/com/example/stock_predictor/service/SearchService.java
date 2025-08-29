package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final StockRepository stockRepository;

    public List<Stock> searchStock(String query){
        return stockRepository.findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(query, query);
    }
}
