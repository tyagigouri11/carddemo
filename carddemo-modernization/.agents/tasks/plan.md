# CardDemo Java + React Modernization: Phase 1 Implementation Plan

## Project Scope

**Target:** Spring Boot 3.x REST API for CardDemo credit card management application  
**Entities:** Account, Card, Customer, Transaction, Authorization, TransactionType  
**Packages:** entity, repository, service, controller, dto, config, exception  
**Database Profiles:** H2 (dev), MySQL (prod)  
**Testing:** JUnit 5 + Mockito, >80% coverage (JaCoCo)  
**Documentation:** Swagger/OpenAPI, data model, API spec, deployment guide

---

## Implementation Plan

### Phase 1.1: Project Setup & Configuration

- [ ] **1. Initialize Maven Project**
      Create Maven project structure with pom.xml configured for Spring Boot 3.x, Java 17 compiler settings, JaCoCo plugin for code coverage, and standard package layout (src/main/java/com/carddemo/{entity,repository,service,controller,dto,config,exception}, src/main/resources, src/test/java).
      Files: pom.xml
      Verify: `mvn clean compile` — build succeeds, dependency tree displays Spring Boot 3.x and MySQL connector.

- [ ] **2. Create Application Properties & Profiles**
      Write application.yml (common config: port 8080, logging level, Jackson serialization), application-dev.yml (H2 in-memory DB with schema/data initialization), and application-prod.yml (MySQL connection to localhost:3306/carddemo with user/pass placeholders).
      Files: src/main/resources/application.yml, application-dev.yml, application-prod.yml
      Verify: `java -jar target/carddemo.jar --spring.profiles.active=dev` — app starts, H2 console available at http://localhost:8080/h2-console.

- [ ] **3. Create CarddemoApplication Spring Boot Entry Point**
      Write main @SpringBootApplication class with standard configuration, @EnableJpaRepositories, @EnableSpringDataWebSupport, @EnableCaching (optional for Phase 1), and a configuration class importing @RestControllerAdvice bean for global exception handling.
      Files: src/main/java/com/carddemo/CarddemoApplication.java, src/main/java/com/carddemo/config/GlobalExceptionHandler.java
      Verify: `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"` — app starts without errors, logs "Tomcat started on port 8080".

---

### Phase 1.2: Data Model & Database

- [ ] **4. Create Entity Classes**
      Define JPA @Entity classes for Account, Card, Customer, Transaction, Authorization, and TransactionType with @Id, @GeneratedValue, @Column, @ManyToOne, @OneToMany, @Enumerated annotations per gap-assessment assumptions. Use Lombok @Data, @AllArgsConstructor, @NoArgsConstructor to reduce boilerplate. Document PIC clause mappings and inferred field widths in Javadoc comments (e.g., "accountId: PIC 9(12) → Long"; "creditLimit: COMP-3 money field → BigDecimal(12,2)").
      Files: src/main/java/com/carddemo/entity/Account.java, Card.java, Customer.java, Transaction.java, Authorization.java, TransactionType.java
      Verify: `mvn clean compile` — compilation succeeds, no Lombok warnings.

- [ ] **5. Create Database Schema (DDL)**
      Write schema.sql with CREATE TABLE statements for all 6 entities, primary keys, foreign keys, unique constraints (Card.number, Transaction.id, Authorization.id), and indexes on frequently queried fields (Account.id, Card.accountId, Transaction.accountId, Authorization.accountId). Include COMMENT statements documenting original COBOL program sources (e.g., "-- CBACT01C: Account balance tracking"). Ensure DDL is compatible with both H2 and MySQL (use VARCHAR for strings, DECIMAL(12,2) for money, DATE for dates, ENUM or CHAR for status).
      Files: src/main/resources/schema.sql
      Verify: (deferred to schema test; see item 6).

