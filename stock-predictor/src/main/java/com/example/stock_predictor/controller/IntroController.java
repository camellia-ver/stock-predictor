package com.example.stock_predictor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IntroController {
    @GetMapping("/")
    public String intro(){
        return "intro";
    }
}
