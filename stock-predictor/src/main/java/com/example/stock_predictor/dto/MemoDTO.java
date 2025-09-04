package com.example.stock_predictor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MemoDTO {
    private String ticker;
    private String title;
    private String content;
    private LocalDateTime stockDate;
}
