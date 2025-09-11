package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime stockDate;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Memo)) return false;

        Memo that = (Memo) o;

        return Objects.equals(user != null ? user.getId() : null,
                that.user != null ? that.user.getId() : null) &&
                Objects.equals(stock != null ? stock.getId() : null,
                        that.stock != null ? that.stock.getId() : null) &&
                Objects.equals(stockDate, that.stockDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user != null ? user.getId() : null,
                stock != null ? stock.getId() : null,
                stockDate);
    }
}
