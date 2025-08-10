package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "price_history",
        indexes = {
                @Index(name = "idx_stock_recordedat", columnList = "stock_id, recordedAt")
        }
)
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id",nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate recordedAt;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal open;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal low;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal close;

    @Column(nullable = false)
    private Long volume;
}
