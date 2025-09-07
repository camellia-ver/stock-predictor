package com.example.stock_predictor.util;

import java.math.BigDecimal;

public class NumberParseUtils {

    public static BigDecimal parseBigDecimalOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return new BigDecimal(s.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseLongOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Long.parseLong(s.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
