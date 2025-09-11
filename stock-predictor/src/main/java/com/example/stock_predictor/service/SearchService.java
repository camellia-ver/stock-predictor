package com.example.stock_predictor.service;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    public List<Stock> searchStock(String query){
        return stockRepository.findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(query, query);
    }

    public Optional<StockPrice> searchStockPrice(Stock stock){
        return stockPriceRepository.findTopByStockOrderByDateDesc(stock);
    }

    public StockWithPriceDTO toStockWithPriceDTO(Stock stock, StockPrice latestPrice){
        BigDecimal price = Optional.ofNullable(latestPrice).map(StockPrice::getClosePrice).orElse(null);
        BigDecimal changePercent = Optional.ofNullable(latestPrice).map(StockPrice::getChangeRate).orElse(null);
        BigDecimal change = (price != null && changePercent != null)
                ? price.multiply(changePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;

        return new StockWithPriceDTO(
                stock.getName(),
                stock.getTicker(),
                stock.getSector(),
                stock.getMarket(),
                price,
                change,
                changePercent
        );
    }

    public List<StockWithPriceDTO> searchStockWithPrice(String query){
        return searchStock(query).stream()
                .map(stock -> {
                    StockPrice latestPrice = searchStockPrice(stock).orElse(null);
                    return toStockWithPriceDTO(stock, latestPrice);
                })
                .collect(Collectors.toList());
    }
}
