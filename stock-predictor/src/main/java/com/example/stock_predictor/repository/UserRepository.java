package com.example.stock_predictor.repository;

import com.example.stock_predictor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
}
