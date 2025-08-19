package com.example.stock_predictor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemoController {
    @GetMapping("/memo")
    public String memo(){
        return "memo";
    }

    @GetMapping("/memo-detail")
    public String memoDetail(){
        return "memo-detail";
    }
}
