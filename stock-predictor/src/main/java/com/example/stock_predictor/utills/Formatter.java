package com.example.stock_predictor.utills;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Formatter {
    public String formattingDate(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        return today.format(formatter);
    }
}
