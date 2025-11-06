package com.bankingapp.Repository;



import com.bankingapp.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * This is used by Spring Security's UserDetailsService.
     *
     * @param email The email address to search for.
     * @return An Optional containing the User if found, or empty if not.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email.
     * Useful for registration to prevent duplicate emails.
     *
     * @param email The email address to check.
     * @return true if an email exists, false otherwise.
     */
    Boolean existsByEmail(String email);
    long countByRoles(String role);
}

