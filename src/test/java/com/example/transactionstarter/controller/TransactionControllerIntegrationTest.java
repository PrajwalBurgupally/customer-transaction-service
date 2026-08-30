package com.example.transactionstarter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTransactionSuccessfully() throws Exception {

        String requestBody = """
                {
                    "transactionId": "TXN-TEST-001",
                    "customerId": "CUST-TEST-001",
                    "amount": 1500.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-TEST-001"))
                .andExpect(jsonPath("$.customerId").value("CUST-TEST-001"))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.transactionType").value("PAYMENT"))
                .andExpect(jsonPath("$.transactionStatus").value("PENDING"));
    }

    @Test
    void rejectTransactionWhenAmountExceedsMaximum() throws Exception {

        String requestBody = """
                {
                    "transactionId": "TXN-TEST-002",
                    "customerId": "CUST-TEST-001",
                    "amount": 100001.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction amount must not exceed 100000.00"));
    }

    @Test
    void rejectDuplicateTransactionId() throws Exception {

        String requestBody = """
                {
                    "transactionId": "TXN-TEST-003",
                    "customerId": "CUST-TEST-001",
                    "amount": 1500.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        // First request creates the transaction.
        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        // Second request with the same transaction ID must be rejected.
        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction ID already exists: TXN-TEST-003"));
    }

    @Test
    void getTransactionThatDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/transactions/TXN-DOES-NOT-EXIST")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction not found: TXN-DOES-NOT-EXIST"));
    }
}