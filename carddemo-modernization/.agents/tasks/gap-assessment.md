# CardDemo Java + React Modernization: Gap Assessment & Dependencies

## Overview

This assessment evaluates the AWS Transform-generated documentation for the CardDemo COBOL system and determines what is available, what is missing, and what assumptions Phase 1 must make for a successful Java + React conversion.

**Assessment Date:** 2026-11-05  
**Source Documents:** Business_Document and Technical_document directories (AWS Transform output)  
**Target Stack:** Java 17 Spring Boot 3.x, React, MySQL/H2

---

## What the AWS Transform Documentation Covers

### Program-Level Analysis ✓

The reverse-engineering successfully captured **29 COBOL programs** with detailed business logic documentation:

- **Account Programs (CBACT01C-04C):** Account file processing, customer data retrieval, balance tracking, interest calculation
- **Transaction Programs (CBTRN01C-03C):** Daily transaction posting, settlement, reporting
- **Authorization Programs (COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C):** Real-time authorization decisions, pending authorization management, expiration handling
- **Card Programs (COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC):** Card issuance, updates, billing
- **User/Admin Programs (COUSR00C-03C, COADM01C):** User security, administrative workflows
- **Utilities (CBSTM03A/B, CODATE01, CSUTLDTC):** File I/O, date conversion

Each program document includes:
- **Main flow diagrams** (Mermaid flowcharts showing program logic)
- **100+ business rules per program** (detailed acceptance criteria, decision trees, process steps)
- **Data field mappings** (input/output structures with PIC clause hints via descriptions)
- **File/database interactions** (VSAM KSDS, IMS, DB2, MQ queues referenced)

### Data Structures Partially Captured ✓

From program rules and file references, the following entities can be inferred:

- **Account Master (CBACT01C, ACCTFILE):** Account ID, Balance, Credit Limit, Active Status, Reissue Date, Open/Expiration Dates, Cash Credit Limit, Current Cycle Debit/Credit, Group ID
- **Card Cross-Reference (CCXREF, XREF-FILE):** Card Number → Account ID, Customer ID  
- **Customer Master (CUSTDAT, CUSTOMER-FILE):** Customer ID, Profile Information
- **Transaction (CBTRN01C-03C, TRANSACT-FILE):** Card Number, Transaction ID, Amount, Date, Type, Status, Merchant Details
- **Authorization (COPAUA0C, PAUTSUM0/PAUTDTL1):** Authorization ID, Account ID, Amount, Date, Status, Decline Reason, Pending/Settled Flag
- **Card (CARD-FILE):** Card Number, Status, Expiry Date, Account ID
- **Transaction Type (COTRTUPC, DB2):** Type ID, Description, Category

**PIC Clauses Inferred from Descriptions:**
- Money fields → COMP-3 (packed decimal) → `BigDecimal` in Java
- Dates (YYYYMMDD) → PIC 9(8) → `LocalDate` in Java
- Flags (Y/N) → PIC X(1) → `Boolean` or `char` in Java
- Counts → PIC 9(5) → `int` in Java
- IDs → PIC 9(8)-9(12) → `Long` in Java

---

## Critical Gaps Identified

### 1. **Missing Copybook/Record Layout Details** ❌

**What's missing:** Exact COBOL copybooks (e.g., `CBACT01C-cpy`, `CBTRN01C-cpy`) with precise field definitions, lengths, and encoding.

**Impact:** Uncertain field widths, decimal places, and trailing/leading zero handling.

**Assumption for Phase 1:**
- Account: 50-char ID, BigDecimal for money (2 decimal places), Boolean for active status, LocalDate for dates
- Transaction: 12-char ID, 20-char Card Number, BigDecimal amount, LocalDate/time for posting
- Authorization: BigDecimal limits, 3-day expiry default, approved/declined/pending status enum
- See `docs/data-model.md` for full schema and documented assumptions

### 2. **Missing DB2 and IMS Schema Definitions** ❌

**What's missing:** 
- DB2 table schemas (Transaction Type module uses COTRTUPC accessing DB2)
- IMS PAUTSUM0/PAUTDTL1 segment layouts and key hierarchies
- Primary/foreign key definitions across all data stores

**Impact:** Authorization module logic (COPAUA0C) references IMS pending auth summary; translation requires schema clarity.

