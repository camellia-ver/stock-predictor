package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.repository.StockPriceRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.service.SearchService;
import com.example.stock_predictor.service.StockIndexPriceService;
import com.example.stock_predictor.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchApiController {
    private final SearchService searchService;

    @GetMapping("/stocks")
    public  List<StockWithPriceDTO> autocomplete(@RequestParam("query") String query){
        return searchService.searchStockWithPrice(query);
    }
}
