package com.bankingapp.Service;

import com.bankingapp.Model.User;
import com.bankingapp.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    /**
     * This method is required by Spring Security's UserDetailsService.
     * It's called during the authentication process.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );
    }

    /**
     * This method handles new user registration.
     */
    @Override
    @Transactional
    public User registerUser(String name, String email, String rawPassword) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // 2. Create new user's account
        User newUser = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles("ROLE_USER") // Default role
                .build();

        // 3. Save the new user
        User savedUser = userRepository.save(newUser);

        // 4. Create a default bank account for the new user
        // This runs within the same transaction
        accountService.createDefaultAccount(savedUser);

        return savedUser;
    }
}
