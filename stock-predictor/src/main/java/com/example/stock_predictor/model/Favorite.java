package com.example.stock_predictor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "stock_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public int hashCode() {
        return Objects.hash(user != null ? user.getId() : null,
                stock != null ? stock.getId() : null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Favorite)) return false;

        Favorite that = (Favorite) obj;

        return Objects.equals(user != null ? user.getId() : null,
                that.user != null ? that.user.getId() : null) &&
                Objects.equals(stock != null ? stock.getId() : null,
                        that.stock != null ? that.stock.getId() : null);
    }
}
