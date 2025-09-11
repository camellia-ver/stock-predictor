package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    public Stock getStockByTicker(String ticker){
        return stockRepository.findByTicker(ticker)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock not found for ticker: " + ticker
                ));
    }

    @Transactional
    public void syncWithCsv(List<Stock> csvData) {
        // CSV에 있는 모든 ticker 수집
        List<String> csvTickers = csvData.stream()
                .map(Stock::getTicker)
                .toList();

        // DB에서 모든 stock 조회
        List<Stock> dbData = stockRepository.findAll();
        Map<String, Stock> dbMap = dbData.stream()
                .collect(Collectors.toMap(Stock::getTicker, Function.identity()));

        List<Stock> toSave = new ArrayList<>();

        // 1. CSV 기반 insert/update
        for (Stock csvEntity : csvData) {
            Stock dbEntity = dbMap.get(csvEntity.getTicker());
            if (dbEntity == null) {
                // DB에 없는 경우 새로 추가
                toSave.add(csvEntity);
            } else if (csvEntity.getDate().isAfter(dbEntity.getDate())) {
                // DB에 있고 CSV가 최신이면 새로운 객체 생성 후 저장
                Stock updated = Stock.builder()
                        .id(dbEntity.getId()) // 기존 ID 유지
                        .ticker(csvEntity.getTicker())
                        .name(csvEntity.getName())
                        .market(csvEntity.getMarket())
                        .sector(csvEntity.getSector())
                        .date(csvEntity.getDate())
                        .build();
                toSave.add(updated);
            }
        }

        if (!toSave.isEmpty()) {
            stockRepository.saveAll(toSave);
        }

        // 2. CSV에 없는 DB 데이터 삭제
        List<Stock> toDelete = dbData.stream()
                .filter(db -> csvData.stream()
                        .noneMatch(csv -> csv.getTicker().equals(db.getTicker())))
                .toList();

        if (!toDelete.isEmpty()) {
            stockRepository.deleteAll(toDelete);
        }
    }
}
