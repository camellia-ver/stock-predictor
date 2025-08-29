package com.example.stock_predictor.utills;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calculator {
    public record IndexStatus(boolean isRising, BigDecimal rate){}

    public IndexStatus calculateIndex(BigDecimal close, BigDecimal open){
        boolean isRising = false;
        BigDecimal rate = BigDecimal.ZERO;

        if (close != null && open != null) {
            isRising = close.compareTo(open) > 0;
            rate = close.subtract(open)
                    .divide(open, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        return new IndexStatus(isRising, rate);
    }
}
