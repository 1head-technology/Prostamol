# Prostamol

Personal money management REST API built with Spring Boot and Clean Architecture principles.

Manage accounts, track income and expenses, handle transfers between accounts, set budgets with spending limits per category, and track savings goals with contributions.

## Tech Stack

- **Java 25** / **Spring Boot 4.0.5**
- **Spring Data JPA** with **PostgreSQL**
- **Jakarta Bean Validation**
- **Maven** wrapper included

## Prerequisites

- Java 25+
- PostgreSQL running on `localhost:5432` with a database named `prostamol`

## Getting Started

### Database setup

Create the PostgreSQL database:

```sql
CREATE DATABASE prostamol;
```

### Configuration

Create a `.env` file in the project root (auto-imported by Spring):

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=prostamol
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

Hibernate manages the schema automatically (`ddl-auto=update`).

### Run

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### Build

```bash
./mvnw clean package
```

### Tests

```bash
./mvnw test
```

## API Reference

All endpoints are under `/api/v1`. Request and response bodies use JSON.

### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users` | Create a user |
| `GET` | `/api/v1/users/{id}` | Get user by ID |

**Create user:**

```json
POST /api/v1/users

{
  "email": "john@example.com",
  "name": "John",
  "defaultCurrency": "EUR"
}
```

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users/{userId}/accounts` | Create an account |
| `GET` | `/api/v1/users/{userId}/accounts` | List user accounts |
| `GET` | `/api/v1/accounts/{accountId}/balance` | Get computed balance |

Account types: `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `CASH`, `INVESTMENT`, `BANK_ACCOUNT`

**Create account:**

```json
POST /api/v1/users/{userId}/accounts

{
  "name": "Main Checking",
  "type": "CHECKING",
  "initialBalance": 1000.00,
  "currency": "EUR"
}
```

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users/{userId}/transactions` | Record income or expense |
| `POST` | `/api/v1/transfers` | Record a transfer between accounts |
| `GET` | `/api/v1/accounts/{accountId}/transactions?from=&to=` | List by account (optional date range) |
| `GET` | `/api/v1/users/{userId}/transactions` | List all user transactions |

Transaction types: `INCOME`, `EXPENSE`, `TRANSFER_OUT`, `TRANSFER_IN`

Balance calculation: `initialBalance + SUM(INCOME + TRANSFER_IN) - SUM(EXPENSE + TRANSFER_OUT)`

**Record a transaction:**

```json
POST /api/v1/users/{userId}/transactions

{
  "accountId": "...",
  "type": "EXPENSE",
  "amount": 45.50,
  "currency": "EUR",
  "date": "2026-04-15",
  "description": "Groceries",
  "categoryId": "...",
  "recurring": false,
  "recurrenceFrequency": null
}
```

Recurring transactions set `recurring: true` with a `recurrenceFrequency` of `DAILY`, `WEEKLY`, `MONTHLY`, or `YEARLY`.

**Record a transfer:**

```json
POST /api/v1/transfers

{
  "userId": "...",
  "sourceAccountId": "...",
  "destinationAccountId": "...",
  "amount": 200.00,
  "currency": "EUR",
  "date": "2026-04-15",
  "description": "Move to savings"
}
```

A transfer creates two linked transactions (`TRANSFER_OUT` and `TRANSFER_IN`) referencing each other via `linkedTransactionId`.

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users/{userId}/categories` | Create a category |
| `GET` | `/api/v1/users/{userId}/categories` | List categories (system + user) |

Category types: `INCOME`, `EXPENSE`

System categories (`system: true`) are shared across all users. User-created categories are scoped to the user.

**Create category:**

```json
POST /api/v1/users/{userId}/categories

{
  "name": "Groceries",
  "type": "EXPENSE"
}
```

### Budgets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users/{userId}/budgets` | Create a budget |
| `GET` | `/api/v1/users/{userId}/budgets` | List budgets |
| `GET` | `/api/v1/budgets/{budgetId}/summary` | Budget summary (planned vs. spent per category) |

**Create budget:**

```json
POST /api/v1/users/{userId}/budgets

{
  "name": "April 2026",
  "from": "2026-04-01",
  "to": "2026-04-30",
  "lines": [
    {
      "categoryId": "...",
      "plannedAmount": 300.00,
      "currency": "EUR"
    }
  ]
}
```

The summary endpoint returns planned vs. actual spending and the remaining amount for each budget line.

### Savings Goals

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users/{userId}/savings-goals` | Create a savings goal |
| `GET` | `/api/v1/users/{userId}/savings-goals` | List savings goals |
| `POST` | `/api/v1/savings-goals/{goalId}/contributions` | Add contribution |

Statuses: `ACTIVE`, `ACHIEVED`, `CANCELLED`. A goal transitions to `ACHIEVED` automatically when the current amount reaches the target.

**Create savings goal:**

```json
POST /api/v1/users/{userId}/savings-goals

{
  "name": "Vacation Fund",
  "targetAmount": 2000.00,
  "currency": "EUR",
  "deadline": "2026-12-31"
}
```

**Add contribution:**

```json
POST /api/v1/savings-goals/{goalId}/contributions

{
  "amount": 150.00,
  "currency": "EUR"
}
```

### Error Responses

All errors return a consistent format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Description of the problem",
  "timestamp": "2026-04-15T10:30:00Z"
}
```

## Architecture

The project follows **Hexagonal (Clean) Architecture** with three layers:

```
domain/              Pure Java -- no framework dependencies
  model/             Aggregates, value objects, enums
  port/in/           Use-case interfaces (input ports)
  port/out/          Repository interfaces (output ports)

application/
  usecase/           Use-case implementations -- plain Java, wired via BeanConfig

infrastructure/
  persistence/       JPA entities, Spring Data repositories, adapters, mappers
  web/               Controllers, DTOs, web mappers, exception handler
  config/            BeanConfig (Spring bean wiring)
```

- The **domain** layer has zero dependencies on Spring or JPA.
- Application services are plain Java classes with no `@Service` annotation -- they are instantiated as beans in `BeanConfig`.
- Infrastructure adapters implement the output ports, keeping the domain decoupled from persistence details.
