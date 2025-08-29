package com.example.stock_predictor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class StockWithPriceDTO {
    private String name;
    private String ticker;
    private String sector;
    private String market;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
}
