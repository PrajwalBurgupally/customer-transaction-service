package com.example.transactionstarter.transaction.service;

import com.example.transactionstarter.transaction.dto.CreateTransactionRequest;
import com.example.transactionstarter.transaction.dto.UpdateTransactionStatusRequest;
import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.enums.Currency;
import com.example.transactionstarter.transaction.enums.TransactionStatus;
import com.example.transactionstarter.transaction.exception.DuplicateTransactionException;
import com.example.transactionstarter.transaction.exception.InvalidTransactionException;
import com.example.transactionstarter.transaction.exception.TransactionNotFoundException;
import com.example.transactionstarter.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private static final BigDecimal MIN_AMOUNT = BigDecimal.ONE;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {

        checkForDuplicateTransactionId(request.getTransactionId());
        validateAmount(request.getAmount());
        validateCurrency(request.getCurrency());
        validateTransactionType(request);

        Transaction transaction = new Transaction();

        transaction.setTransactionId(request.getTransactionId());
        transaction.setCustomerId(request.getCustomerId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setTransactionStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transaction);
    }

    public Transaction getTransaction(String transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found: " + transactionId
                        )
                );
    }

    public Transaction updateTransactionStatus(
            String transactionId,
            UpdateTransactionStatusRequest request) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found: " + transactionId
                        )
                );

        TransactionStatus currentStatus = transaction.getTransactionStatus();
        TransactionStatus requestedStatus = request.getStatus();

        if (!isValidStatusTransition(currentStatus, requestedStatus)) {
            throw new InvalidTransactionException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }

        transaction.setTransactionStatus(requestedStatus);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getCustomerTransactions(String customerId) {

        return transactionRepository.findByCustomerId(customerId);
    }

    private void checkForDuplicateTransactionId(String transactionId) {

        if (transactionRepository.existsById(transactionId)) {
            throw new DuplicateTransactionException(
                    "Transaction ID already exists: " + transactionId
            );
        }
    }

    private void validateAmount(BigDecimal amount) {

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
    }

    private void validateCurrency(Currency currency) {

        if (currency == null) {
            throw new InvalidTransactionException(
                    "Currency is required"
            );
        }

        if (currency != Currency.INR) {
            throw new InvalidTransactionException(
                    "Only INR transactions are supported"
            );
        }
    }

    private void validateTransactionType(CreateTransactionRequest request) {

        if (request.getTransactionType() == null) {
            throw new InvalidTransactionException(
                    "Transaction type is required"
            );
        }
    }

    private boolean isValidStatusTransition(
            TransactionStatus currentStatus,
            TransactionStatus requestedStatus) {

        return currentStatus == TransactionStatus.PENDING
                && (requestedStatus == TransactionStatus.COMPLETED
                || requestedStatus == TransactionStatus.FAILED);
    }
}