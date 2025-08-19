package com.example.stock_predictor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewsArticleController {
    @GetMapping("/news-list")
    public String newsList(){
        return "news-list";
    }

    @GetMapping("/news-detail")
    public String newsDetail(){
        return "news-detail";
    }
}
