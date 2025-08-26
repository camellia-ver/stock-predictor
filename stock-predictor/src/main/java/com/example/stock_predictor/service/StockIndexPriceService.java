package com.example.stock_predictor.service;

import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockIndexPriceService {
    private final StockIndexPriceRepository stockIndexPriceRepository;

    public StockIndexPrice getLatestIndex(String indexName){
        return stockIndexPriceRepository.findTopByIndexNameOrderByDateDesc(indexName)
                .orElseThrow(() -> new IllegalArgumentException("데이터 없음: " + indexName));
    }
}
