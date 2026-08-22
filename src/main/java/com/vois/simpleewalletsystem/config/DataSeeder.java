package com.vois.simpleewalletsystem.config;

import com.vois.simpleewalletsystem.dto.request.UserRequest;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.enums.Role;
import com.vois.simpleewalletsystem.repository.UserRepository;
import com.vois.simpleewalletsystem.service.UserService;
import com.vois.simpleewalletsystem.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {

        // Create ADMIN
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

            User admin = User.builder()
                    .fullName("Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("Admin123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();

            User savedAdmin = userRepository.save(admin);

            walletService.createWallet(savedAdmin);
        }

        // Create 50 normal USERS
        for (int i = 1; i <= 50; i++) {

            String email = "user" + i + "@gmail.com";
            String fullName = "User " + i;

            if (userRepository.findByEmail(email).isEmpty()) {

                UserRequest request = UserRequest.builder()
                        .fullName(fullName)
                        .email(email)
                        .password("User123")
                        .role(Role.USER)
                        .build();

                userService.createUser(request);
                System.out.println("Finished creating " + email);
            }
        }
        System.out.println("===== DATA SEEDER FINISHED =====");
    }
}