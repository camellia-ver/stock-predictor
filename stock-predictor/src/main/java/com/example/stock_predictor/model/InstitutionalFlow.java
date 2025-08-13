package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institutional_flow", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private java.sql.Date date;

    private Long foreignBuy;
    private Long foreignSell;
    private Long institutionBuy;
    private Long institutionSell;
    private Long individualBuy;
    private Long individualSell;

    @Column(length = 20)
    private String source;
}
