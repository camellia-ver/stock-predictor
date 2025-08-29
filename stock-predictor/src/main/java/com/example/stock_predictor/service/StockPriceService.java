package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockPriceService {
    private final StockPriceRepository stockPriceRepository;

    public Optional<StockPrice> getLatestPrice(Stock stock){
        return stockPriceRepository.findTopByStockOrderByDateDesc(stock);
    }
}
