package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model){
        List<Stock> results = searchService.searchStock(query);
        model.addAttribute("query", query);
        model.addAttribute("results",results);
        return  "search-results";
    }
}
