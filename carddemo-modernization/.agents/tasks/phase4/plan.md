# Phase 4 - Authorization Management Implementation Plan

## Overview
Implement Phase 4: Authorization Management for CardDemo backend, following the exact pattern and structure of Phase 3 (Transaction Management). The implementation includes entity model updates, business logic with 5 core validations for automatic approval, 6 REST APIs, and 11 comprehensive JUnit5 integration tests.

---

## Implementation Items

### 1. Update Card Entity with Daily and Transaction Limits
Add two new fields to `com/carddemo/entity/Card.java` to support authorization validation rules:
- `dailyLimit` (DECIMAL 10,2, default 5000.00): Maximum amount card can spend in a day
- `maxTransactionAmount` (DECIMAL 10,2, default 2000.00): Maximum single transaction amount

These fields are crucial for validations #4 and #5 in AuthorizationService.

**Files:** `src/main/java/com/carddemo/entity/Card.java`

**Verify:** 
```bash
mvn clean compile
```
Compilation succeeds with no errors. Inspect Card.java in IDE to confirm both fields are present with correct types and defaults.

---

### 2. Verify Authorization Entity Structure
Confirm the Authorization entity (`src/main/java/com/carddemo/entity/Authorization.java`) is correctly set up with:
- All required fields: id, account (FK), cardNumber, merchantId, merchantName, amount, requestDate, responseDate, expiryDate, status (ENUM), declineReasonCode, approvedAmount, createdDate, updatedDate
- Correct JPA annotations (OneToMany from Account, proper column types, indexes)
- AuthorizationStatus enum with PENDING, APPROVED, DECLINED values
- PrePersist/PreUpdate lifecycle hooks

No changes needed; entity already exists and is properly structured per Phase 3 pattern.

**Files:** `src/main/java/com/carddemo/entity/Authorization.java` (read-only verification)

**Verify:**
```bash
mvn clean compile
```
Compilation succeeds; no errors in Authorization entity.

---

### 3. Verify AuthorizationRepository Query Methods
Confirm `src/main/java/com/carddemo/repository/AuthorizationRepository.java` has all required finder methods:
- `findByAccountId(Long accountId)`: List all auths for account
- `findByCardNumber(String cardNumber)`: List auths for card
- `findByStatusPending()`: List pending auths
- `findByAccountIdAndStatus(Long accountId, AuthorizationStatus status)`: Filtered lookup
- `findByStatus(AuthorizationStatus status)`: Status-based filter
- `findByExpiryDateBefore(LocalDate date)`: Expiry-based cleanup
- `countPendingApprovedByAccountId(Long accountId)`: Count for available credit calc

No changes needed; repository is properly structured.

**Files:** `src/main/java/com/carddemo/repository/AuthorizationRepository.java` (read-only verification)

**Verify:**
```bash
mvn clean compile
```
Compilation succeeds; all repository methods are present and syntactically correct.

---

### 4. Implement Authorization Business Logic with 5 Core Validations
Refactor and enhance `src/main/java/com/carddemo/service/AuthorizationService.java` to implement the exact 5 validations per Phase 4 spec:

**Validation Logic (in order):**
1. **Card Exists and Is ACTIVE**: Query Card by cardNumber; check status == ACTIVE (not EXPIRED, not BLOCKED)
2. **Card Not Expired**: Check card.expiryDate > TODAY (using LocalDate.now())
3. **Sufficient Balance**: Check card's account availableBalance >= authorizationAmount (where availableBalance = creditLimit - currentBalance)
4. **Daily Spending Limit**: Calculate dailySpent by summing APPROVED authorizations for cardNumber on TODAY; verify dailySpent + amount <= card.dailyLimit (default 5000.00)
5. **Single Transaction Limit**: Verify amount <= card.maxTransactionAmount (default 2000.00)

**Key Implementation Details:**
- Validation method: `validateAndAuthorize(card, amount, dailySpent)` returns a tuple of (status, declineReasonCode) 
- Automatic approval decision: All 5 validations pass → APPROVED (approvedAmount = amount); ANY validation fails → DECLINED (approvedAmount = null)
- Decline reason codes: "INVALID_CARD", "CARD_EXPIRED", "INSUFFICIENT_BALANCE", "DAILY_LIMIT_EXCEEDED", "TRANSACTION_LIMIT_EXCEEDED" (custom codes per Phase 4, not COBOL legacy codes)
- Core method: `createAuthorizationWithAutoApproval(Long accountId, Long cardId, String merchantId, BigDecimal amount, String merchantName)` — performs lookups, validations, and saves Authorization entity

