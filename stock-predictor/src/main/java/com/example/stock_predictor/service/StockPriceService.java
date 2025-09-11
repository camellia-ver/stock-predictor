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
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (distinctTickers.isEmpty()){
            return Collections.emptyMap();
        }

        List<StockPrice> latestPrices = stockPriceRepository.findLatestByTickersUsingWindow(distinctTickers);

        return latestPrices.stream()
                .filter(sp -> sp != null && sp.getStock() != null && sp.getStock().getTicker() != null)
                .collect(Collectors.toMap(
                        sp -> sp.getStock().getTicker(),
                        sp -> sp,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}
