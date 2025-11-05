package com.bankingapp.service;

import com.bankingapp.Model.Account;
import com.bankingapp.Model.Transaction;
import com.bankingapp.Model.TransactionType;
import com.bankingapp.Model.User;
import com.bankingapp.Repository.AccountRepository;
import com.bankingapp.Repository.TransactionRepository;
import com.bankingapp.Service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Account createDefaultAccount(User user) {
        // Build the new account with a 5000 starting balance
        Account newAccount = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType("SAVINGS")
                .balance(new BigDecimal("5000.00")) // <-- The requested change
                .user(user)
                .transactions(new ArrayList<>()) // Initialize the list for the new transaction
                .build();

        // Create an initial "CREDIT" transaction for the starting balance
        Transaction initialDeposit = Transaction.builder()
                .amount(new BigDecimal("5000.00"))
                .transactionType(TransactionType.CREDIT)
                .description("Initial deposit")
                .account(newAccount)
                // @CreationTimestamp will handle the timestamp
                .build();

        // Add the transaction to the account's list
        // This will be saved by cascade (CascadeType.ALL in Account.java)
        newAccount.getTransactions().add(initialDeposit);

        // Save the account and its initial transaction
        return accountRepository.save(newAccount);
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByIdAndUserEmail(Long accountId, String userEmail) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new RuntimeException("Account not found or access denied."));
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account with number " + accountNumber + " not found."));
    }

    @Override
    @Transactional // This annotation is critical for all or nothing
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

        // 5. Save both accounts (and new transactions via cascade)
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    private String generateAccountNumber() {
        // Simple 10-digit random number string
        long number = (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(number);
    }




    @Override
    @Transactional
    public void depositToAccount(String toAccountNumber, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive.");
        }

        Account account = getAccountByAccountNumber(toAccountNumber);

        // 1. Add balance
        account.setBalance(account.getBalance().add(amount));

        // 2. Create transaction
        Transaction creditTx = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .description("Admin Deposit: " + description)
                .account(account)
                .build();

        // 3. Save changes
        transactionRepository.save(creditTx);
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> searchAccounts(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        // Try searching by account number first
        Optional<Account> accountByNum = accountRepository.findByAccountNumber(query);
        if (accountByNum.isPresent()) {
            return List.of(accountByNum.get());
        }

        // If not found, try searching by user email
        // We need a new method in AccountRepository for this
        return accountRepository.findByUserEmail(query);
    }



}

