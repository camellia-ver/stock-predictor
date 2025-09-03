package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.ValuationMetric;
import com.example.stock_predictor.repository.ValuationMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValuationMetricService {
    private final ValuationMetricRepository valuationMetricRepository;

    public ValuationMetric getLatestByStock(Stock stock){
        return valuationMetricRepository.findTopByStockOrderByDateDesc(stock)
                .orElse(null);
    }
}
