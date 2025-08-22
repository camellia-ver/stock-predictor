package com.example.stock_predictor.initializer;

import com.example.stock_predictor.model.Role;
import com.example.stock_predictor.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // USER 역할 초기화
        if (roleRepository.findByName("USER").isEmpty()){
            Role userRole = Role.builder().name("USER").build();
            roleRepository.save(userRole);
        }

        // ADMIM 역할 초기화
        if (roleRepository.findByName("ADMIN").isEmpty()){
            Role adminRole = Role.builder().name("ADMIN").build();
            roleRepository.save(adminRole);
        }

        System.out.println("초기 역할 데이터 삽입 완료!");
    }
}
