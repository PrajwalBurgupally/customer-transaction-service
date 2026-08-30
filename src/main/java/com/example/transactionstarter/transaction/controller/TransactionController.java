package com.example.transactionstarter.transaction.controller;

import com.example.transactionstarter.transaction.dto.CreateTransactionRequest;
import com.example.transactionstarter.transaction.dto.TransactionResponse;
import com.example.transactionstarter.transaction.dto.UpdateTransactionStatusRequest;
import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = transactionService.createTransaction(request);

        return toResponse(transaction);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse getTransaction(
            @PathVariable String transactionId) {

        Transaction transaction = transactionService.getTransaction(transactionId);

        return toResponse(transaction);
    }

    @PatchMapping("/{transactionId}/status")
    public TransactionResponse updateTransactionStatus(
            @PathVariable String transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {

        Transaction transaction = transactionService.updateTransactionStatus(
                transactionId,
                request
        );

        return toResponse(transaction);
    }

    @GetMapping
    public List<TransactionResponse> getCustomerTransactions(
            @RequestParam @NotBlank String customerId) {

        return transactionService.getCustomerTransactions(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus()
        );
    }
}