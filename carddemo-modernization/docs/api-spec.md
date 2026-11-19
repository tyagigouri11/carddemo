# CardDemo REST API Specification

## Base URL

```
http://localhost:8080/api
```

## Authentication

Phase 1: No authentication required. Phase 2 will add OAuth2/JWT security.

## Content Type

All endpoints accept and return `application/json`

---

## Accounts

### GET /accounts/{id}

Retrieve account details by ID.

**Source**: CBACT01C (Account balance tracking)

**Path Parameters**:
- `id` (Long): Account ID

**Response** (200 OK):
```json
{
  "id": 1,
  "creditLimit": 50000.00,
  "currentBalance": 25000.00,
  "activeStatus": true,
  "reissueDate": "2026-06-01",
  "openDate": "2020-01-15",
  "expirationDate": "2028-01-15",
  "cashCreditLimit": 10000.00,
  "currentCycleDebit": 2500.00,
  "currentCycleCredit": 0.00,
  "groupId": "GROUP_A"
}
```

**Error Responses**:
- 404: Account not found
  ```json
  {
    "timestamp": "2026-11-06T10:30:00",
    "code": "ACCOUNT_NOT_FOUND",
    "message": "Account not found with ID: 999",
    "path": "/api/accounts/999",
    "status": 404
  }
  ```

---

### PUT /accounts/{id}

Update account balance and active status.

**Source**: CBACT01C (Account balance update)

**Path Parameters**:
- `id` (Long): Account ID

**Request Body**:
```json
{
  "currentBalance": 20000.00,
  "activeStatus": true
}
```

**Response** (200 OK): Updated account DTO

**Error Responses**:
- 400: Invalid input
- 404: Account not found

---

### GET /accounts/{id}/transactions

List transactions for an account within optional date range.

**Source**: CBTRN03C (Transaction reporting)

**Path Parameters**:
- `id` (Long): Account ID

**Query Parameters**:
- `from` (optional, date YYYY-MM-DD): Start date
- `to` (optional, date YYYY-MM-DD): End date

**Response** (200 OK):
```json
[
  {
    "id": "TXN001",
    "accountId": 1,
    "cardNumber": "4111111111111111",
    "amount": 150.00,
    "date": "2026-10-15",
    "postDate": "2026-10-15",
    "type": "PURCHASE",
    "status": "SETTLED",
    "merchantId": "M123",
    "merchantName": "Walmart",
    "processingCode": "000000"
  }
]
```

---

### POST /accounts/{id}/balance

Post a debit or credit transaction to account balance.

**Source**: CBACT01C (Balance posting), CBTRN01C (Transaction posting)

**Path Parameters**:
- `id` (Long): Account ID

**Request Body**:
```json
{
  "amount": 100.00,
  "type": "PURCHASE",
  "merchantId": "M123",
  "merchantName": "Walmart"
}
```

**Response** (200 OK): Updated account DTO

**Error Responses**:
- 400: Invalid amount or type
- 404: Account not found
- 409: Account inactive or insufficient balance

---

### POST /accounts/{id}/reissue

Issue a new card for an account and block old cards.

**Source**: COCRDUPC (Card reissue)

**Path Parameters**:
- `id` (Long): Account ID

**Request Body**:
```json
{
  "cardNumber": "5555555555554444",
  "expiryDate": "2028-12-31"
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "number": "5555555555554444",
  "accountId": 1,
  "status": "ACTIVE",
  "expiryDate": "2028-12-31"
}
```

---

### GET /accounts/{id}/cards

List all cards for an account.

**Source**: COCRDUPC (Card management)

