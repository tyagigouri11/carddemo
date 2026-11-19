# CardDemo Data Model

## Overview

This document describes the entity relationships, PIC clause mappings from COBOL, and design assumptions for the Phase 1 Java Spring Boot implementation.

## Entity Relationships (ER Model)

```
Customer
    ↓ (1:N)
Account (1:N) ↔ Card
    ↓ (1:N)
Transaction
    ↓
AuthorizationTransactionType
```

- **Account 1:N Card**: Each account can have multiple cards (ACTIVE, EXPIRED, BLOCKED)
- **Account 1:N Transaction**: Each account has a transaction history
- **Account 1:N Authorization**: Each account has authorization requests
- **Card ∞:1 Transaction**: Transactions reference cards by card number (weak FK)
- **TransactionType**: Reference table for transaction type definitions

## Entity Specifications

### 1. Account

**Source**: CBACT01C-04C (Account file processing, balance tracking, interest calculation)

| Field | PIC/Type | Java Type | Notes |
|-------|----------|-----------|-------|
| id | 9(12) | Long | Primary key, auto-increment |
| creditLimit | COMP-3 9(9)V99 | BigDecimal(12,2) | Maximum credit available |
| currentBalance | COMP-3 9(9)V99 | BigDecimal(12,2) | Current balance (sum of all debits/credits) |
| activeStatus | X(1) Y/N | Boolean | TRUE=active, FALSE=inactive (per CBACT01C R00003) |
| reissueDate | 9(8) YYYYMMDD | LocalDate | Date of card reissue (per COCRDUPC) |
| openDate | 9(8) YYYYMMDD | LocalDate | Account opening date |
| expirationDate | 9(8) YYYYMMDD | LocalDate | Account expiration date |
| cashCreditLimit | COMP-3 9(5)V99 | BigDecimal(7,2) | Cash advance limit (≤ creditLimit per R00005) |
| currentCycleDebit | COMP-3 9(7)V99 | BigDecimal(9,2) | Purchase debits in current cycle (R00008: default 2525.00) |
| currentCycleCredit | COMP-3 9(7)V99 | BigDecimal(9,2) | Payment credits in current cycle (R00009) |
| groupId | X(50) | String | Batch group ID for processing |

**PIC Clause Mapping Rationale**:
- COMP-3 (packed decimal) is a binary-encoded decimal format in COBOL used for memory efficiency
- V99 suffix indicates 2 decimal places (implicit decimal point)
- 9(12) for IDs: 12-digit numeric identifier typical in financial systems
- All date fields use YYYYMMDD pattern per COBOL date storage

### 2. Card

**Source**: COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC (Card issuance, updates, reissue)

| Field | PIC/Type | Java Type | Notes |
|-------|----------|-----------|-------|
| id | 9(12) | Long | Primary key, auto-increment |
| number | X(19) | String | Card number (PAN), 12-19 digits per credit card standards |
| accountId | FK | Long | Reference to Account (COBIL00C: every card linked to account) |
| status | X(1) enum | CardStatus | A=ACTIVE, E=EXPIRED, B=BLOCKED |
| expiryDate | 9(8) YYYYMMDD | LocalDate | Card expiration date (displayed as MM/YY) |

**CardStatus Enum**:
- ACTIVE: Card is valid and can be used for transactions
- EXPIRED: Card has passed expiration date (per CBACT01C R00001)
- BLOCKED: Card has been flagged as lost/fraud (per COCRDUPC R00003)

### 3. Customer

**Source**: COUSR00C-03C (User/Admin programs - customer profile)

| Field | PIC/Type | Java Type | Notes |
|-------|----------|-----------|-------|
| id | 9(8) | Long | Primary key, auto-increment |
| name | X(100) | String | Customer full name |
| contact | X(100) | String | Email and/or phone (combined for Phase 1) |

### 4. Transaction

**Source**: CBTRN01C-03C (Daily transaction posting, settlement, reporting)

| Field | PIC/Type | Java Type | Notes |
|-------|----------|-----------|-------|
| id | X(12) | String | Primary key, format: TXN + timestamp |
| accountId | FK | Long | Reference to Account |
| cardNumber | X(19) | String | Card number used (weak FK to Card.number) |
| amount | COMP-3 9(9)V99 | BigDecimal(12,2) | Transaction amount |
| date | 9(8) YYYYMMDD | LocalDate | Transaction date (when occurred) |
| postDate | 9(8) YYYYMMDD | LocalDate | Post date (1-2 days later per CBTRN02C R00005) |
| type | X(1) enum | TransactionType | P=PURCHASE, C=CASH, F=FEE, I=INTEREST, M=PAYMENT |
| status | X(1) enum | TransactionStatus | P=PENDING, S=POSTED, E=SETTLED |
| merchantId | X(50) | String | Merchant ID |
| merchantName | X(100) | String | Merchant name |
| processingCode | X(10) | String | Processing code (per CBTRN01C R00008) |

