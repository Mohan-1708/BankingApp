package com.bankingapp.Service;

import com.bankingapp.Model.User;
// Assuming you will create a DTO for registration
// If not, you can pass individual fields
// import com.bankingapp.dto.UserRegistrationDto;

public interface UserService {

    /**
     * Registers a new user in the system.
     *
     * @param name The user's full name.
     * @param email The user's email (will be used as username).
     * @param rawPassword The user's plaintext password.
     * @return The newly created and saved User entity.
     * @throws RuntimeException if the email is already in use.
     */
    User registerUser(String name, String email, String rawPassword);
}
