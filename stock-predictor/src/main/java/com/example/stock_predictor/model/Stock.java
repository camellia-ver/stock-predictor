package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length = 20)
    private String ticker;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false,length = 50)
    private String market;

    public Stock(Long id, String ticker, String name, String market) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
        this.market = market;
    }
}