**Assumption for Phase 1:**
- IMS and DB2 will be replaced with MySQL relational tables
- Pending authorization summary will be a flattened table (not hierarchical)
- No segment-to-table 1:1 mapping; instead, denormalize as needed for performance

### 3. **Missing BMS Screen Map Definitions** ❌

**What's missing:** CICS BMS (Basic Mapping Support) map files that define UI layout, field placement, and input/output formatting for screens like CICSMAP.

**Impact:** Cannot directly translate screen layouts; React UI must be redesigned from business rules, not screen captures.

**Assumption for Phase 1:**
- Focus on REST API contracts (GET/PUT/POST endpoints)
- Assume CICS screen workflows map to sequential API calls
- React Phase 2 will design screens based on API specs and UX patterns, not BMS maps

### 4. **Missing JCL Job Definitions** ⚠️

**What's covered:** JCL JSON summaries exist but lack detailed scheduling, parameter passing, and error handling.

**Impact:** Batch job orchestration (e.g., POSTTRAN, INTCALC scheduling) is unclear.

**Assumption for Phase 1:**
- Batch operations (interest calculation, transaction settlement) will become background Spring job services
- No JCL translation needed; instead, use Spring Scheduler or Quartz
- Daily/monthly batch cycles inferred from business rules

### 5. **Missing MQ Message Format Specifications** ⚠️

**What's covered:** Authorization programs (COPAUA0C) reference MQ queue reads/writes with "comma-delimited" format.

**Impact:** Cannot accurately parse/serialize MQ messages without explicit field order and separator specification.

**Assumption for Phase 1:**
- Authorization requests will be REST POST calls (not MQ)
- MQ integration deferred to Phase 2
- External system integration via REST APIs in `config` package (mock or adapter pattern)

### 6. **Missing Error/Exception Codes** ⚠️

**What's covered:** Business rules define some decline reasons (3100=Card Not Found, 4100=Insufficient Funds, etc.) but error categories are incomplete.

**Assumption for Phase 1:**
- Define custom exceptions: `AccountNotFoundException`, `InvalidTransactionException`, `AuthorizationExpiredException`, `InsufficientFundsException`
- HTTP 400 (bad request), 404 (not found), 409 (conflict), 500 (server error) mappings
- Reason codes and error messages in exception details

### 7. **Missing Performance/Concurrency Requirements** ❌

**What's missing:** SLAs, throughput targets, concurrency limits (e.g., COPAUA0C processes 500 messages in a loop—what's the time window?).

**Assumption for Phase 1:**
- Assume modest scale: <1000 active accounts, <10k transactions/day, <100 authorizations/min
- Single Spring Boot instance with H2 in-memory DB for dev; MySQL for production
- No distributed caching or horizontal scaling in Phase 1

---

## What Phase 1 Will Deliver

### Entities (Derived from AWS Transform Docs)

1. **Account**
   - id (Long, primary key)
   - creditLimit (BigDecimal)
   - currentBalance (BigDecimal)
   - activeStatus (Boolean)
   - reissueDate (LocalDate)
   - open/expirationDate (LocalDate)
   - cashCreditLimit (BigDecimal)
   - currentCycleDebit/Credit (BigDecimal)
   - groupId (String)

2. **Card**
   - number (String, primary key / unique)
   - accountId (Long, FK to Account)
   - status (enum: ACTIVE, EXPIRED, BLOCKED)
   - expiryDate (LocalDate)

3. **Customer**
   - id (Long, primary key)
   - name (String)
   - contact (String) — email/phone combined

4. **Transaction**
   - id (String, primary key)
   - accountId (Long, FK)
   - cardNumber (String, FK)
   - amount (BigDecimal)
   - date (LocalDate)
   - postDate (LocalDate)
   - type (enum: PURCHASE, CASH_ADVANCE, PAYMENT, FEE)
   - status (enum: PENDING, POSTED, SETTLED)
   - merchantId (String)
   - merchantName (String)
   - processingCode (String)

