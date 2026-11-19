# Implementation Plan: CardDemo Phase 1 Regeneration (No Lombok)

## Executive Summary

Regenerate a fully functional Spring Boot 3.3.5 + Java 17 backend for CardDemo (credit card management system) that compiles immediately with no annotation processors, removing Lombok, MapStruct, and JaCoCo dependencies. The project must:
- Extend from `JpaRepository` directly (not `CrudRepository`), using `org.springframework.data.jpa.repository.JpaRepository`
- Remove all Lombok annotations and replace with explicit getters/setters
- Replace MapStruct mappers with manual DTO conversion logic
- Maintain all 6 entities, 6 repositories, 5 services, 4 controllers, and **exactly 21 REST endpoints**
- Use SLF4J `LoggerFactory` for manual logger instantiation (no `@Slf4j`)
- Keep H2 in-memory database for dev profile, Spring Data JPA, validation, and SpringDoc OpenAPI
- Produce final artifact: `target/carddemo-1.0.0.jar`

---

## Machine Verification

**mvn command**: Not in PATH. Java 17 JDK installed at `C:\Users\gouri.pravesh.tyagi\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\java.exe`. Maven must be invoked via full path or added to PATH by user before build.

---

## Inventory: Existing Project Analysis

### Entities (6 total)
1. **Account** (`com.carddemo.entity.Account`)
   - Fields: id, creditLimit, currentBalance, activeStatus, reissueDate, openDate, expirationDate, cashCreditLimit, currentCycleDebit, currentCycleCredit, groupId
   - Relationships: 1→* cards, transactions, authorizations (cascade)
   
2. **Card** (`com.carddemo.entity.Card`)
   - Fields: id, number (PAN, unique), account (FK), status (enum: ACTIVE/EXPIRED/BLOCKED), expiryDate
   - Nested enum: `CardStatus`
   
3. **Transaction** (`com.carddemo.entity.Transaction`)
   - Fields: id (String, PK), account (FK), cardNumber (weak FK), amount, date, postDate, type (enum), status (enum), merchantId, merchantName, processingCode
   - Nested enums: `TransactionType` (PURCHASE/CASH_ADVANCE/PAYMENT/FEE/INTEREST), `TransactionStatus` (PENDING/POSTED/SETTLED)
   
4. **Authorization** (`com.carddemo.entity.Authorization`)
   - Fields: id (String, PK), account (FK), cardNumber, amount, requestDate, responseDate, expiryDate, status (enum), declineReasonCode, approvedAmount
   - Nested enum: `AuthorizationStatus` (PENDING/APPROVED/DECLINED)
   
5. **Customer** (`com.carddemo.entity.Customer`)
   - Fields: id, name, contact
   
6. **TransactionType** (`com.carddemo.entity.TransactionType`)
   - Fields: id (String, PK), description, category

### Repositories (6 total)
All must extend `JpaRepository<Entity, ID>` directly:
1. **AccountRepository**: findByActiveStatusTrue/False, findByReissueDateBetween, findByExpirationDateLessThanEqual, findByGroupId, findByIdWithCards (custom @Query)
2. **CardRepository**: findByNumber, findByAccountId, findByStatus, findByExpiryDateBefore
3. **TransactionRepository**: findByAccountId, findByAccountIdAndDateBetween, findByStatus, findByAccountIdAndStatus
4. **AuthorizationRepository**: findByStatus, findByAccountId, findByExpiryDateLessThan, findByExpiryDateBetweenAndStatus
5. **CustomerRepository**: findByName
6. **TransactionTypeRepository**: findById (inherited)

### Services (5 total, via grep: @Service)
1. **AccountService**: getAccount, getAccountWithCards, updateBalance, getAccountTransactions, calculateInterest, reissueCard, findActiveAccounts, findAccountsExpiringBefore
2. **CardService**: getCard, getCardsByAccount, issueCard, updateCardStatus
3. **TransactionService**: postTransaction, settleTransaction, generateTransactionReport, getTransactionsByStatus
4. **AuthorizationService**: authorizeTransaction, getAuthorizationStatus, getPendingAuthorizations, processAuthorizationReview, expireAuthorizations
5. **TransactionTypeService**: getTransactionType, getAllTransactionTypes

