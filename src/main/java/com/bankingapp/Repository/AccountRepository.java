package com.bankingapp.Repository;

import com.bankingapp.Model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by its unique account number.
     * This is essential for the fund transfer feature.
     *
     * @param accountNumber The account number to search for.
     * @return An Optional containing the Account if found, or empty if not.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Finds all accounts belonging to a specific user, identified by the user's ID.
     * This is useful for the dashboard to display all of a user's accounts.
     *
     * @param userId The ID of the user.
     * @return A List of Accounts belonging to the user.
     */
    List<Account> findByUserId(Long userId);


    List<Account> findByUserEmail(String email);
}