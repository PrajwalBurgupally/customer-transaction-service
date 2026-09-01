# Customer Transaction Service

A Spring Boot REST API for creating, retrieving, updating, and querying customer transactions.

This project was implemented as part of the Customer Transactions coding exercise.

## Table of Contents

- [Technology Stack](#technology-stack)
- [Implementation Assumptions](#implementation-assumptions)
- [Status Transition Rules](#status-transition-rules)
- [Validation Rules](#validation-rules)
- [API Endpoints](#api-endpoints)
- [Error Handling](#error-handling)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Running the Application](#running-the-application)
- [Manual API Testing](#manual-api-testing)
- [Design Decisions](#design-decisions)
- [Known Limitations](#known-limitations)
- [AI Usage Disclosure](#ai-usage-disclosure)
## Technology Stack

- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Jakarta Bean Validation
- JUnit 5
- Spring MockMvc
- Maven
## Implementation Assumptions

The transaction variants were not provided with the starter project, so the following assumptions were selected for this implementation.

### Supported Currency

Only `INR` is supported.

### Transaction Amount

- Minimum amount: ₹1.00
- Maximum amount: ₹100,000.00
- Maximum precision: 2 decimal places
### Transaction Types

- `PAYMENT`
- `REFUND`
### Initial Transaction Status

Every newly created transaction starts with `PENDING`. The client cannot choose the initial status.

### Status Transitions

Allowed transitions:

```text
PENDING → COMPLETED
PENDING → FAILED
```

## Status Transition Rules

`COMPLETED` and `FAILED` are treated as **terminal states**.

The following transitions are rejected:

- `COMPLETED → PENDING`
- `COMPLETED → FAILED`
- `FAILED → PENDING`
- `FAILED → COMPLETED`
### Reasoning

A newly created transaction starts in `PENDING` while processing is in progress. Once a transaction reaches `COMPLETED` or `FAILED`, its state is considered final. Preventing transitions away from terminal states avoids inconsistent transaction history.

## Validation Rules

The following fields are required when **creating** a transaction:

- Transaction ID
- Customer ID
- Amount
- Currency
- Transaction Type
  The `status` field is required when **updating** a transaction status.

### Additional Business Validation (service layer)

- Transaction IDs must be unique.
- Amount must be at least ₹1.00.
- Amount must not exceed ₹100,000.00.
- Amount must have at most two decimal places.
- Only `INR` is supported.
- Transaction type must be `PAYMENT` or `REFUND`.
- Newly created transactions always start with `PENDING`.
- Only valid status transitions are allowed.
## API Endpoints

### 1. Create Transaction

`POST /api/transactions`

**Example request:**

```json
{
  "transactionId": "TXN10001",
  "customerId": "CUST001",
  "amount": 1500.00,
  "currency": "INR",
  "transactionType": "PAYMENT"
}
```

**Successful response:**

```json
{
  "transactionId": "TXN10001",
  "customerId": "CUST001",
  "amount": 1500.00,
  "currency": "INR",
  "transactionType": "PAYMENT",
  "transactionStatus": "PENDING"
}
```

**HTTP status:** `201 Created`

### 2. Get Transaction

`GET /api/transactions/{transactionId}`

**Example:**

```
GET /api/transactions/TXN10001
```

**Successful response:** `200 OK`

If the transaction does not exist: `404 Not Found`

```json
{
  "status": 404,
  "message": "Transaction not found: TXN10001"
}
```

### 3. Update Transaction Status

`PATCH /api/transactions/{transactionId}/status`

**Example request:**

```json
{
  "status": "COMPLETED"
}
```

**Example:**

```
PATCH /api/transactions/TXN10001/status
```

**Successful response:** `200 OK`

```json
{
  "transactionId": "TXN10001",
  "customerId": "CUST001",
  "amount": 1500.00,
  "currency": "INR",
  "transactionType": "PAYMENT",
  "transactionStatus": "COMPLETED"
}
```

An invalid status transition returns `400 Bad Request`:

```json
{
  "status": 400,
  "message": "Invalid status transition from COMPLETED to PENDING"
}
```

### 4. Get Transactions for a Customer

`GET /api/transactions?customerId={customerId}`

**Example:**

```
GET /api/transactions?customerId=CUST001
```

If transactions exist, a list is returned. If the customer has no transactions, an empty list `[]` is returned with `200 OK`.

## Error Handling

Application errors are handled centrally using `@RestControllerAdvice`.

| Situation                          | HTTP Status |
|-------------------------------------|-------------|
| Successful creation                 | 201         |
| Successful retrieval/update         | 200         |
| Invalid request or business validation | 400      |
| Transaction not found                | 404        |
| Duplicate transaction ID             | 409        |

**Example error response:**

```json
{
  "status": 400,
  "message": "Transaction amount must not exceed 100000.00"
}
```

The application also handles request validation errors, blank query parameters, missing status values, and invalid enum values consistently as bad requests.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/transactionstarter/
│   │       ├── TransactionStarterApplication.java
│   │       └── transaction/
│   │           ├── controller/
│   │           │   └── TransactionController.java
│   │           ├── dto/
│   │           │   ├── CreateTransactionRequest.java
│   │           │   ├── ErrorResponse.java
│   │           │   ├── TransactionResponse.java
│   │           │   └── UpdateTransactionStatusRequest.java
│   │           ├── entity/
│   │           │   └── Transaction.java
│   │           ├── enums/
│   │           │   ├── Currency.java
│   │           │   ├── TransactionStatus.java
│   │           │   └── TransactionType.java
│   │           ├── exception/
│   │           │   ├── DuplicateTransactionException.java
│   │           │   ├── GlobalExceptionHandler.java
│   │           │   ├── InvalidTransactionException.java
│   │           │   └── TransactionNotFoundException.java
│   │           ├── repository/
│   │           │   └── TransactionRepository.java
│   │           └── service/
│   │               └── TransactionService.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/example/transactionstarter/
            ├── TransactionStarterApplicationTests.java
            └── controller/
                └── TransactionControllerIntegrationTest.java
```

## Testing

The project uses **JUnit 5** and **Spring MockMvc** for integration/API testing.

The test suite covers:

- Application context startup
- Successful transaction creation
- Duplicate transaction IDs
- Amount validation
- Minimum amount boundary
- Maximum amount boundary
- Required-field validation
- Invalid currency
- Getting a nonexistent transaction
- `PENDING → COMPLETED`
- `PENDING → FAILED`
- Invalid status transitions
- Missing status during status update
- Retrieving multiple transactions for a customer

    - Blank customer ID

Run the complete test suite with:

```powershell
.\mvnw.cmd clean test

```
.\mvnw.cmd clean test
```

**Expected result:**

```
Tests run: 16
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## Running the Application

From the project root:

```
.\mvnw.cmd spring-boot:run
```

The application runs on: `http://localhost:8080`

The H2 console is available at: `http://localhost:8080/h2-console`

The configured database is: `jdbc:h2:mem:transactions`

> The database is intentionally configured as an in-memory database for the exercise. Transaction data is lost when the application stops.

## Manual API Testing

### Create a transaction

```powershell
$body = @{
    transactionId = "TXN10001"
    customerId = "CUST001"
    amount = 1500.00
    currency = "INR"
    transactionType = "PAYMENT"
} | ConvertTo-Json
 
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/transactions" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Update transaction status

```powershell
$statusBody = @{
    status = "COMPLETED"
} | ConvertTo-Json
 
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/transactions/TXN10001/status" `
    -Method Patch `
    -ContentType "application/json" `
    -Body $statusBody
```

### Get a transaction

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/transactions/TXN10001" `
    -Method Get
```

### Get transactions for a customer

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/transactions?customerId=CUST001" `
    -Method Get
```

## Design Decisions

### BigDecimal for transaction amounts

`BigDecimal` is used for transaction amounts because monetary values should not rely on binary floating-point arithmetic.

### Enums

`Currency`, `TransactionType`, and `TransactionStatus` are represented as enums so that the domain model uses a controlled set of values.

### DTOs

Request and response DTOs separate the REST API contract from the JPA persistence entity.

### Service Layer

Business validation and status-transition rules are implemented in the service layer, while the controller remains focused on HTTP concerns.

### Centralized Exception Handling

`GlobalExceptionHandler` provides consistent HTTP responses for business, validation, and request-format errors.

### Database Constraints

The transaction entity defines non-null persistence constraints and explicit precision and scale for the transaction amount.

### H2

H2 was retained from the starter project because it provides a lightweight database suitable for the coding exercise and integration testing.

## Known Limitations

This implementation is intentionally scoped to the requirements of the coding exercise. It does not include:

- Authentication and authorization
- Pagination
- Sorting
- Database migrations
- External payment-provider integration
- Audit/history tables
- Production database configuration
## AI Usage Disclosure

AI assistance was used during development through ChatGPT.

### How AI was used

AI was used for:

- Guidance on Spring Boot project structure and implementation
- Writing and reviewing Java/Spring Boot code
- Designing integration tests using JUnit 5 and MockMvc
- Debugging compilation and runtime errors
- Git workflow guidance
- Reviewing validation and exception-handling design
- Drafting and reviewing project documentation

### Significant AI-generated or suggested work

AI suggested and helped draft several parts of the implementation, including:

- Transaction DTOs
- Transaction service logic
- REST controller endpoints
- Custom exceptions and centralized exception handling
- Integration tests
- Validation rules
- Status-transition logic
- README documentation

The generated code was reviewed and tested before being retained.

### What I changed, corrected, or rejected

I reviewed the generated suggestions against the actual project and changed them when necessary.

Examples include:

- Moving `DuplicateTransactionException` from the incorrect `entity` package into the `exception` package.
- Correcting `TransactionType.java` after an enum declaration/file-name mismatch caused a compilation error.
- Correcting the test assertions for the `status` field after changing the error response from a String status to an integer status.
- Fixing test-file structure and misplaced braces when compilation errors such as `reached end of file while parsing` occurred.
- Adding `ConstraintViolationException` handling after testing revealed that blank `customerId` requests were producing an unhandled validation exception.
- Reviewing the suggested status-transition rules and choosing `PENDING → COMPLETED` and `PENDING → FAILED`, while treating `COMPLETED` and `FAILED` as terminal states.

### What AI got wrong and how I fixed it

During development, some AI-generated suggestions were not correct for the actual project state.

A significant example was the customer ID validation test. The initial test expected a normal `400` response, but the actual application produced a `ConstraintViolationException`. I inspected the Maven test failure, identified that method-parameter validation was using `ConstraintViolationException`, and added an appropriate handler in `GlobalExceptionHandler`.

There were also several instances where generated test code was inserted with incorrect class braces, causing compilation errors. I replaced the affected test class with a complete corrected version and reran the full Maven test suite.

These issues were not accepted blindly; the implementation was corrected based on compiler output, runtime behavior, and test results.

### How the final result was verified

The final implementation was verified by:

- Running `.\mvnw.cmd clean test`
- Maintaining a suite of 16 automated tests
- Verifying successful and failure scenarios through MockMvc
- Manually testing the REST APIs using PowerShell
- Verifying duplicate transaction handling
- Verifying validation and error responses
- Verifying valid and invalid status transitions
- Cloning the final GitHub repository into a separate clean directory
- Running the complete test suite from the clean clone

Final verification result:

```text
Tests run: 16
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
