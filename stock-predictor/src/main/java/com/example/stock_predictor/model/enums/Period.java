package com.example.stock_predictor.model.enums;

import java.time.LocalDate;

public enum Period {
    WEEK, MONTH, YEAR;

    public LocalDate calculateStartDate(LocalDate endDate){
        return switch (this){
            case WEEK -> endDate.minusWeeks(1);
            case MONTH -> endDate.minusMonths(1);
            case YEAR -> endDate.minusYears(1);
        };
    }

    public static Period fromString(String period){
        try {
            return Period.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid period: " + period);
        }
    }
}
