package com.example.transactionstarter.transaction.exception;

import com.example.transactionstarter.transaction.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateTransactionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateTransaction(
            DuplicateTransactionException exception) {

        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidTransactionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTransaction(
            InvalidTransactionException exception) {

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTransactionNotFound(
            TransactionNotFoundException exception) {

        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(
            ConstraintViolationException exception) {

        String message = exception.getConstraintViolations()
                .stream()
                .map(violation ->
                        violation.getPropertyPath()
                                + ": "
                                + violation.getMessage()
                )
                .collect(Collectors.joining(", "));

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMessageNotReadable(
            HttpMessageNotReadableException exception) {

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Request contains invalid or unreadable data"
        );
    }
}