**Path Parameters**:
- `id` (Long): Account ID

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "number": "4111111111111111",
    "accountId": 1,
    "status": "ACTIVE",
    "expiryDate": "2028-12-31"
  }
]
```

---

## Cards

### GET /cards/{number}

Retrieve card details by card number.

**Source**: COCRDLIC (Card lookup)

**Path Parameters**:
- `number` (String): Card number (PAN)

**Response** (200 OK): Card DTO

**Error Responses**:
- 404: Card not found

---

### GET /cards/account/{accountId}

List all cards for an account (alternative endpoint).

**Path Parameters**:
- `accountId` (Long): Account ID

**Response** (200 OK): Array of card DTOs

---

### POST /cards

Issue a new card.

**Source**: COCRDUPC (Card issuance)

**Request Body**:
```json
{
  "accountId": 1,
  "number": "4532015112830366",
  "expiryDate": "2028-06-30"
}
```

**Response** (201 Created): Card DTO

**Error Responses**:
- 400: Invalid card number or account ID
- 404: Account not found
- 409: Card number already exists (duplicate)

---

## Transactions

### GET /transactions/{id}

Retrieve transaction details by ID.

**Source**: CBTRN01C (Transaction lookup)

**Path Parameters**:
- `id` (String): Transaction ID

**Response** (200 OK): Transaction DTO

**Error Responses**:
- 404: Transaction not found

---

### POST /transactions

Post a new transaction.

**Source**: CBTRN01C (Daily transaction posting)

**Request Body**:
```json
{
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 150.00,
  "type": "PURCHASE",
  "merchantId": "M123",
  "merchantName": "Walmart"
}
```

**Response** (201 Created):
```json
{
  "id": "TXN1730860200000",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 150.00,
  "date": "2026-11-06",
  "postDate": null,
  "type": "PURCHASE",
  "status": "PENDING",
  "merchantId": "M123",
  "merchantName": "Walmart",
  "processingCode": null
}
```

**Error Responses**:
- 400: Invalid amount or type
- 404: Account or card not found

---

### POST /transactions/settle

Settle a transaction (mark as posted/cleared).

**Source**: CBTRN02C (Transaction settlement)

**Request Body**:
```json
{
  "transactionId": "TXN1730860200000"
}
```

**Response** (200 OK): Updated transaction DTO with status=SETTLED

**Error Responses**:
- 404: Transaction not found

---

### GET /reports/transactions

Generate transaction report (summary by status).

**Source**: CBTRN03C (Transaction reporting)

**Query Parameters**:
- `asOf` (optional, date YYYY-MM-DD): Report date (default: today)

**Response** (200 OK):
```json
{
  "asOf": "2026-11-06",
  "totalCount": 20,
  "totalAmount": 5432.50,
  "byStatus": {
    "PENDING": 3,
    "POSTED": 5,
    "SETTLED": 12
  }
}
```

---

## Authorizations

### POST /authorizations

Request authorization for a transaction.

**Source**: COPAUA0C (Real-time authorization decision)

**Request Body**:
```json
{
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 500.00,
  "merchantId": "M125",
  "merchantName": "Amazon"
}
```

**Response** (201 Created):
```json
{
  "id": "AUTH1730860200000",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 500.00,
  "requestDate": "2026-11-06",
  "responseDate": "2026-11-06",
  "expiryDate": "2026-11-09",
  "status": "APPROVED",
  "declineReasonCode": null,
  "approvedAmount": 500.00
}
```

**Decline Scenarios** (per COPAUA0C R00016):

- 4100 (Insufficient Funds):
```json
{
  "id": "AUTH...",
  "status": "DECLINED",
  "declineReasonCode": "4100",
  "approvedAmount": null
}
```

- 5100 (Account Closed):
```json
{
  "id": "AUTH...",
  "status": "DECLINED",
  "declineReasonCode": "5100",
  "approvedAmount": null
}
```

---

### GET /authorizations/{id}

Retrieve authorization details by ID.

**Path Parameters**:
- `id` (String): Authorization ID

**Response** (200 OK): Authorization DTO

**Error Responses**:
- 404: Authorization not found

---

### GET /authorizations/pending

List all pending authorizations.

**Source**: COPAUA0C (Pending authorization summary, rule R00010)

**Response** (200 OK):
```json
[
  {
    "id": "AUTH1730860200000",
    "accountId": 5,
    "cardNumber": "3714496353622689",
    "amount": 100.00,
    "requestDate": "2026-10-24",
    "responseDate": null,
    "expiryDate": "2026-10-27",
    "status": "PENDING",
    "declineReasonCode": null,
    "approvedAmount": null
  }
]
```

---

### PUT /authorizations/{id}/review

Review and approve/decline a pending authorization.

**Source**: COPAUS0C/1C/2C (Pending authorization review)

**Path Parameters**:
- `id` (String): Authorization ID

**Request Body**:
```json
{
  "decision": "APPROVE",
  "notes": "Manual review approved"
}
```

Or:
```json
{
  "decision": "DECLINE",
  "notes": "Suspected fraud"
}
```

**Response** (200 OK): Updated authorization DTO with status=APPROVED or DECLINED

**Error Responses**:
- 404: Authorization not found
- 400: Invalid decision

---

### POST /authorizations/expire

Expire pending authorizations that have passed their expiry date.

**Source**: CBPAUP0C (Authorization expiration management, 3-day default)

**Request Body**: {} (empty)

**Response** (200 OK):
```json
{
  "expiredCount": 2
}
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2026-11-06T10:30:00",
  "code": "ERROR_CODE",
  "message": "Human-readable error message",
  "path": "/api/endpoint/path",
  "status": 400
}
```

### HTTP Status Codes

| Status | Meaning | Examples |
|--------|---------|----------|
| 200 | OK | GET, PUT, POST successful |
| 201 | Created | POST creates new resource |
| 400 | Bad Request | Invalid input, validation failed |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Insufficient funds, card expired, duplicate ID |
| 500 | Server Error | Unexpected internal error |

---

## Example Workflows

### Workflow 1: Post Purchase Transaction

1. `POST /authorizations` → Authorization APPROVED (available credit sufficient)
2. `POST /transactions` → Transaction created with status=PENDING
3. `POST /transactions/settle` → Transaction status→SETTLED, account balance updated

### Workflow 2: Insufficient Funds Decline

1. `POST /authorizations` → Authorization DECLINED (reason code 4100)
2. Transaction NOT created (authorization failed)

### Workflow 3: Card Reissue

1. `POST /accounts/{id}/reissue` → New card ACTIVE, old cards BLOCKED
2. Subsequent transactions use new card number

---

## Pagination (Phase 2)

- Phase 1: No pagination
- Phase 2: Add `limit`, `offset` query params to LIST endpoints

## Rate Limiting (Phase 2)

- Phase 1: No rate limits
- Phase 2: Implement via Spring Cloud Gateway or custom interceptor

---

**API Specification Version**: 1.0.0
**Last Updated**: 2026-11-06
**COBOL Source Programs**: CBACT01C, CBTRN01C, COPAUA0C, COCRDUPC, etc.
