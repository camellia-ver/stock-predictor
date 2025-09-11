package com.example.stock_predictor.service;

import com.example.stock_predictor.dto.StockIndexPriceDTO;
import com.example.stock_predictor.model.StockIndexPrice;
import com.example.stock_predictor.model.enums.Period;
import com.example.stock_predictor.repository.StockIndexPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockIndexPriceService {
    private final StockIndexPriceRepository stockIndexPriceRepository;

    public List<StockIndexPriceDTO> getIndexData(String indexName, Period period, LocalDate endDate){
        LocalDate startDate = period.calculateStartDate(endDate);

        return stockIndexPriceRepository.findByIndexNameAndDateBetween(indexName, startDate, endDate)
                .stream()
                .map(p -> new StockIndexPriceDTO(
                        p.getIndexName(),
                        p.getDate(),
                        p.getOpenPrice(),
                        p.getClosePrice(),
                        p.getHighPrice(),
                        p.getLowPrice(),
                        p.getVolume()
                ))
                .toList();
    }

    public StockIndexPriceDTO getLatestIndex(String indexName){
        StockIndexPrice latest =  stockIndexPriceRepository.findTopByIndexNameOrderByDateDesc(indexName)
                .orElseThrow(() -> new IllegalArgumentException("데이터 없음: " + indexName));

        return new StockIndexPriceDTO(
                latest.getIndexName(),
                latest.getDate(),
                latest.getOpenPrice(),
                latest.getClosePrice(),
                latest.getHighPrice(),
                latest.getLowPrice(),
                latest.getVolume()
        );
    }

    public List<String> getIndexNames(){
        return stockIndexPriceRepository.findDistinctIndexNames();
    }
}
