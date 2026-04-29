package com.campus.campus_system.config;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class AdminBootstrap {
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";

    @Bean
    public ApplicationRunner adminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).orElseGet(User::new);
            if (admin.getId() == null) {
                admin.setUsername(DEFAULT_ADMIN_USERNAME);
                admin.setCreateTime(LocalDateTime.now());
            }

            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setRealName("System Admin");
            admin.setCollege("Administration");
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            userRepository.save(admin);
        };
    }
}
