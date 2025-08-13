package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "valuation_metrics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValuationMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long valuationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private java.sql.Date date;

    private Double per;
    private Double pbr;
    private Double roe;
    private Double eps;
    private Double bps;
    private Double dividendYield;

    @Column(length = 20)
    private String source;
}
