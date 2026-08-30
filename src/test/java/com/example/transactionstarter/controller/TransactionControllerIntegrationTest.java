package com.example.transactionstarter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                .andExpect(jsonPath("$.status").value(400))
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

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Transaction ID already exists: TXN-TEST-003"));
    }

    @Test
    void getTransactionThatDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/transactions/TXN-DOES-NOT-EXIST")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Transaction not found: TXN-DOES-NOT-EXIST"));
    }

    @Test
    void updateTransactionStatusFromPendingToCompleted() throws Exception {

        String createRequest = """
                {
                    "transactionId": "TXN-TEST-004",
                    "customerId": "CUST-TEST-002",
                    "amount": 2500.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated());

        String statusRequest = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(
                        patch("/api/transactions/TXN-TEST-004/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(statusRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-TEST-004"))
                .andExpect(jsonPath("$.transactionStatus").value("COMPLETED"));
    }

    @Test
    void rejectInvalidStatusTransition() throws Exception {

        String createRequest = """
                {
                    "transactionId": "TXN-TEST-005",
                    "customerId": "CUST-TEST-002",
                    "amount": 2500.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated());

        String completeRequest = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(
                        patch("/api/transactions/TXN-TEST-005/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(completeRequest)
                )
                .andExpect(status().isOk());

        String invalidStatusRequest = """
                {
                    "status": "PENDING"
                }
                """;

        mockMvc.perform(
                        patch("/api/transactions/TXN-TEST-005/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidStatusRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid status transition from COMPLETED to PENDING"));
    }

    @Test
    void getTransactionsForCustomer() throws Exception {

        String firstTransaction = """
                {
                    "transactionId": "TXN-TEST-006",
                    "customerId": "CUST-TEST-003",
                    "amount": 1000.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        String secondTransaction = """
                {
                    "transactionId": "TXN-TEST-007",
                    "customerId": "CUST-TEST-003",
                    "amount": 500.00,
                    "currency": "INR",
                    "transactionType": "REFUND"
                }
                """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstTransaction)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondTransaction)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/transactions")
                                .param("customerId", "CUST-TEST-003")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("CUST-TEST-003"))
                .andExpect(jsonPath("$[1].customerId").value("CUST-TEST-003"));
    }

    @Test
    void rejectTransactionWhenTransactionIdIsBlank() throws Exception {

        String requestBody = """
                {
                    "transactionId": "",
                    "customerId": "CUST-TEST-010",
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("transactionId: must not be blank"));
    }

    @Test
    void rejectTransactionWhenCurrencyIsInvalid() throws Exception {

        String requestBody = """
                {
                    "transactionId": "TXN-TEST-011",
                    "customerId": "CUST-TEST-010",
                    "amount": 1500.00,
                    "currency": "USD",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Request contains invalid or unreadable data"));
    }
}