package com.bankingapp.Repository;

import com.bankingapp.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions for a specific account, ordered by the timestamp
     * in descending order (newest transactions first).
     *
     * @param accountId The ID of the account to fetch transactions for.
     * @return A List of Transactions, sorted by date.
     */
    List<Transaction> findByAccountIdOrderByTimestampDesc(Long accountId);
}