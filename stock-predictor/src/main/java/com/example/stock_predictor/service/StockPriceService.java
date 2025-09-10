package com.example.stock_predictor.service;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockPriceService {
    private final StockPriceRepository stockPriceRepository;

    public Optional<StockPrice> getLatestPrice(Stock stock){
        return stockPriceRepository.findTopByStockOrderByDateDesc(stock);
    }

    public List<StockPrice> getPrice(Stock stock, LocalDate fromDate){
        return stockPriceRepository.findByStockAndDateAfterOrderByDateAsc(stock, fromDate);
    }

    @Transactional(readOnly = true)
    public Map<String, StockPrice> getLatestPricesForTickers(List<String> tickers){
        if (tickers == null || tickers.isEmpty()){
            return Collections.emptyMap();
        }

        List<String> distinctTickers = tickers.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .distinct()
                .toList();

        if (distinctTickers.isEmpty()){
            return Collections.emptyMap();
        }

        List<StockPrice> latestPrices = stockPriceRepository.findLatestByTickersUsingWindow(distinctTickers);

        Map<String, StockPrice> map = new LinkedHashMap<>();
        for (StockPrice sp : latestPrices){
            if (sp != null && sp.getStock().getTicker() != null){
                map.put(sp.getStock().getTicker(),sp);
            }
        }

        return map;
    }
}