- [ ] **6. Create Sample Data (DML)**
      Write data.sql with INSERT statements for 5 sample accounts (varying credit limits 5000–50000, balances 500–25000), 10 cards (5 active, 3 expired, 2 blocked), 5 customers (dummy names/emails), 20 transactions (mix of PURCHASE, PAYMENT, FEE types, dates spanning last 90 days), and 5 authorizations (2 APPROVED, 2 DECLINED, 1 PENDING). Use realistic values per PIC clauses inferred from COBOL (e.g., current cycle debits in 1000–2500 range per CBACT01C rule R00008).
      Files: src/main/resources/data.sql
      Verify: Start app with dev profile, query H2 console: `SELECT COUNT(*) FROM account;` should return 5; verify foreign key constraints are satisfied; manually check a transaction has amount > 0 and status is one of {PENDING, POSTED, SETTLED}.

- [ ] **7. Document Data Model**
      Create docs/data-model.md describing entity relationships (Account 1:N Card, 1:N Transaction, 1:N Authorization; Card 1:1 Account; Customer 1:N Account; etc.), field-level PIC mappings (e.g., Account.creditLimit: COBOL COMP-3 9(9)V99 → Java BigDecimal with 2 decimal places), assumptions (3-day auth expiry default from CBPAUP0C rule; 2525.00 default cycle debit from CBACT01C rule R00008), and pointers to source programs (CBACT01C, COPAUA0C, etc.). Include ER diagram (text or reference to MySQL Workbench export).
      Files: docs/data-model.md
      Verify: File contains ≥6 entities, ≥1 PIC mapping per entity with rationale, ≥3 assumptions explicitly stated with source program reference.

---

### Phase 1.3: Repositories & Finder Methods

- [ ] **8. Create JPA Repository Interfaces**
      Write Spring Data JPA repository interfaces extending CrudRepository<T, ID> for all 6 entities. Add custom finder methods per business requirements: AccountRepository (findByIdWithTransactions, findByActiveStatusTrue, findByIdWithCards, findByReissueDateBetween); CardRepository (findByAccountId, findByNumber, findByStatusAndExpiryDateBefore); CustomerRepository (findByName); TransactionRepository (findByAccountId, findByCardNumber, findByDateBetween, findByStatus, findByStatusAndDateBefore); AuthorizationRepository (findByAccountId, findByStatusPending, findByExpiryDateBefore); TransactionTypeRepository (findAll). Use @Query annotations with JPQL or native SQL when needed to optimize JOINs or bulk updates.
      Files: src/main/java/com/carddemo/repository/AccountRepository.java, CardRepository.java, CustomerRepository.java, TransactionRepository.java, AuthorizationRepository.java, TransactionTypeRepository.java
      Verify: `mvn clean test -Dtest=*RepositoryTest` — all repository finder tests pass (see item 21 for test specifics). No SQL syntax errors in @Query annotations.

---

### Phase 1.4: Service Layer

- [ ] **9. Create AccountService**
      Implement AccountService with methods: getAccount(Long id) returning Account or throw AccountNotFoundException; updateBalance(Long id, BigDecimal amount, String type) updating currentBalance and posting a Transaction; getAccountTransactions(Long id, LocalDate from, LocalDate to) returning list; calculateInterest(Long id, BigDecimal annualRate, LocalDate asOf) per CBACT04C rule (compute interest on balance, post as FEE transaction); reissueCard(Long id, String cardNumber) per COCRDUPC (create new Card, set expiryDate to +5 years, mark old card BLOCKED). Use @Transactional for mutation methods. Inject AccountRepository and TransactionRepository.
      Files: src/main/java/com/carddemo/service/AccountService.java
      Verify: Unit tests (item 22) pass; service correctly applies balance calculations and creates transaction records.

- [ ] **10. Create CardService**
      Implement CardService with methods: getCard(String number) or throw AccountNotFoundException; getCardsByAccount(Long accountId) returning list; issueCard(Long accountId, String number, LocalDate expiry) per COCRDUPC (create Card, status=ACTIVE, link to account); expireCard(String number) per CBACT01C (set status=EXPIRED); verifyCard(String number) checking status=ACTIVE and expiryDate > today. Inject CardRepository and AccountRepository.
      Files: src/main/java/com/carddemo/service/CardService.java
      Verify: Unit tests (item 22) pass; verifyCard throws InvalidTransactionException if card is expired or blocked.