**TransactionType Enum**:
- PURCHASE: Purchase debit transaction (CBTRN01C R00005: category=DEBIT)
- CASH_ADVANCE: Cash withdrawal (category=DEBIT)
- PAYMENT: Payment received (category=CREDIT)
- FEE: Service/late fee (category=DEBIT)
- INTEREST: Interest charge (category=DEBIT)

**TransactionStatus Enum** (per CBTRN02C):
- PENDING: Transaction submitted but not yet posted
- POSTED: Transaction posted to account balance
- SETTLED: Transaction cleared through payment processor

### 5. Authorization

**Source**: COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C (Real-time authorization, pending auth management)

| Field | PIC/Type | Java Type | Notes |
|-------|----------|-----------|-------|
| id | X(12) | String | Primary key, format: AUTH + timestamp |
| accountId | FK | Long | Reference to Account |
| cardNumber | X(19) | String | Card number (weak FK) |
| amount | COMP-3 9(9)V99 | BigDecimal(12,2) | Requested authorization amount |
| requestDate | 9(8) YYYYMMDD | LocalDate | Authorization request date |
| responseDate | 9(8) YYYYMMDD | LocalDate | Authorization response date |
| expiryDate | 9(8) YYYYMMDD | LocalDate | Expiry date (3 days from request per CBPAUP0C) |
| status | X(1) enum | AuthorizationStatus | P=PENDING, A=APPROVED, D=DECLINED |
| declineReasonCode | X(4) | String | Reason code if declined (R00016) |
| approvedAmount | COMP-3 9(9)V99 | BigDecimal(12,2) | Amount approved (may be less than requested) |

**Decline Reason Codes** (COPAUA0C R00016):
- 3100: Card Not Found
- 4100: Insufficient Funds (per COPAUA0C R00013: available credit < requested amount)
- 4200: Expired Card
- 4300: Card Blocked
- 5100: Account Closed
- 5200: Account Suspended
- 9000: System Error
- 9001: Authorization Expired

**AuthorizationStatus Enum**:
- PENDING: Authorization request submitted, awaiting response
- APPROVED: Authorization approved, transaction may proceed (per COPAUA0C R00010)
- DECLINED: Authorization declined, transaction blocked

**3-Day Expiry Assumption** (CBPAUP0C):
- Pending authorizations default to 3 days from request date
- Used for cleanup of aged pending authorizations (see AuthorizationService.expireAuthorizations())
- Approved/declined authorizations do not expire in Phase 1

### 6. TransactionType

**Source**: COTRTUPC (Transaction type module accessing DB2)

| Field | PIC/Type | Java Type | Notes |
|-------|----------|-----------|-------|
| id | X(50) | String | Primary key, e.g., "PURCH", "CASH", "PMT", "FEE", "INT" |
| description | X(255) | String | Human-readable description |
| category | X(50) | String | DEBIT or CREDIT (determines balance posting treatment) |

## PIC Clause Mapping Summary

| COBOL PIC | Meaning | Java Type | Example |
|-----------|---------|-----------|---------|
| 9(n) | Numeric (n digits) | Long/Integer | 9(12) → Long |
| COMP-3 | Packed decimal | BigDecimal | COMP-3 9(9)V99 → BigDecimal(12,2) |
| X(n) | Character string | String | X(50) → String |
| 9(n)V99 | Numeric with 2 decimals | BigDecimal | 2 decimal places |
| X(1) Y/N | Boolean flag | Boolean | PIC X(1) Y/N → Boolean |
| 9(8) YYYYMMDD | Date (YYYYMMDD) | LocalDate | YYYYMMDD format |

**Decimal Precision Convention**:
- All money fields use BigDecimal with 2 decimal places (cents)
- Scale 2, precision 12 for general amounts: `DECIMAL(12, 2)`
- JPA: `@Column(precision=12, scale=2)`

## Design Assumptions for Phase 1

### 1. **No Copybook Field Widths**
- Assumption: Account ID up to 12 digits (Long range)
- Assumption: Card number 12-19 characters (standard credit card length)
- Assumption: Monetary values fit in DECIMAL(12, 2)
- Rationale: AWS Transform docs describe field *purposes* but not exact widths; chosen conservatively to avoid data loss

