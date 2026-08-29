package com.example.transactionstarter.transaction.dto;

import com.example.transactionstarter.transaction.enums.Currency;
import com.example.transactionstarter.transaction.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateTransactionRequest {

    @NotBlank
    private String transactionId;

    @NotBlank
    private String customerId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotNull
    private TransactionType transactionType;

    public CreateTransactionRequest() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}