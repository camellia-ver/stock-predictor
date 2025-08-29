package com.example.stock_predictor.service;

import com.example.stock_predictor.dto.SignupDTO;
import com.example.stock_predictor.model.Role;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.repository.RoleRepository;
import com.example.stock_predictor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RoleRepository roleRepository;

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
    }

    public User signup(SignupDTO request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("기본 역할이 존재하지 않습니다."));

        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(defaultRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
}
