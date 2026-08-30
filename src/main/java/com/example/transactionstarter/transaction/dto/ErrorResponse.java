package com.example.transactionstarter.transaction.dto;

public record ErrorResponse(
        int status,
        String message
) {
}