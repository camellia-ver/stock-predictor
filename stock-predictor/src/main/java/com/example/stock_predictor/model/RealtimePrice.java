package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "realtime_price",
    indexes = {
        @Index(name = "idx_stock_recordedat", columnList = "stock_id, recordedAt")
    })
public class RealtimePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
}