**Methods to Update or Create:**
- `createAuthorizationWithAutoApproval()`: Main entry point; replaces old `authorizeTransaction()` logic
- `validateAndAuthorize()`: Private helper; encapsulates all 5 checks
- `getAuthorizationById(String authorizationId)`: Simple getter (already exists)
- `approveAuthorizationManually(String authorizationId)`: Manual approval by admin (new)
- `declineAuthorizationManually(String authorizationId, String reason)`: Manual decline (new)
- `listAuthorizations(AuthorizationStatus status, Long cardId, LocalDate fromDate, LocalDate toDate)`: Filtered listing (new)
- `getAuthorizationStatistics()`: Summary report (new)

**Files:** `src/main/java/com/carddemo/service/AuthorizationService.java`

**Verify:**
```bash
mvn clean compile
```
Compilation succeeds; all new methods compile without errors.

---

### 5. Implement AuthorizationController with 6 REST Endpoints
Create/update `src/main/java/com/carddemo/controller/AuthorizationController.java` following the TransactionController pattern exactly:

**Endpoints (6 total):**

1. **POST /api/authorizations**
   - Request body: AuthorizationDto (accountId, cardNumber/cardId, amount, merchantId, merchantName)
   - Logic: Call `authorizationService.createAuthorizationWithAutoApproval()`
   - Response: 201 CREATED with AuthorizationDto (includes id, status, declineReasonCode if declined)
   - Declined authorizations return 201 (not 422 per spec — 422 is for manual operations)

2. **GET /api/authorizations/{id}**
   - Path param: authorization ID
   - Logic: Call `authorizationService.getAuthorizationById(id)`
   - Response: 200 OK with AuthorizationDto; 404 NOT FOUND if not found

3. **PUT /api/authorizations/{id}/approve**
   - Path param: authorization ID
   - Admin-only endpoint (optional permission check can be added later)
   - Logic: Call `authorizationService.approveAuthorizationManually(id)`
   - Response: 200 OK with updated AuthorizationDto; 404 if not found; 422 UNPROCESSABLE_ENTITY if already finalized

4. **PUT /api/authorizations/{id}/decline**
   - Path param: authorization ID
   - Request body: Map with `reason` field
   - Admin-only endpoint
   - Logic: Call `authorizationService.declineAuthorizationManually(id, reason)`
   - Response: 200 OK with updated AuthorizationDto; 404 if not found; 422 if already finalized

5. **GET /api/authorizations**
   - Query params (all optional): status (PENDING/APPROVED/DECLINED), cardId (Long), fromDate (LocalDate), toDate (LocalDate)
   - Logic: Call `authorizationService.listAuthorizations(status, cardId, fromDate, toDate)`
   - Response: 200 OK with List<AuthorizationDto>

6. **GET /api/authorizations/reports/summary**
   - No required params
   - Logic: Call `authorizationService.getAuthorizationStatistics()`
   - Response: 200 OK with Map containing: totalCount, approvedCount, declinedCount, pendingCount, averageAmount, totalAmount

**Common Patterns (from Phase 3):**
- Use @Tag(name = "Authorizations") for Swagger grouping
- Use @Operation, @Parameter annotations for Swagger docs
- Use @Valid on request DTOs
- Return AuthorizationDto via AuthorizationMapper.toDto(entity)
- Error handling: RuntimeException("...not found") → 404; IllegalStateException → 422
- Use TestRestTemplate in tests (matching Phase 3 test pattern)

**Files:** `src/main/java/com/carddemo/controller/AuthorizationController.java`

**Verify:**
```bash
mvn clean compile
```
Compilation succeeds; all 6 endpoints compile without errors.

---

### 6. Verify/Update AuthorizationDto Structure
Confirm `src/main/java/com/carddemo/dto/AuthorizationDto.java` has correct structure:
- REQUEST fields: accountId, cardNumber, amount, merchantId, merchantName (all @NotNull/@NotBlank/@Positive)
- RESPONSE fields (READ_ONLY): id, requestDate, responseDate, expiryDate, status, declineReasonCode, approvedAmount
- Swagger @Schema annotations on all fields
- Getters/setters for all fields

No changes needed; DTO is already properly structured and annotated.

**Files:** `src/main/java/com/carddemo/dto/AuthorizationDto.java` (read-only verification)

**Verify:**
```bash
mvn clean compile
```
Compilation succeeds; DTO is valid and properly annotated.

---

### 7. Verify AuthorizationMapper Implementation
Confirm `src/main/java/com/carddemo/mapper/AuthorizationMapper.java` has:
- `toDto(Authorization entity)`: Converts entity to DTO, extracting account.id as accountId
- `toEntity(AuthorizationDto dto)`: Converts DTO to entity (without account; callers set it)