5. **Authorization**
   - id (String, primary key)
   - accountId (Long, FK)
   - cardNumber (String, FK)
   - amount (BigDecimal)
   - requestDate (LocalDate)
   - responseDate (LocalDate)
   - expiryDate (LocalDate) — expires 3 days by default per CBPAUP0C
   - status (enum: PENDING, APPROVED, DECLINED)
   - declineReasonCode (String) — 3100, 4100, 4200, 4300, 5100, 5200, 9000
   - approvedAmount (BigDecimal)

6. **TransactionType**
   - id (String, primary key)
   - description (String)
   - category (String)

### REST Endpoints (Mapped to Business Rules)

**Accounts:**
- GET /api/accounts/{id} (read account)
- PUT /api/accounts/{id} (update balance, status per COACTUPC)
- GET /api/accounts/{id}/transactions (list by account)
- PUT /api/accounts/{id}/balance (post debit/credit)
- POST /api/accounts/{id}/reissue (new card per COCRDUPC)

**Cards:**
- GET /api/cards/{id} (read card)
- GET /api/accounts/{id}/cards (list by account)
- POST /api/cards (issue new card)

**Transactions:**
- GET /api/transactions/{id}
- POST /api/transactions (post daily transaction per CBTRN01C)
- POST /api/transactions/settle (settlement per CBTRN02C)
- GET /api/reports/transactions (report per CBTRN03C)

**Authorizations:**
- POST /api/authorizations (authorize per COPAUA0C)
- GET /api/authorizations/{id}
- GET /api/authorizations/pending
- PUT /api/authorizations/{id}/review (COPAUS0C/1C/2C decision)
- POST /api/authorizations/expire (cleanup per CBPAUP0C)

### Services

- **AccountService:** getAccount, updateBalance, getTransactions, calculateInterest (CBACT04C logic)
- **CardService:** getCard, issueCard, expireCard, verifyCard
- **TransactionService:** postTransaction, settle, generateReport, getByDateRange
- **AuthorizationService:** authorize, expireAuthorizations, review, getStatus
- **TransactionTypeService:** getAll, update

### Testing & Documentation

- JUnit 5 + Mockito unit tests for services (>80% coverage via JaCoCo)
- Repository tests verifying finder methods
- REST integration tests exercising endpoints
- Swagger/OpenAPI spec auto-generated from @RestController annotations
- `docs/data-model.md` documenting entity relationships, PIC assumptions, and mapping rationale
- `docs/api-spec.md` with endpoint descriptions and example payloads

---

## Dependencies & Environment

### Java/Spring Stack

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.35</version>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>
  <dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.4</version>
  </dependency>
</dependencies>
```

### Profiles

- **application.yml:** Common settings
- **application-dev.yml:** H2 in-memory database (file: mem:carddemo)
- **application-prod.yml:** MySQL (jdbc:mysql://localhost:3306/carddemo)

### Sample Data

- 5 accounts with varying credit limits and balances
- 10 cards linked to accounts
- 20 transactions across date ranges
- 5 pending/approved/declined authorizations

---

## Validation Strategy for Phase 1

✓ Entities persist and retrieve correctly from H2/MySQL  
✓ All REST endpoints respond with valid status codes and payloads  
✓ Services apply business rules from COBOL programs (e.g., interest calculation, decline reason logic)  
✓ Unit and integration tests pass with >80% coverage  
✓ Swagger UI displays all endpoints  
✓ `docs/data-model.md` and `docs/api-spec.md` are complete and accurate  

---

## Phase 2 Scope (Out of Scope for Phase 1)

- React front-end UI development
- Real-time authorization messaging (MQ integration)
- IMS hierarchical database support (if needed for legacy integration)
- DB2 migration (assuming all data moves to MySQL)
- Batch job scheduling and orchestration (Spring Scheduler as placeholder)
- Advanced security (OAuth2, multi-tenancy, audit logging)
- Performance tuning and horizontal scaling

---

## Conclusion

The AWS Transform documentation provides **sufficient detail** on business logic, data flows, and entity relationships to build a working Java backend for Phase 1. The main unknowns are copybook field widths and MQ message formats, but these can be inferred conservatively or added incrementally. Documentation gaps will be addressed with comments in code and in `docs/data-model.md` to ensure Phase 2 developers understand assumptions made during the initial conversion.

**Recommended Action:** Proceed to Phase 1 implementation using the plan in `plan.md`. Revisit original COBOL copybooks and IMS schemas if field-level precision becomes critical during testing.
