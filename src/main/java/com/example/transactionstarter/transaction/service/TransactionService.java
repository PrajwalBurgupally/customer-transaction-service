package com.example.transactionstarter.transaction.service;

import com.example.transactionstarter.transaction.dto.CreateTransactionRequest;
import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.enums.Currency;
import com.example.transactionstarter.transaction.enums.TransactionStatus;
import com.example.transactionstarter.transaction.exception.DuplicateTransactionException;
import com.example.transactionstarter.transaction.exception.InvalidTransactionException;
import com.example.transactionstarter.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private static final BigDecimal MIN_AMOUNT = BigDecimal.ONE;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {

        if (transactionRepository.existsById(request.getTransactionId())) {
            throw new DuplicateTransactionException(
                    "Transaction ID already exists: " + request.getTransactionId()
            );
        }

        BigDecimal amount = request.getAmount();

        if (amount == null) {
            throw new InvalidTransactionException(
                    "Transaction amount is required"
            );
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidTransactionException(
                    "Transaction amount must be at least 1.00"
            );
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidTransactionException(
                    "Transaction amount must not exceed 100000.00"
            );
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidTransactionException(
                    "Transaction amount must have at most 2 decimal places"
            );
        }

        if (request.getCurrency() == null) {
            throw new InvalidTransactionException(
                    "Currency is required"
            );
        }

        if (request.getCurrency() != Currency.INR) {
            throw new InvalidTransactionException(
                    "Only INR transactions are supported"
            );
        }

        if (request.getTransactionType() == null) {
            throw new InvalidTransactionException(
                    "Transaction type is required"
            );
        }

        Transaction transaction = new Transaction();

        transaction.setTransactionId(request.getTransactionId());
        transaction.setCustomerId(request.getCustomerId());
        transaction.setAmount(amount);
        transaction.setCurrency(request.getCurrency());
        transaction.setTransactionType(request.getTransactionType());

        // New transactions always start in PENDING status.
        transaction.setTransactionStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transaction);
    }
}