No changes needed; mapper is already correctly implemented per Phase 3 pattern.

**Files:** `src/main/java/com/carddemo/mapper/AuthorizationMapper.java` (read-only verification)

**Verify:**
```bash
mvn clean compile
```
Compilation succeeds; mapper methods are correct.

---

### 8. Create Comprehensive JUnit5 Integration Tests (11 Total)
Create `src/test/java/com/carddemo/controller/AuthorizationControllerTest.java` with 11 integration tests using TestRestTemplate (exact pattern from Phase 3):

**Test Suite Setup:**
- @SpringBootTest with RANDOM_PORT
- @ActiveProfiles("test")
- Use TestRestTemplate for HTTP calls
- BeforeEach: Create 3 test cards with different scenarios:
  - Card 1: ACTIVE, not expired, sufficient balance, with limits (dailyLimit=5000, maxTransaction=2000)
  - Card 2: EXPIRED status
  - Card 3: BLOCKED status
- Baseline test data: 3 accounts, 3 cards, 20 authorization requests pre-configured

**11 Test Cases:**

1. **test1_CreateAuthorizationCardActiveAndSufficientBalance**
   - POST /api/authorizations with valid card, sufficient balance
   - Expected: 201 CREATED, status=APPROVED, approvedAmount=amount, declineReasonCode=null

2. **test2_DeclineAuthorizationCardInactive**
   - POST /api/authorizations with BLOCKED card
   - Expected: 201 CREATED (not 422), status=DECLINED, declineReasonCode="INVALID_CARD" (or "CARD_BLOCKED"), approvedAmount=null

3. **test3_DeclineAuthorizationCardExpired**
   - POST /api/authorizations with card expiryDate < TODAY
   - Expected: 201 CREATED, status=DECLINED, declineReasonCode="CARD_EXPIRED", approvedAmount=null

4. **test4_DeclineAuthorizationInsufficientBalance**
   - POST /api/authorizations with amount > availableBalance
   - Expected: 201 CREATED, status=DECLINED, declineReasonCode="INSUFFICIENT_BALANCE", approvedAmount=null

5. **test5_DeclineAuthorizationDailyLimitExceeded**
   - POST /api/authorizations where dailySpent + amount > dailyLimit (e.g., dailyLimit=5000, daily spent=4500, request=600)
   - Expected: 201 CREATED, status=DECLINED, declineReasonCode="DAILY_LIMIT_EXCEEDED", approvedAmount=null

6. **test6_DeclineAuthorizationSingleTransactionLimitExceeded**
   - POST /api/authorizations with amount > maxTransactionAmount (e.g., maxTransaction=2000, request=2500)
   - Expected: 201 CREATED, status=DECLINED, declineReasonCode="TRANSACTION_LIMIT_EXCEEDED", approvedAmount=null

7. **test7_GetAuthorizationDetails**
   - Create an authorization, then GET /api/authorizations/{id}
   - Expected: 200 OK with full AuthorizationDto matching the created authorization

8. **test8_ManualApproveAuthorization**
   - Create authorization in PENDING or DECLINED state, then PUT /api/authorizations/{id}/approve
   - Expected: 200 OK, status=APPROVED

9. **test9_ManualDeclineAuthorization**
   - Create authorization, then PUT /api/authorizations/{id}/decline with reason="Manual review"
   - Expected: 200 OK, status=DECLINED

10. **test10_ListAuthorizationsWithFilters**
    - Create multiple authorizations (APPROVED, DECLINED, PENDING statuses)
    - Call GET /api/authorizations?status=APPROVED&cardId=1
    - Expected: 200 OK, List contains only filtered results

11. **test11_GetAuthorizationStatisticsSummary**
    - Call GET /api/authorizations/reports/summary
    - Expected: 200 OK with Map containing totalCount, approvedCount, declinedCount, pendingCount, averageAmount, totalAmount

**Test Patterns (from Phase 3):**
- Use RestTemplate.postForEntity(), getForEntity(), exchange() (PUT)
- Assert on HTTP status codes (201, 200, 404, 422)
- Parse response body as Map or DTO
- Print test status to console with ✓ prefix for readability

**Files:** `src/test/java/com/carddemo/controller/AuthorizationControllerTest.java`

**Verify:**
```bash
mvn test -Dtest=AuthorizationControllerTest
```
All 11 tests pass with no failures or errors.

---

### 9. Build, Compile, and Run Full Test Suite
Verify the entire implementation compiles and all tests pass:

**Files:** All implementation files from items 1-8

