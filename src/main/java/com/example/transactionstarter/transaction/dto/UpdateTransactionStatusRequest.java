package com.example.transactionstarter.transaction.dto;

import com.example.transactionstarter.transaction.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTransactionStatusRequest {

    @NotNull
    private TransactionStatus status;

    public UpdateTransactionStatusRequest() {
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}