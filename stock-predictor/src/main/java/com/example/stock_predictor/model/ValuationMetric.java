package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "valuation_metrics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_id", "date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ValuationMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDate date;

    private BigDecimal per;
    private BigDecimal pbr;
    private BigDecimal roe;
    private BigDecimal eps;
    private BigDecimal bps;
    private BigDecimal dividendYield;

    @Column(length = 20)
    private String source;
}