- [ ] **11. Create TransactionService**
      Implement TransactionService with methods: postTransaction(Long accountId, String cardNumber, BigDecimal amount, String type, String merchantId, String merchantName) per CBTRN01C (create Transaction with status=PENDING, post date = today); getTransactions(LocalDate from, LocalDate to, Long accountId) returning filtered list; settleTransaction(String txnId) per CBTRN02C (update status=SETTLED, post to account balance); generateTransactionReport(LocalDate asOf) per CBTRN03C (return summary: count, total amount, by status). Inject TransactionRepository and AccountRepository. Use calculateInterest helper from AccountService if needed.
      Files: src/main/java/com/carddemo/service/TransactionService.java
      Verify: Unit tests (item 22) pass; postTransaction creates transaction with status PENDING and does not immediately affect account balance; settleTransaction updates balance and changes status to SETTLED.

- [ ] **12. Create AuthorizationService**
      Implement AuthorizationService with methods: authorizeTransaction(Long accountId, String cardNumber, BigDecimal amount, String merchantId, String merchantName) per COPAUA0C (calculate available credit = creditLimit - currentBalance, compare to amount, return APPROVED or DECLINED with reason code per decline rules 3100/4100/4200/etc., set expiryDate = today + 3 days); expireAuthorizations() per CBPAUP0C (find authorizations with expiryDate <= today, mark DECLINED); getAuthorizationStatus(String authId) returning Authorization; processAuthorizationReview(String authId, String decision, String notes) per COPAUS0C/1C/2C (transition PENDING → APPROVED or final DECLINED, update approvedAmount). Inject AuthorizationRepository, AccountRepository, CardRepository. Apply pending authorization summary logic from COPAUA0C rule R00010-R00012 (use account balance + pending approved auths to compute available credit; fallback to account balance if no pending summary).
      Files: src/main/java/com/carddemo/service/AuthorizationService.java
      Verify: Unit tests (item 22) pass; authorizeTransaction correctly calculates available credit and applies decline reason codes per business rules.

- [ ] **13. Create TransactionTypeService**
      Implement TransactionTypeService with methods: getAll() returning list of TransactionType; update(String id, String description, String category) per COTRTUPC (update and save). Inject TransactionTypeRepository.
      Files: src/main/java/com/carddemo/service/TransactionTypeService.java
      Verify: Unit tests (item 22) pass; update persists changes to database.

---

### Phase 1.5: Data Transfer Objects & Mapping

