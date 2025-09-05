package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Prediction;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {
    private final PredictionRepository predictionRepository;

    public Map<LocalDate, Map<String, List<Prediction>>> getPredictionsGroupedByDateAndModel(Stock stock){
        List<Prediction> predictions = predictionRepository.findByStockOrderByTargetDateAsc(stock);

        return predictions.stream()
                .collect(Collectors.groupingBy(
                        Prediction::getTargetDate,
                        TreeMap::new,
                        Collectors.groupingBy(Prediction::getModelName)
                ));
    }
}
