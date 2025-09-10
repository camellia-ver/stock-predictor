package com.example.stock_predictor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MemoDTO {
    @NotBlank(message = "Ticker is required")
    private String ticker;

    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Stock date is required")
    private LocalDateTime stockDate;
}