- [ ] **14. Create DTO Classes**
      Define DTOs for API request/response payloads: AccountDto (id, creditLimit, currentBalance, activeStatus, reissueDate), CardDto (number, accountId, status, expiryDate), CustomerDto (id, name, contact), TransactionDto (id, accountId, cardNumber, amount, date, type, status, merchantId, merchantName), AuthorizationDto (id, accountId, cardNumber, amount, requestDate, responseDate, expiryDate, status, declineReasonCode, approvedAmount), TransactionTypeDto (id, description, category). Include Jakarta.validation annotations (@NotNull, @Positive, @PastOrPresent, @FutureOrPresent) for request validation.
      Files: src/main/java/com/carddemo/dto/*Dto.java (one per entity)
      Verify: `mvn clean compile` — no validation or Lombok errors.

- [ ] **15. Create MapStruct Mappers**
      Write MapStruct mapper interfaces using @Mapper(componentModel = "spring") to convert Entity ↔ Dto: AccountMapper (Account ↔ AccountDto), CardMapper, CustomerMapper, TransactionMapper, AuthorizationMapper, TransactionTypeMapper. Include custom mappings for LocalDate formatting (if needed) and nested object mappings (e.g., Transaction → TransactionDto includes embedded merchant fields).
      Files: src/main/java/com/carddemo/mapper/*Mapper.java
      Verify: `mvn clean compile` — MapStruct generates implementations without errors; mapper beans are autowired in controllers.

---

### Phase 1.6: REST Controllers & Exception Handling

- [ ] **16. Create AccountController**
      Implement REST endpoints per gap-assessment:
      - GET /api/accounts/{id} (read; 200 OK or 404 Not Found)
      - PUT /api/accounts/{id} (update balance, activeStatus; 200 OK or 400 Bad Request)
      - GET /api/accounts/{id}/transactions?from=YYYY-MM-DD&to=YYYY-MM-DD (list; 200 OK)
      - PUT /api/accounts/{id}/balance (POST debit/credit: {amount, type}; 200 OK, updates account balance and creates transaction)
      - POST /api/accounts/{id}/reissue (issue new card: 201 Created, returns CardDto)
      
      All endpoints return standardized DTO responses wrapped in ResponseEntity. Inject AccountService, CardService, mappers. Apply Jackson @JsonSerialize annotations for date/decimal formatting if needed (LocalDate as YYYY-MM-DD, BigDecimal as string with 2 decimals).
      Files: src/main/java/com/carddemo/controller/AccountController.java
      Verify: Integration tests (item 23) pass; endpoint responses match DTO schema; date/decimal formatting is consistent across all responses.

- [ ] **17. Create CardController**
      Implement REST endpoints:
      - GET /api/cards/{number} (read; 200 OK or 404)
      - GET /api/accounts/{accountId}/cards (list by account; 200 OK)
      - POST /api/cards (create: {accountId, number, expiryDate}; 201 Created, returns CardDto)
      
      Inject CardService, AccountService, mappers. Validate card number format (12–19 digit range per typical credit card).
      Files: src/main/java/com/carddemo/controller/CardController.java
      Verify: Integration tests (item 23) pass; POST validates card number format; duplicate card number returns 409 Conflict.

- [ ] **18. Create TransactionController**
      Implement REST endpoints:
      - GET /api/transactions/{id} (read; 200 OK or 404)
      - POST /api/transactions (create: {accountId, cardNumber, amount, type, merchantId, merchantName}; 201 Created, returns TransactionDto)
      - POST /api/transactions/settle (settle: {transactionId}; 200 OK, updates status and account balance)
      - GET /api/reports/transactions?asOf=YYYY-MM-DD (report; 200 OK, returns {totalCount, totalAmount, byStatus: {PENDING: count, POSTED: count, SETTLED: count}})
      
      Inject TransactionService, mappers. POST and POST /settle are @Transactional across service layer.
      Files: src/main/java/com/carddemo/controller/TransactionController.java
      Verify: Integration tests (item 23) pass; POST creates PENDING transaction; /settle transitions to SETTLED and updates account balance; /reports aggregates correctly.

- [ ] **19. Create AuthorizationController**
      Implement REST endpoints:
      - POST /api/authorizations (authorize: {accountId, cardNumber, amount, merchantId, merchantName}; 201 Created, returns AuthorizationDto with status APPROVED/DECLINED and declineReasonCode)
      - GET /api/authorizations/{id} (read; 200 OK or 404)
      - GET /api/authorizations/pending (list pending; 200 OK)
      - PUT /api/authorizations/{id}/review (review: {decision: APPROVED|DECLINED, notes}; 200 OK, transitions status per COPAUS0C logic)
      - POST /api/authorizations/expire (expire: {}; 200 OK, calls AuthorizationService.expireAuthorizations())
      
      Inject AuthorizationService, mappers. POST validates available credit per COPAUA0C rules.
      Files: src/main/java/com/carddemo/controller/AuthorizationController.java
      Verify: Integration tests (item 23) pass; POST /authorizations applies decline reason codes correctly; /pending returns only PENDING authorizations; /expire marks expired authorizations as DECLINED.

- [ ] **20. Create Exception Classes & Global Exception Handler**
      Define custom exceptions: AccountNotFoundException (404), InvalidTransactionException (400), AuthorizationExpiredException (409), InsufficientFundsException (409). Implement @RestControllerAdvice class GlobalExceptionHandler with @ExceptionHandler methods returning standardized {code, message, timestamp, path} error response (HTTP status 400/404/409/500 as appropriate). Log exceptions at ERROR level.
      Files: src/main/java/com/carddemo/exception/{AccountNotFoundException, InvalidTransactionException, AuthorizationExpiredException, InsufficientFundsException}.java, src/main/java/com/carddemo/config/GlobalExceptionHandler.java
      Verify: Integration tests (item 23) verify exception handling; error responses contain expected JSON structure and HTTP status codes.

---

### Phase 1.7: Testing

- [ ] **21. Create Repository Tests**
      Write unit tests for all repository finder methods using JUnit 5 and @DataJpaTest annotation. Test scenarios:
      - AccountRepository: findByIdWithTransactions() returns account + transactions; findByActiveStatusTrue() filters correctly; findByIdWithCards() returns account + cards; findByReissueDateBetween() filters by date range.
      - CardRepository: findByAccountId() returns only cards for given account; findByNumber() finds by exact number; findByStatusAndExpiryDateBefore() returns expired cards.
      - TransactionRepository: findByAccountId() and findByDateBetween() filters correctly; findByStatus() returns only matching status.
      - AuthorizationRepository: findByStatusPending() returns only PENDING; findByExpiryDateBefore() returns expired.
      
      Use @Sql to load test data, assert result counts and field values.
      Files: src/test/java/com/carddemo/repository/*RepositoryTest.java
      Verify: `mvn clean test -Dtest=*RepositoryTest` — all tests pass; coverage for repository query methods is >85%.

- [ ] **22. Create Service Unit Tests**
      Write unit tests for AccountService, CardService, TransactionService, AuthorizationService using JUnit 5 and Mockito. Mock repositories and test business logic:
      - AccountService.updateBalance() updates currentBalance and creates Transaction record.
      - CardService.verifyCard() returns true if card is ACTIVE and not expired.
      - TransactionService.postTransaction() creates transaction with status PENDING; settleTransaction() updates to SETTLED and affects account balance.
      - AuthorizationService.authorizeTransaction() correctly calculates available credit (creditLimit - currentBalance), compares to amount, and returns APPROVED/DECLINED with appropriate decline reason code (3100=Card Not Found, 4100=Insufficient Funds, etc. per COPAUA0C rules R00016).
      - AuthorizationService.expireAuthorizations() finds and marks authorizations with expiryDate <= today as DECLINED.
      
      Use @Mock for repositories, @InjectMocks for services, assert method calls and return values. Cover both happy path and exception cases (card not found, insufficient funds, expired authorization, etc.).
      Files: src/test/java/com/carddemo/service/*ServiceTest.java
      Verify: `mvn clean test -Dtest=*ServiceTest` — all tests pass; coverage for service business logic is >85%; exception cases are handled.

- [ ] **23. Create REST Integration Tests**
      Write integration tests for all controllers using JUnit 5, @SpringBootTest, and MockMvc. Test scenarios:
      - AccountController: GET /api/accounts/{id} returns 200 with correct JSON; PUT updates and returns updated account; GET /api/accounts/{id}/transactions filters by date range; POST /api/accounts/{id}/balance creates transaction and updates balance; POST /api/accounts/{id}/reissue creates new card.
      - CardController: GET /api/cards/{number} returns card; POST creates with validation.
      - TransactionController: POST creates PENDING; POST /settle transitions to SETTLED; GET /reports/transactions aggregates by status.
      - AuthorizationController: POST authorizes and returns APPROVED or DECLINED with reason code; GET /authorizations/pending lists pending only; PUT /review transitions status; POST /expire marks as DECLINED.
      - Exception handling: invalid account ID returns 404; insufficient funds returns 409; invalid JSON returns 400.
      
      Use @TestPropertySource to override profile to "test" (H2 in-memory), @Sql to load test data per test class. Verify response status, JSON structure, and field values.
      Files: src/test/java/com/carddemo/controller/*ControllerTest.java
      Verify: `mvn clean test -Dtest=*ControllerTest` — all tests pass; coverage for REST endpoints is >80% (including happy path and error cases).

- [ ] **24. Configure JaCoCo Code Coverage**
      Add JaCoCo Maven plugin to pom.xml with execution goals prepare-agent and report, set minimum coverage threshold to 80% for line and branch coverage. Generate reports during test phase.
      Files: pom.xml (updated)
      Verify: `mvn clean test jacoco:report` — generates target/site/jacoco/index.html; overall coverage >= 80%; any uncovered lines are documented (e.g., fallback logging, exception constructors).

---

### Phase 1.8: Documentation & Deployment

- [ ] **25. Configure Swagger/OpenAPI**
      Add springdoc-openapi dependency, enable Swagger UI at /swagger-ui.html by default. Create config class with @Configuration annotating @Bean for OpenAPI customization: title "CardDemo API", description, version "1.0.0", contact, license. Document each controller method with @Operation(summary, description) and @ApiResponses for 200/400/404/409/500. Add example DTOs with @Schema and @JsonExample annotations.
      Files: src/main/java/com/carddemo/config/SwaggerConfig.java (or annotations in controllers)
      Verify: Start app, navigate to http://localhost:8080/swagger-ui.html; verify all endpoints are listed with correct HTTP methods, paths, and response types; click "Try it out" for GET endpoint and execute successfully.

- [ ] **26. Create API Specification Documentation**
      Document all REST endpoints in docs/api-spec.md: endpoint path, HTTP method, request/response payloads (JSON schema), status codes, example requests/responses. Cross-reference to source COBOL programs (e.g., "POST /authorizations implements COPAUA0C rule R00013: Authorization Approval Decision"). Include authentication/authorization notes (if applicable; Phase 1 assumes none), rate limits (if applicable; Phase 1 assumes none), and error codes and their meanings (3100=Card Not Found, 4100=Insufficient Funds, etc.).
      Files: docs/api-spec.md
      Verify: File documents all 5 controllers (Account, Card, Transaction, Authorization, TransactionType), minimum ≥1 example request/response per endpoint, all error codes mentioned, and cross-references to COBOL programs present.

- [ ] **27. Create Deployment Guide**
      Write docs/deployment-guide.md with instructions: prerequisites (Java 17, Maven 3.8+), clone repo, build (mvn clean package), choose profile (dev or prod), set environment variables (DB_HOST, DB_USER, DB_PASSWORD for prod), run (java -jar target/carddemo-*.jar --spring.profiles.active=prod), test endpoints (curl examples for all CRUD operations). Include MySQL setup (CREATE DATABASE carddemo, run schema.sql). Note H2 in-memory limitations for production.
      Files: docs/deployment-guide.md
      Verify: Follow guide to build and run app on both H2 and MySQL (if available); confirm schema and sample data are initialized; confirm Swagger UI is accessible; confirm one endpoint works (e.g., GET /api/accounts/1).

- [ ] **28. Add Logging & SLF4J Configuration**
      Configure SLF4J with Logback in logback-spring.xml: log level INFO for application, DEBUG for org.springframework.web (HTTP requests), WARN for org.hibernate.SQL (JPA queries). Log business events at INFO (e.g., "Authorization APPROVED for accountId=123"), errors at ERROR with full stack trace. Ensure logs are written to console and optionally to file (logs/carddemo.log).
      Files: src/main/resources/logback-spring.xml
      Verify: Start app, perform API call, verify log output includes timestamp, level, logger name, and message.

---

### Phase 1.9: Build & Verify

- [ ] **29. Final Build and Test**
      Run full Maven build: `mvn clean verify -P dev` (or prod if MySQL available). This executes compile, unit tests, integration tests, JaCoCo coverage report, and package phases. Confirm build succeeds with no errors or critical warnings. Coverage report shows ≥80% overall. JAR artifact is created (target/carddemo-*.jar).
      Files: (all previous)
      Verify: Build output shows "BUILD SUCCESS"; target/carddemo-*.jar exists; target/site/jacoco/index.html shows overall coverage ≥80%.

- [ ] **30. Package as Executable JAR and Test Boot**
      Build fat JAR with spring-boot-maven-plugin (default in Spring Boot pom.xml). Test app startup: `java -jar target/carddemo-*.jar --spring.profiles.active=dev`. Confirm:
      - Tomcat starts on port 8080 (or configured port)
      - Database is initialized (H2 console accessible or MySQL connected)
      - Sample data is loaded (query /api/accounts/1 returns sample account)
      - Swagger UI is available at /swagger-ui.html
      - No startup errors in logs
      
      Files: (all previous)
      Verify: App starts within 10 seconds; log shows "Tomcat started on port 8080"; curl http://localhost:8080/api/accounts/1 returns 200 with account JSON; Swagger UI loads.

---

## Deliverables Summary

### Artifacts
- **pom.xml** – Maven configuration with Spring Boot 3.x, Java 17, all dependencies
- **6 Entity classes** – Account, Card, Customer, Transaction, Authorization, TransactionType
- **6 Repository interfaces** – Finder methods for all entities
- **5 Service classes** – Business logic per gap-assessment
- **5 Controller classes** – REST endpoints per gap-assessment
- **6 DTO classes** – Request/response DTOs with validation
- **6 MapStruct mappers** – Entity ↔ DTO conversions
- **6 Custom exception classes** – Specific to CardDemo domain
- **Global exception handler** – Standardized error responses
- **Database schema** – DDL for H2/MySQL compatibility
- **Sample data** – 5 accounts, 10 cards, 20 transactions, 5 authorizations
- **Test classes** – Repository, service, and controller tests >80% coverage
- **JaCoCo configuration** – Code coverage measurement and reporting
- **Swagger configuration** – OpenAPI spec, UI at /swagger-ui.html
- **Property files** – application.yml, application-dev.yml, application-prod.yml
- **Logback configuration** – SLF4J logging setup
- **Documentation**
  - docs/data-model.md – Entity relationships, PIC mappings, assumptions
  - docs/api-spec.md – REST endpoints, payloads, error codes
  - docs/deployment-guide.md – Build and run instructions

### Success Criteria
✓ Entities persist to H2 (dev) and MySQL (prod) without errors  
✓ All 5 controllers expose 21 REST endpoints (see controllers section)  
✓ Service layer enforces business rules from COBOL programs (e.g., authorization decline reasons, interest calculation)  
✓ All tests pass (repository, service, integration) with >80% code coverage (JaCoCo)  
✓ Swagger UI displays all endpoints with correct methods, paths, and response types  
✓ Sample data loads on app start and can be queried via API  
✓ docs/data-model.md and docs/api-spec.md are complete and accurate  
✓ Executable JAR builds and runs without errors on both H2 and MySQL profiles  

---

## Notes

- **COBOL to Java Mapping:** Every service method includes a comment referencing the source COBOL program(s) (e.g., `// CBACT01C: Account Balance Tracking`).
- **PIC Clause Assumptions:** Documented in Javadoc on entity fields and in docs/data-model.md. Examples: Account.creditLimit = BigDecimal(12,2) from COMP-3 9(9)V99; dates = LocalDate from PIC 9(8) YYYYMMDD; flags = Boolean from PIC X(1) Y/N.
- **Phase 1 Scope:** REST API backend only. React UI, real-time MQ messaging, and advanced features (OAuth, caching, horizontal scaling) are Phase 2+.
- **Testing Philosophy:** Unit tests mock repositories; integration tests use H2 in-memory DB. No external dependencies required to run tests.
- **Deployment:** Supports two profiles: dev (H2 in-memory, fast, single-user) and prod (MySQL, requires external DB, multi-user ready).

---

## Timeline Estimate

Assuming 1 developer:
- Items 1–7 (setup, config, schema): 4–6 hours
- Items 8–15 (repositories, services, DTOs): 12–16 hours
- Items 16–20 (controllers, exceptions): 8–10 hours
- Items 21–24 (testing, coverage): 10–14 hours
- Items 25–28 (documentation, logging, Swagger): 4–6 hours
- Items 29–30 (final build, verification): 2–3 hours

**Total: ~50–65 hours** (~1.5–2 weeks full-time, or 3–4 weeks part-time)

---

## Next Steps (Phase 2)

1. **React Front-End:** Design and build UI screens based on REST API spec (docs/api-spec.md).
2. **MQ Integration:** Replace REST authorization endpoint with real-time MQ message processing.
3. **Advanced Features:** Add OAuth2 security, request/response logging, API rate limiting, horizontal scaling with Spring Cloud.
4. **Database Migration:** If needed, integrate legacy COBOL copybooks for exact field-level data mapping; migrate IMS/DB2 data to MySQL.
5. **Performance Tuning:** Add caching, optimize JPA queries, profile and benchmark against COBOL throughput targets.
6. **Automated Deployment:** CI/CD pipeline (GitHub Actions, GitLab CI, etc.) to build, test, and deploy to cloud (AWS, Azure, etc.).
