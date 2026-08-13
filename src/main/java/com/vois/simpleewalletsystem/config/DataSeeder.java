package com.vois.simpleewalletsystem.config;

import com.vois.simpleewalletsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed (UserRepository userRepository){
        return args -> {

        };
    }
}