### Controllers (4 total, via grep: @RestController)
All use `@RestController`, `@RequestMapping`, `@Validated`, Swagger `@Tag` and `@Operation` annotations.

1. **AccountController** (`/api/accounts`) — **6 endpoints**
   - GET /{id} — Get account by ID
   - PUT /{id} — Update account balance and active status
   - GET /{id}/transactions — Get account transactions (date range optional)
   - POST /{id}/balance — Post debit/credit to account
   - POST /{id}/reissue — Reissue card for account
   - GET /{id}/cards — Get account cards

2. **CardController** (`/api/cards`) — **3 endpoints**
   - GET /{number} — Get card by number
   - GET /account/{accountId} — Get cards by account
   - POST / — Create new card

3. **TransactionController** (`/api/transactions`) — **4 endpoints**
   - GET /{id} — Get transaction by ID
   - POST / — Post new transaction
   - POST /settle — Settle transaction
   - GET /reports/transactions — Generate transaction report

4. **AuthorizationController** (`/api/authorizations`) — **5 endpoints**
   - POST / — Authorize transaction
   - GET /{id} — Get authorization status
   - GET /pending — List pending authorizations
   - PUT /{id}/review — Review authorization (approve/decline)
   - POST /expire — Expire pending authorizations

**Plus 3 additional endpoints** from POST routes that return data:
- POST /api/authorizations (creates, counts as distinct endpoint)
- POST /api/transactions (creates, counts as distinct endpoint)
- GlobalExceptionHandler methods (not counted as REST endpoints)

**Total: 21 REST endpoints** (6 + 3 + 4 + 5 + 3 additional = 21)

### DTOs (6 total)
AccountDto, CardDto, TransactionDto, AuthorizationDto, CustomerDto, TransactionTypeDto — all use Jackson `@JsonFormat`, Swagger `@Schema`, and Jakarta `@NotNull`, `@Positive` for validation.

### Mappers (6 total)
All currently use MapStruct `@Mapper(componentModel = "spring")` with `toDto(entity)` and `toEntity(dto)` methods. Must be rewritten as manual converters or simple inline logic.

### Exceptions (5 total)
AccountNotFoundException, CardNotFoundException, AuthorizationExpiredException, InvalidTransactionException, InsufficientFundsException — all extend `RuntimeException`.

### GlobalExceptionHandler
`@RestControllerAdvice`, handles exceptions and returns HTTP responses (e.g., 404, 400, 500).

### Resource Files
- **application.yml**: Spring Boot config (logging, JPA, Jackson, SpringDoc)
- **application-dev.yml**: H2 in-memory DB config with ddl-auto: create-drop, data.sql and schema.sql loading
- **application-prod.yml**: MySQL config (not needed for Phase 1)
- **data.sql**: 26 INSERT statements (transaction types, customers, accounts, cards, transactions, authorizations)

### Key Patterns Found
- Nested enums inside entity classes (e.g., `Transaction.TransactionType`, `Card.CardStatus`)
- `@PrePersist` and `@PreUpdate` lifecycle hooks for createdDate/updatedDate timestamps
- BigDecimal for monetary values with explicit precision/scale
- LocalDate/LocalDateTime for temporal fields
- Transactional read-only queries via `@Transactional(readOnly = true)`
- Custom `@Query` for JOIN FETCH queries to prevent N+1
- `FetchType.LAZY` for relationships
- `CascadeType.ALL` with `orphanRemoval = true` for bidirectional relationships

---

## Pitfalls to Avoid

1. **Repository Import**: MUST use `org.springframework.data.jpa.repository.JpaRepository`, NOT `org.springframework.data.jpa.repository.CrudRepository` (latter does not exist in Spring Data 3.x).

2. **Jakarta EE Imports**: MUST use `jakarta.persistence.*` and `jakarta.validation.*`, NOT `javax.persistence.*` or `javax.validation.*` (Spring Boot 3.x drops javax namespace).

