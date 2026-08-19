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

        // Create Admin
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

        // Create 99 normal users
        for (int i = 1; i <= 99; i++) {

            String email = String.format("user%03d@ewallet.local", i);
            String fullName = String.format("User %03d", i);

            if (userRepository.findByEmail(email).isEmpty()) {

                User user = User.builder()
                        .fullName(fullName)
                        .email(email)
                        .password(passwordEncoder.encode("User@123"))
                        .role(Role.USER)
                        .active(true)
                        .build();

                userRepository.save(user);
            }
        }
    }
}