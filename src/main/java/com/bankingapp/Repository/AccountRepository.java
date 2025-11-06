package com.bankingapp.Repository;

import com.bankingapp.Model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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


    /**
     * Finds all accounts associated with a user's email.
     * (Used by Admin search)
     *
     * @param email The user's email.
     * @return A List of Accounts belonging to that user.
     */
    List<Account> findByUserEmail(String email);

    /**
     * Calculates the sum of all balances in all accounts.
     * (Used by Admin dashboard stats)
     *
     * @return The total balance of the bank.
     */
    @Query("SELECT SUM(a.balance) FROM Account a")
    BigDecimal getTotalBankBalance();
}