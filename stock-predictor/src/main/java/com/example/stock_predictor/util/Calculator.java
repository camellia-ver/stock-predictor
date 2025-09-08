package com.example.stock_predictor.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calculator {
    public record IndexStatus(boolean isRising, BigDecimal rate){}

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public IndexStatus calculateIndex(BigDecimal close, BigDecimal open){
        if (close == null || open == null || open.compareTo(BigDecimal.ZERO) == 0){
            return new IndexStatus(false, BigDecimal.ZERO);
        }

        boolean isRising = close.compareTo(open) > 0;
        BigDecimal rate = close.subtract(open)
                .divide(open, 8, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
        
        return new IndexStatus(isRising, rate);
    }
}
