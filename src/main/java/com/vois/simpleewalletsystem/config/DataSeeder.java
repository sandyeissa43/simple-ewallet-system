package com.vois.simpleewalletsystem.config;

import com.vois.simpleewalletsystem.dto.request.UserRequest;
import com.vois.simpleewalletsystem.enums.Role;
import com.vois.simpleewalletsystem.repository.UserRepository;
import com.vois.simpleewalletsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(String @NonNull ... args) {


        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

            UserRequest adminRequest = UserRequest.builder()
                    .fullName("System Admin")
                    .email("admin@gmail.com")
                    .password("Admin@123")
                    .role(Role.ADMIN)
                    .build();

            userService.createUser(adminRequest);
        }


        for (int i = 1; i <= 99; i++) {

            String email = String.format("user%03d@ewallet.local", i);
            String fullName = String.format("User %03d", i);

            if (userRepository.findByEmail(email).isEmpty()) {

                UserRequest request = UserRequest.builder()
                        .fullName(fullName)
                        .email(email)
                        .password("User@123")
                        .role(Role.USER)
                        .build();

                userService.createUser(request);
            }
        }
    }
}