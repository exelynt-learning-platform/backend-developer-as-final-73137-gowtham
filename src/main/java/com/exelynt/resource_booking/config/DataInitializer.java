package com.exelynt.resource_booking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.exelynt.resource_booking.entity.Role;
import com.exelynt.resource_booking.entity.User;
import com.exelynt.resource_booking.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // =========================
            // ADMIN USER
            // =========================

            if (!userRepository
                    .existsByUsername("admin")) {

                User admin = new User(
                        "admin",
                        passwordEncoder.encode(
                                "Admin@123"
                        ),
                        Role.ADMIN
                );

                userRepository.save(admin);
            }

            // =========================
            // NORMAL USER
            // =========================

            if (!userRepository
                    .existsByUsername("user")) {

                User user = new User(
                        "user",
                        passwordEncoder.encode(
                                "User@123"
                        ),
                        Role.USER
                );

                userRepository.save(user);
            }
        };
    }
}