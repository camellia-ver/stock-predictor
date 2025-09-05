package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "predictions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDate predictionDate;
    private LocalDate targetDate;

    @Column(length = 50)
    private String modelName;

    private BigDecimal upProb;
    private BigDecimal downProb;

    private LocalDateTime createdAt;

    @Override
    public int hashCode() {
        return Objects.hash(stock.getId(), targetDate, modelName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof Prediction)) return false;

        Prediction that = (Prediction) obj;

        return stock.getId().equals(that.stock.getId()) &&
                targetDate.equals(that.targetDate) &&
                modelName.equals(that.modelName);
    }
}
