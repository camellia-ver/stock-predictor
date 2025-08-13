package com.example.stock_predictor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank
    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @NotBlank
    @Getter(AccessLevel.NONE)
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Email
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @Builder
    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public void addFavorite(Favorite favorite){
        favorites.add(favorite);
        favorite.setUser(this);
    }

    public void removeFavorite(Favorite favorite) {
        favorites.remove(favorite);
        favorite.setUser(null);
    }

    public void addNote(Note note) {
        notes.add(note);
        note.setUser(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setUser(null);
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
