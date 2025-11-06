package com.bankingapp.Service;

import com.bankingapp.Model.Account;
import com.bankingapp.Model.Transaction;
import com.bankingapp.Model.TransactionType;
import com.bankingapp.Model.User;
import com.bankingapp.Repository.AccountRepository;
import com.bankingapp.Repository.TransactionRepository;
import com.bankingapp.Repository.UserRepository; // <-- This is required
import com.bankingapp.Service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository; // <-- This field is required

    /**
     * Creates a default savings account for a new user with a 5000 balance.
     */
    @Override
    @Transactional
    public Account createDefaultAccount(User user) {
        // Build the new account with a 5000 starting balance
        Account newAccount = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType("SAVINGS")
                .balance(new BigDecimal("5000.00"))
                .user(user)
                .transactions(new ArrayList<>()) // Initialize the list
                .build();

        // Create an initial "CREDIT" transaction for the starting balance
        Transaction initialDeposit = Transaction.builder()
                .amount(new BigDecimal("5000.00"))
                .transactionType(TransactionType.CREDIT)
                .description("Initial deposit")
                .account(newAccount)
                .build();

        newAccount.getTransactions().add(initialDeposit);

        // Save the account and its initial transaction
        return accountRepository.save(newAccount);
    }

    /**
     * Gets all accounts for a specific user ID.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    /**
     * Gets transaction history for a specific account.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId);
    }

    /**
     * Security check: Gets an account only if it belongs to the specified user.
     */
    @Override
    @Transactional(readOnly = true)
    public Account getAccountByIdAndUserEmail(Long accountId, String userEmail) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new RuntimeException("Account not found or access denied."));
    }

    /**
     * Finds an account by its number. Throws an error if not found.
     */
    @Override
    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account with number " + accountNumber + " not found."));
    }

    /**
     * Performs a fund transfer between two accounts.
     */
    @Override
    @Transactional
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        // 1. Find both accounts
        Account fromAccount = getAccountByAccountNumber(fromAccountNumber);
        Account toAccount = getAccountByAccountNumber(toAccountNumber);

        // 2. Check for sufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds.");
        }

        // 3. Perform debit from sender
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        Transaction debitTx = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.DEBIT)
                .description("To: " + toAccount.getAccountNumber() + " - " + description)
                .account(fromAccount)
                .build();
        fromAccount.getTransactions().add(debitTx);

        // 4. Perform credit to receiver
        toAccount.setBalance(toAccount.getBalance().add(amount));
        Transaction creditTx = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .description("From: ".concat(fromAccount.getAccountNumber()).concat(" - ").concat(description))
                .account(toAccount)
                .build();
        toAccount.getTransactions().add(creditTx);

        // 5. Save both accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    /**
     * Admin: Deposits money into an account.
     */
    @Override
    @Transactional
    public void depositToAccount(String toAccountNumber, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive.");
        }

        Account account = getAccountByAccountNumber(toAccountNumber);

        // 1. Add balance
        account.setBalance(account.getBalance().add(amount));

        // 2. Create transaction
        String txDescription = (description == null || description.isBlank())
                ? "Admin Deposit"
                : "Admin Deposit: " + description;

        Transaction creditTx = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .description(txDescription)
                .account(account)
                .build();

        // 3. Save changes
        transactionRepository.save(creditTx);
        accountRepository.save(account);
    }

    /**
     * Admin: Searches for accounts by email or account number.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Account> searchAccounts(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        List<Account> accounts = accountRepository.findByUserEmail(query);

        if (accounts.isEmpty()) {
            Optional<Account> accountByNum = accountRepository.findByAccountNumber(query);
            if (accountByNum.isPresent()) {
                accounts = List.of(accountByNum.get());
            }
        }

        // Eagerly fetch user data to avoid LazyInitializationException
        accounts.forEach(account -> account.getUser().getName());
        return accounts;
    }

    /**
     * Admin: Gets all accounts.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Account> findAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        // Eagerly fetch user data to avoid LazyInitializationException
        accounts.forEach(account -> account.getUser().getName());
        return accounts;
    }

    /**
     * Admin: Gets total customer count (ROLE_USER only).
     */
    @Override
    @Transactional(readOnly = true)
    public long getTotalCustomerCount() {
        return userRepository.countByRoles("ROLE_USER");
    }

    /**
     * Admin: Gets total count of all accounts.
     */
    @Override
    @Transactional(readOnly = true)
    public long getTotalAccountCount() {
        return accountRepository.count();
    }



    /**
     * Admin: Gets the sum of all money in all accounts.
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalBankBalance() {
        BigDecimal total = accountRepository.getTotalBankBalance();
        return (total == null) ? BigDecimal.ZERO : total;
    }

    // --- PRIVATE HELPER METHOD ---

    private String generateAccountNumber() {
        // Simple 10-digit random number string
        long number = (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(number);
    }
}