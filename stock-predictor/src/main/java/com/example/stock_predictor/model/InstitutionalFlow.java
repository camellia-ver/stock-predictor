package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "institutional_flow", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_id", "date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InstitutionalFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDate date;

    private Long foreignBuy;
    private Long foreignSell;
    private Long foreignNetBuy; // 외국인 순매수

    private Long institutionBuy;
    private Long institutionSell;
    private Long institutionNetBuy; // 기관 순매수

    private Long individualBuy;
    private Long individualSell;
    private Long individualNetBuy; // 개인 순매순
}
