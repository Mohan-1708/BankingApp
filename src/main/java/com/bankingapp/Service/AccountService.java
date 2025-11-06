package com.bankingapp.Service;

import com.bankingapp.Model.Account;
import com.bankingapp.Model.Transaction;
import com.bankingapp.Model.User;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    /**
     * Creates a new default (e.g., "Savings") account for a newly registered user.
     *
     * @param user The user for whom to create the account.
     * @return The newly created Account.
     */
    Account createDefaultAccount(User user);

    /**
     * Retrieves all accounts associated with a specific user ID.
     *
     * @param userId The ID of the user.
     * @return A list of the user's accounts.
     */
    List<Account> getAccountsByUserId(Long userId);

    /**
     * Retrieves the transaction history for a specific account ID,
     * ordered by the most recent transaction first.
     *
     * @param accountId The ID of the account.
     * @return A list of transactions.
     */
    List<Transaction> getTransactionHistory(Long accountId);

    /**
     * Transfers a specified amount from one account to another.
     * This method must be transactional.
     *
     * @param fromAccountNumber The account number to debit.
     * @param toAccountNumber   The account number to credit.
     * @param amount            The amount to transfer.
     * @param description       A description of the transaction.
     * @throws RuntimeException if the sender has insufficient funds,
     * or if either account does not exist.
     */
    void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description);

    /**
     * Finds an account by the currently logged-in user and account ID.
     * This is a security check to ensure a user can only access their own accounts.
     *
     * @param accountId The ID of the account.
     * @param userEmail The email of the currently authenticated user.
     * @return The Account if it exists and belongs to the user.
     * @throws RuntimeException if the account is not found or doesn't belong to the user.
     */
    Account getAccountByIdAndUserEmail(Long accountId, String userEmail);

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber The account number.
     * @return The Account.
     * @throws RuntimeException if the account is not found.
     */
    Account getAccountByAccountNumber(String accountNumber);


    // --- NEW ADMIN METHODS ---

    /**
     * Admin: Deposits a specified amount into an account.
     */
    void depositToAccount(String toAccountNumber, BigDecimal amount, String description);

    /**
     * Admin: Searches for accounts by account number or user email.
     */
    List<Account> searchAccounts(String query);

    /**
     * Admin: Retrieves a list of all accounts in the bank.
     */
    List<Account> findAllAccounts();

    /**
     * Admin: Gets the total count of all customers (ROLE_USER).
     */
    long getTotalCustomerCount();

    /**
     * Admin: Gets the total count of all accounts.
     */
    long getTotalAccountCount();

    /**
     * Admin: Gets the sum of all money in all accounts.
     */
    BigDecimal getTotalBankBalance();
}