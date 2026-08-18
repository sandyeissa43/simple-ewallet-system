package com.vois.simpleewalletsystem.config;

import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.enums.Role;
import com.vois.simpleewalletsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {

        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

            User admin = User.builder()
                    .fullName("System Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);
        }

        if (userRepository.findByEmail("user@gmail.com").isEmpty()) {

            User user = User.builder()
                    .fullName("Test User")
                    .email("user@gmail.com")
                    .password(passwordEncoder.encode("User@123"))
                    .role(Role.USER)
                    .active(true)
                    .build();

            userRepository.save(user);
        }
    }
}