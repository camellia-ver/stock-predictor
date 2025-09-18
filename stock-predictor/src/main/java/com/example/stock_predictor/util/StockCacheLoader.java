package com.example.stock_predictor.util;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockCacheLoader {
    private final StockRepository stockRepository;

    private Map<String, Stock> stockCache = new HashMap<>();

    @PostConstruct
    public void init(){
        stockCache = stockRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Stock::getTicker, s -> s));
    }

    public Stock getStock(String ticker){
        return  stockCache.get(ticker);
    }
}