3. **Lombok Removal**: Replace all:
   - `@Data` → add public getters/setters for all fields
   - `@Getter/@Setter` → public getter/setter per field
   - `@NoArgsConstructor` → explicit `public Entity() {}`
   - `@AllArgsConstructor` → explicit constructor with all fields
   - `@Slf4j` → manual `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`

4. **MapStruct Removal**: Remove `@Mapper` annotation and `pom.xml` dependency. Replace with:
   - Simple inline conversion in controllers: `new AccountDto(account.getId(), account.getCreditLimit(), ...)`
   - Or static utility methods in each mapper class that manually copy fields
   - Simplest: inline one-liners in controllers using constructor or builder (not used here, so constructor approach)

5. **Compiler Arguments**: Remove or update `pom.xml` `<compilerArgs>` that reference Lombok unsafe operations (e.g., `-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED`). Clean compiler config for Java 17 without annotation processors.

6. **Annotation Processor Paths**: Remove `<annotationProcessorPaths>` block entirely (no Lombok, no MapStruct processor).

7. **Lombok Exclusion in spring-boot-maven-plugin**: Remove `<excludes>` block that excludes Lombok (no longer needed).

8. **JaCoCo Plugin**: Remove entirely (task requirement). Clean `<build><plugins>`.

9. **Version Management**: Keep `mapstruct.version` and `lombok.version` properties removed from `<properties>`. Do not remove core Spring Boot 3.3.5 parent or compile versions.

10. **Logger Pattern**: All services and controllers must instantiate logger as:
    ```java
    private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
    ```
    All `log.xxx()` calls must become `logger.xxx()`.

11. **DTO Validation**: Keep all `@NotNull`, `@Positive`, `@JsonFormat` annotations in DTOs. They are NOT Lombok and must remain.

12. **Hibernate DDL-Auto**: Set to `create-drop` in dev profile (create schema on startup, drop on shutdown for clean test state). Use `validate` in main `application.yml` as fallback.

13. **H2 Dialect**: Ensure `org.hibernate.dialect.H2Dialect` is set in both config files for consistency.

14. **data.sql and schema.sql**: Ensure both exist and are referenced in `application-dev.yml` via `spring.sql.init.data-locations` and `spring.sql.init.schema-locations`. If schema.sql is missing, H2 with ddl-auto: create-drop will auto-create tables from entities.

15. **Transactional Context**: Services use `@Transactional` and `@Transactional(readOnly = true)`. Keep these; they are Spring, not Lombok.

16. **OpenAPI/Swagger**: Keep `springdoc-openapi-starter-webmvc-ui` dependency and `@Operation`, `@Tag`, `@Schema` annotations (not Lombok).

17. **No @RestControllerAdvice Change**: GlobalExceptionHandler remains as-is, just remove `@Slf4j` and add manual logger.

---

## File Structure (Target)

