package com.example.transactionstarter.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateTransactionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateTransaction(
            DuplicateTransactionException exception) {

        return Map.of(
                "status", "409",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidTransactionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidTransaction(
            InvalidTransactionException exception) {

        return Map.of(
                "status", "400",
                "message", exception.getMessage()
        );
    }
}