**Verify:**
```bash
mvn clean compile
mvn clean test
mvn clean package
```
- `mvn clean compile`: No compilation errors
- `mvn clean test`: All 11 AuthorizationControllerTest tests pass; no test failures
- `mvn clean package`: Build succeeds; JAR is created

---

### 10. Verify Swagger API Documentation Auto-Generation
Confirm Swagger/OpenAPI automatically documents all 6 endpoints:

**Files:** (read-only verification; depends on items 1-8)

**Verify:**
```bash
mvn spring-boot:run
# In browser: http://localhost:8080/swagger-ui.html
```
- All 6 endpoints appear in Swagger UI under "Authorizations" tag
- Each endpoint has correct request/response schemas
- Parameters are documented
- HTTP status codes are shown (201, 200, 404, 422)

---

## Key Design Decisions

1. **Automatic Approval Logic**: Five validations implemented in AuthorizationService as sequential checks; if ANY fail, the entire authorization is DECLINED with a specific reason code. No partial approval or approval with caveats.

2. **Decline Reason Codes**: Custom codes (INVALID_CARD, CARD_EXPIRED, INSUFFICIENT_BALANCE, DAILY_LIMIT_EXCEEDED, TRANSACTION_LIMIT_EXCEEDED) replace legacy COBOL codes for Phase 4; forward-compatible and human-readable.

3. **Card Lookup**: By cardNumber (String weak FK) rather than cardId; matching real-world authorization flow where merchant provides card number (or masked PAN).

4. **Daily Spending Tracking**: Calculated at request time by summing APPROVED authorizations for the card on TODAY; no persistent dailySpent field on Card. This allows dynamic recalculation and reverse of declined authorizations.

5. **Manual Approval Endpoints**: Separate PUT endpoints for /approve and /decline; allows admin review workflows; return 200 OK (not 422) on success; return 422 UNPROCESSABLE_ENTITY if authorization is already APPROVED or DECLINED (no state transition).

6. **API Response Status Codes**:
   - 201 CREATED for POST /api/authorizations (regardless of approval status)
   - 200 OK for all successful GET and PUT operations
   - 404 NOT FOUND for non-existent resources
   - 422 UNPROCESSABLE_ENTITY for invalid state transitions or business rule violations

7. **Test Data**: 3 test cards with varied scenarios; 20+ authorization requests covering all 6 decline scenarios plus approvals; ensures comprehensive coverage of all validation paths.

8. **Pattern Consistency**: Exact replication of Phase 3 (Transaction Management) patterns: TestRestTemplate, DTO mappings, controller structure, error handling, Swagger annotations.

---

## Completion Criteria

All 11 tests pass with 100% success rate:
- ✓ Test 1: Automatic approval for valid card and sufficient balance
- ✓ Test 2: Decline for inactive/blocked card
- ✓ Test 3: Decline for expired card
- ✓ Test 4: Decline for insufficient balance
- ✓ Test 5: Decline for daily limit exceeded
- ✓ Test 6: Decline for single transaction limit exceeded
- ✓ Test 7: Successful GET authorization details
- ✓ Test 8: Manual approval workflow
- ✓ Test 9: Manual decline workflow
- ✓ Test 10: List with filtering
- ✓ Test 11: Statistics summary report

Build succeeds: `mvn clean package` creates executable JAR with no errors.

Swagger documentation: All 6 endpoints auto-documented at /swagger-ui.html with correct schemas and status codes.

---

## Files Summary

| File | Type | Action |
|------|------|--------|
| `src/main/java/com/carddemo/entity/Card.java` | Entity | Modify (add dailyLimit, maxTransactionAmount) |
| `src/main/java/com/carddemo/entity/Authorization.java` | Entity | Verify only (already correct) |
| `src/main/java/com/carddemo/repository/AuthorizationRepository.java` | Repository | Verify only (already correct) |
| `src/main/java/com/carddemo/service/AuthorizationService.java` | Service | Modify (5 validations, new methods) |
| `src/main/java/com/carddemo/controller/AuthorizationController.java` | Controller | Create/Modify (6 endpoints) |
| `src/main/java/com/carddemo/dto/AuthorizationDto.java` | DTO | Verify only (already correct) |
| `src/main/java/com/carddemo/mapper/AuthorizationMapper.java` | Mapper | Verify only (already correct) |
| `src/test/java/com/carddemo/controller/AuthorizationControllerTest.java` | Test | Create (11 integration tests) |
| `src/main/resources/application.yml` | Config | Verify only (H2 create-drop mode correct for tests) |
| `pom.xml` | Build | Verify only (all dependencies present) |

