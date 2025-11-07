package com.bankingapp.Config;

import com.bankingapp.Model.User;
import com.bankingapp.Repository.UserRepository;
import com.bankingapp.Service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j // Optional: for logging
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "mohanrajbalaji17@gmail.com";

        // Check if the admin user already exists
        if (!userRepository.existsByEmail(adminEmail)) {

            // 1. Create the Admin User
            User adminUser = User.builder()
                    .name("Admin Manager")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@11#"))
                    .roles("ROLE_ADMIN")
                    .build();

            User savedAdmin = userRepository.save(adminUser);

            // 2. Create a default account for the admin
            // This reuses the logic to give them a 5000 balance and a transaction
            accountService.createDefaultAccount(savedAdmin);

            log.info("Admin user 'admin@bank.com' created successfully.");
        } else {
            log.info("Admin user 'admin@bank.com' already exists.");
        }
    }
}