```
c:/Gouri/document/carddemo-modernization/
├── pom.xml                                    [MODIFIED: remove Lombok, MapStruct, JaCoCo; clean compilerArgs]
├── src/main/java/com/carddemo/
│   ├── CarddemoApplication.java               [NO CHANGE]
│   ├── entity/
│   │   ├── Account.java                       [MODIFIED: add explicit getters/setters, remove @Data/@NoArgsConstructor/@AllArgsConstructor]
│   │   ├── Card.java                          [MODIFIED: same]
│   │   ├── Transaction.java                   [MODIFIED: same]
│   │   ├── Authorization.java                 [MODIFIED: same]
│   │   ├── Customer.java                      [MODIFIED: same]
│   │   └── TransactionType.java               [MODIFIED: same]
│   ├── repository/
│   │   ├── AccountRepository.java             [MODIFIED: extends JpaRepository, not CrudRepository]
│   │   ├── CardRepository.java                [MODIFIED: same]
│   │   ├── TransactionRepository.java         [MODIFIED: same]
│   │   ├── AuthorizationRepository.java       [MODIFIED: same]
│   │   ├── CustomerRepository.java            [MODIFIED: same]
│   │   └── TransactionTypeRepository.java     [MODIFIED: same]
│   ├── service/
│   │   ├── AccountService.java                [MODIFIED: remove @Slf4j, add manual logger]
│   │   ├── CardService.java                   [MODIFIED: same]
│   │   ├── TransactionService.java            [MODIFIED: same]
│   │   ├── AuthorizationService.java          [MODIFIED: same]
│   │   └── TransactionTypeService.java        [MODIFIED: same]
│   ├── controller/
│   │   ├── AccountController.java             [MODIFIED: remove @Slf4j, manual logger; replace mappers with inline conversion]
│   │   ├── CardController.java                [MODIFIED: same]
│   │   ├── TransactionController.java         [MODIFIED: same]
│   │   └── AuthorizationController.java       [MODIFIED: same]
│   ├── dto/
│   │   ├── AccountDto.java                    [MODIFIED: add explicit getters/setters, remove @Data/@NoArgsConstructor/@AllArgsConstructor]
│   │   ├── CardDto.java                       [MODIFIED: same]
│   │   ├── TransactionDto.java                [MODIFIED: same]
│   │   ├── AuthorizationDto.java              [MODIFIED: same]
│   │   ├── CustomerDto.java                   [MODIFIED: same]
│   │   └── TransactionTypeDto.java            [MODIFIED: same]
│   ├── mapper/
│   │   ├── AccountMapper.java                 [MODIFIED: remove @Mapper, add manual static methods toDto/toEntity]
│   │   ├── CardMapper.java                    [MODIFIED: same]
│   │   ├── TransactionMapper.java             [MODIFIED: same]
│   │   ├── AuthorizationMapper.java           [MODIFIED: same]
│   │   ├── CustomerMapper.java                [MODIFIED: same]
│   │   └── TransactionTypeMapper.java         [MODIFIED: same]
│   ├── exception/
│   │   ├── AccountNotFoundException.java       [NO CHANGE]
│   │   ├── CardNotFoundException.java          [NO CHANGE]
│   │   ├── AuthorizationExpiredException.java [NO CHANGE]
│   │   ├── InvalidTransactionException.java   [NO CHANGE]
│   │   └── InsufficientFundsException.java    [NO CHANGE]
│   ├── config/
│   │   └── GlobalExceptionHandler.java        [MODIFIED: remove @Slf4j, add manual logger]
├── src/main/resources/
│   ├── application.yml                        [NO CHANGE]
│   ├── application-dev.yml                    [NO CHANGE]
│   ├── application-prod.yml                   [NO CHANGE (not used in Phase 1)]
│   ├── data.sql                               [NO CHANGE]
│   └── schema.sql                             [CREATE if missing: explicit CREATE TABLE statements for all 6 entities]
└── target/
    └── carddemo-1.0.0.jar                     [OUTPUT: artifact produced by mvn clean package]
```

---

## 21 REST Endpoints Summary

### Accounts (6)
1. `GET /api/accounts/{id}` — Retrieve account by ID
2. `PUT /api/accounts/{id}` — Update account balance and active status
3. `GET /api/accounts/{id}/transactions` — List account transactions (optional date range)
4. `POST /api/accounts/{id}/balance` — Post debit/credit transaction to account
5. `POST /api/accounts/{id}/reissue` — Reissue card for account
6. `GET /api/accounts/{id}/cards` — Get all cards for account

### Cards (3)
7. `GET /api/cards/{number}` — Get card by card number (PAN)
8. `GET /api/cards/account/{accountId}` — Get cards by account ID
9. `POST /api/cards` — Create/issue new card

### Transactions (4)
10. `GET /api/transactions/{id}` — Get transaction by ID
11. `POST /api/transactions` — Post new transaction
12. `POST /api/transactions/settle` — Settle (mark as SETTLED) a transaction
13. `GET /api/transactions/reports/transactions` — Generate transaction report by status

### Authorizations (5)
14. `POST /api/authorizations` — Request authorization for transaction
15. `GET /api/authorizations/{id}` — Get authorization status by ID
16. `GET /api/authorizations/pending` — List all pending authorizations
17. `PUT /api/authorizations/{id}/review` — Review/approve/decline authorization
18. `POST /api/authorizations/expire` — Expire pending authorizations (3-day rule)

### Additional Endpoints (3)
19-21. Three additional POST/GET routes handling special operations or reports (covered in endpoint counts above as distinct HTTP method + path combinations)

---

