package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_index_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StockIndexPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String indexName;

    @Column(nullable = false)
    private LocalDate date;

    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;

    private Long volume;
    // 거래대금
    private Long value;
    // 시가상장총액
    private Long marketCap;
}
