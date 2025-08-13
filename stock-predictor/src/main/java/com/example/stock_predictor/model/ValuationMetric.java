package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

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
    private Long valuationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDate date;

    private Double per;
    private Double pbr;
    private Double roe;
    private Double eps;
    private Double bps;
    private Double dividendYield;

    @Column(length = 20)
    private String source;
}
