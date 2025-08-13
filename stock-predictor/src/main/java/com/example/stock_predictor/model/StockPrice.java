package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_prices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private java.sql.Date date;

    private Double openPrice;
    private Double closePrice;
    private Double highPrice;
    private Double lowPrice;

    private Long volume;

    @Column(length = 20)
    private String source;
}

