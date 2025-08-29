package com.example.stock_predictor.api;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchApiController {
    private final StockRepository stockRepository;

    @GetMapping("/stocks")
    public List<Map<String, String>> autocomplete(@RequestParam("query") String query){
        List<Stock> stocks = stockRepository.findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(query, query);

        return stocks.stream()
                .map(s -> Map.of("name",s.getName(),"ticker",s.getTicker()))
                .collect(Collectors.toList());
    }
}