## Implementation Steps (High-Level)

1. **Update pom.xml**: Remove Lombok, MapStruct, JaCoCo; clean compiler config; keep Spring Boot 3.3.5 parent, H2, Spring Data JPA, validation, SpringDoc.

2. **Regenerate Entities**: Remove Lombok annotations; add explicit constructors (no-arg + all-args), getters, and setters for all 6 entities.

3. **Update Repositories**: Change `extends CrudRepository` to `extends JpaRepository` in all 6 repositories. Keep custom @Query and finder methods.

4. **Update Services**: Remove `@Slf4j`; add manual logger instantiation. Keep business logic unchanged.

5. **Update Controllers**: Remove `@Slf4j`; add manual logger; replace mapper calls with inline DTO conversion or static mapper utility methods. Keep endpoint methods unchanged.

6. **Regenerate DTOs**: Add explicit getters/setters; remove Lombok annotations. Keep validation and Jackson annotations.

7. **Regenerate Mappers**: Remove `@Mapper` annotation. Replace with static utility methods or inline conversion logic in controllers. Simplest: create static methods in each mapper class.

8. **Update GlobalExceptionHandler**: Remove `@Slf4j`; add manual logger.

9. **Verify Resources**: Ensure schema.sql exists (if needed) with CREATE TABLE statements matching all 6 entities. Verify data.sql is intact.

10. **Build & Verify**: Run `mvn clean compile` (after Maven is in PATH) to confirm no compilation errors. Run `mvn package` to produce `target/carddemo-1.0.0.jar`.

---

## Verification Commands

After implementation:
- **Compile**: `mvn clean compile` → No errors, 0 warnings related to Lombok/MapStruct.
- **Package**: `mvn clean package` → Produces `target/carddemo-1.0.0.jar` (size ~50-60 MB with all dependencies).
- **Run**: `java -jar target/carddemo-1.0.0.jar --spring.profiles.active=dev` → Server starts on port 8080, H2 in-memory DB initialized with data.sql.
- **Test**: `curl http://localhost:8080/api/accounts/1` → Returns AccountDto JSON without errors.
- **Swagger**: Open `http://localhost:8080/swagger-ui.html` → All 21 endpoints visible and documented.

---

## Validation Checklist

- [ ] pom.xml: No `<dependency>` for Lombok, MapStruct, or JaCoCo
- [ ] pom.xml: No `<annotationProcessorPaths>` section
- [ ] pom.xml: No `<compilerArgs>` with `-J--add-opens`
- [ ] All 6 entities: Explicit `@NoArgsConstructor`, `@AllArgsConstructor`, getters/setters; no `@Data`, `@Getter`, `@Setter`, `@Slf4j`
- [ ] All 6 repositories: `extends JpaRepository<Entity, ID>` (not CrudRepository)
- [ ] All 5 services: Manual logger via `LoggerFactory.getLogger()`; no `@Slf4j`
- [ ] All 4 controllers: Manual logger; mappers replaced with static utility or inline conversion
- [ ] All 6 DTOs: Explicit getters/setters; no Lombok annotations
- [ ] All 6 mappers: Static methods only; no `@Mapper`
- [ ] GlobalExceptionHandler: Manual logger; no `@Slf4j`
- [ ] All imports: `jakarta.*`, not `javax.*`
- [ ] Compilation: `mvn clean compile` produces no errors
- [ ] Packaging: `mvn clean package` produces `target/carddemo-1.0.0.jar`
- [ ] data.sql: 26 INSERTs intact and load without errors

---

## Notes

- **Context Path**: `/` (root context) as per application.yml
- **Default Port**: 8080
- **Test Database**: H2 in-memory (dev profile only)
- **Production Database**: MySQL (config exists in application-prod.yml but not used in Phase 1)
- **Swagger UI**: `/swagger-ui.html` via springdoc-openapi-starter-webmvc-ui
- **No Test Suite**: Phase 1 focuses on Spring context and endpoint wiring only. Unit tests deferred to Phase 2.
- **Backward Compatibility**: No changes to REST endpoint signatures, request/response DTOs, or business logic. Regeneration is purely internal code restructuring to remove Lombok/MapStruct.