### 2. **IMS Hierarchical Data → Relational Tables**
- Assumption: PAUTSUM0/PAUTDTL1 (pending authorization summary in IMS) flattened into single `authorization` table
- Rationale: MySQL does not support hierarchical segments; Phase 2 can optimize hierarchical access patterns if needed

### 3. **DB2 → MySQL**
- Assumption: All DB2 tables migrated to MySQL (no DB2 in Phase 1 runtime)
- Assumption: TransactionType table created in MySQL (sourced from COTRTUPC)
- Rationale: MySQL is RDBMS compatible with Spring Boot and easier to deploy than DB2

### 4. **MQ Message Format**
- Assumption: Authorization requests/responses via REST API, not MQ queues (Phase 1)
- Rationale: MQ message format specifications are incomplete in AWS Transform docs; REST is simpler for Phase 1

### 5. **3-Day Authorization Expiry**
- Assumption: Pending authorizations default to 3-day expiry from CBPAUP0C business rules
- Rationale: CBPAUP0C mentions expiration processing; 3 days is conservative for fraud detection
- Implementation: `authorization.expiryDate = requestDate + 3 days`

### 6. **Account-Card Relationship**
- Assumption: Accounts are 1:N with cards (multiple cards per account)
- Assumption: Old cards set to BLOCKED when new card reissued (per COCRDUPC rule)
- Rationale: Typical credit card account structure; enables card replacement workflow

### 7. **Transaction Balance Updates**
- Assumption: Only transaction posting updates account balance
- Assumption: POSTED transactions count toward available credit calculation
- Assumption: PENDING transactions do not affect balance or available credit (reserved for authorization holds in Phase 2)
- Rationale: Simplifies Phase 1; Phase 2 can add pre-authorization holds

### 8. **Available Credit Calculation** (COPAUA0C R00013)
- Assumption: `availableCredit = creditLimit - currentBalance`
- Assumption: Pending approved authorizations included in available credit calculation (Phase 1 deferred)
- Rationale: Simple model for initial implementation; Phase 2 refines with pending holds

### 9. **Batch Operations**
- Assumption: Interest calculation (CBACT04C) is on-demand service method
- Assumption: Authorization expiration (CBPAUP0C) is on-demand service method
- Rationale: Phase 1 focuses on transaction API; Phase 2 adds scheduled batch jobs via Spring Scheduler

### 10. **No Performance Scaling**
- Assumption: Single Spring Boot instance, H2 (dev) or MySQL (prod)
- Assumption: No distributed caching or horizontal scaling in Phase 1
- Rationale: Phase 1 targets modest scale (<1000 accounts, <10k transactions/day)

## Validation Rules

### Account
- `creditLimit > 0`
- `currentBalance >= 0` (after all posts)
- `activeStatus` must be Boolean
- `reissueDate`, `openDate`, `expirationDate` are optional

### Card
- `number` is unique, 12-19 digits
- `status` is one of {ACTIVE, EXPIRED, BLOCKED}
- `expiryDate >= today` if status=ACTIVE
- `accountId` references valid Account

### Transaction
- `amount > 0`
- `amount <= account.creditLimit - currentBalance` for debits
- `date <= postDate` (if postDate set)
- `type` and `status` enums are valid
- `accountId` references valid Account

### Authorization
- `amount > 0`
- `requestDate <= responseDate` (if responseDate set)
- `expiryDate >= today`
- `status` is one of {PENDING, APPROVED, DECLINED}
- If status=DECLINED, `declineReasonCode` is set
- If status=APPROVED, `approvedAmount` is set
- `accountId` references valid Account

## Cross-Entity Constraints

1. **Account reissue**: When reissuing card (COCRDUPC), all existing ACTIVE cards set to BLOCKED
2. **Transaction posting**: Transaction amount limited by available credit = creditLimit - currentBalance
3. **Authorization expiry**: Pending authorizations with expiryDate <= today are marked DECLINED (CBPAUP0C cleanup)

## Indexes for Performance

- Account: idx_active_status, idx_reissue_date
- Card: idx_account_id, idx_status_expiry
- Transaction: idx_account_id, idx_card_number, idx_date, idx_status, idx_status_date
- Authorization: idx_account_id, idx_status, idx_status_expiry, idx_expiry_date

## Migration Notes (Phase 2)

1. **Copybook Files**: If original COBOL copybooks become available, validate field widths against assumptions
2. **IMS Schemas**: If hierarchical data preservation needed, implement object-graph mapping or hierarchical queries
3. **DB2 Migration**: If legacy DB2 systems require integration, add DB2 driver and dual-path repository methods
4. **MQ Integration**: Implement MQ adapter to replace REST authorization endpoint when messaging format finalized

---

**Document Generated**: Phase 1 Implementation
**Last Updated**: 2026-11-06
