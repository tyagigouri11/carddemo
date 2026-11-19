package com.carddemo.controller;

import com.carddemo.dto.TransactionDto;
import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 - Transaction Management Integration Tests
 * All 11 test cases for transaction creation, retrieval, settlement, and reporting
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Phase 3 - Transaction Management Tests")
class TransactionControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    private String transactionUrl;
    private String transactionIdUrl;
    private String settleUrl;
    private String reportUrl;
    private String txnId1;
    private String txnId2;
    private String txnId3;

    @BeforeEach
    void setUp() {
        transactionUrl = "/api/transactions";
        transactionIdUrl = "/api/transactions/{id}";
        settleUrl = "/api/transactions/settle";
        reportUrl = "/api/transactions/reports/transactions";

        // Create test accounts if they don't exist
        for (long i = 1; i <= 5; i++) {
            if (!accountRepository.existsById(i)) {
                Account account = new Account();
                account.setId(i);
                account.setCreditLimit(new BigDecimal("5000.00"));
                account.setCurrentBalance(new BigDecimal("2500.00"));
                account.setActiveStatus(true);
                account.setOpenDate(LocalDate.now());
                account.setExpirationDate(LocalDate.now().plusYears(5));
                accountRepository.save(account);
            }
        }
    }

    // ========== TEST 1: Create PURCHASE Transaction ==========
    @Test
    @DisplayName("Test 1: Create PURCHASE transaction → 201 Created")
    void test1_createPurchaseTransaction() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(1L);
        dto.setAmount(new BigDecimal("99.99"));
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.PURCHASE);
        dto.setMerchantId("M001");
        dto.setMerchantName("Coffee Shop");

        ResponseEntity<Map> response = restTemplate.postForEntity(transactionUrl, dto, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 Created");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        txnId1 = (String) response.getBody().get("id");
        assertNotNull(txnId1, "Transaction ID should be generated");
        assertEquals(99.99, ((Number) response.getBody().get("amount")).doubleValue(), "Amount should match");
        System.out.println("✓ Test 1 PASSED: Transaction created with ID: " + txnId1);
    }

    // ========== TEST 2: Create PAYMENT Transaction ==========
    @Test
    @DisplayName("Test 2: Create PAYMENT transaction → 201 Created")
    void test2_createPaymentTransaction() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(2L);
        dto.setAmount(new BigDecimal("500.00"));
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.PAYMENT);
        dto.setMerchantId("M002");
        dto.setMerchantName("Payment Portal");

        ResponseEntity<Map> response = restTemplate.postForEntity(transactionUrl, dto, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 Created");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        txnId2 = (String) response.getBody().get("id");
        assertNotNull(txnId2, "Transaction ID should be generated");
        assertEquals(500.00, ((Number) response.getBody().get("amount")).doubleValue(), "Amount should match");
        System.out.println("✓ Test 2 PASSED: Transaction created with ID: " + txnId2);
    }

    // ========== TEST 3: Create CASH_ADVANCE Transaction ==========
    @Test
    @DisplayName("Test 3: Create CASH_ADVANCE transaction → 201 Created")
    void test3_createCashAdvanceTransaction() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(3L);
        dto.setAmount(new BigDecimal("200.00"));
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.CASH_ADVANCE);
        dto.setMerchantId("M003");
        dto.setMerchantName("ATM Withdrawal");

        ResponseEntity<Map> response = restTemplate.postForEntity(transactionUrl, dto, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 Created");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        txnId3 = (String) response.getBody().get("id");
        assertNotNull(txnId3, "Transaction ID should be generated");
        assertEquals(200.00, ((Number) response.getBody().get("amount")).doubleValue(), "Amount should match");
        System.out.println("✓ Test 3 PASSED: Transaction created with ID: " + txnId3);
    }

    // ========== TEST 4: Reject Negative Amount ==========
    @Test
    @DisplayName("Test 4: Reject transaction with negative amount → 400 Bad Request")
    void test4_rejectNegativeAmount() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(1L);
        dto.setAmount(new BigDecimal("-50.00"));  // Negative amount
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.PURCHASE);
        dto.setMerchantId("M004");
        dto.setMerchantName("Negative Test");

        ResponseEntity<Map> response = restTemplate.postForEntity(transactionUrl, dto, Map.class);

        assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST || 
                   response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY,
                "Should reject negative amount with 400 or 422");
        System.out.println("✓ Test 4 PASSED: Correctly rejected negative amount (Status: " + response.getStatusCode() + ")");
    }

    // ========== TEST 5: Reject Missing Required Fields ==========
    @Test
    @DisplayName("Test 5: Reject transaction with missing fields → 400 Bad Request")
    void test5_rejectMissingFields() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("accountId", 1L);
        // Missing: amount, date, status, type

        ResponseEntity<Map> response = restTemplate.postForEntity(transactionUrl, dto, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Should reject missing required fields");
        System.out.println("✓ Test 5 PASSED: Correctly rejected missing fields");
    }

    // ========== TEST 6: Get Valid Transaction ==========
    @Test
    @DisplayName("Test 6: Get valid transaction → 200 OK")
    void test6_getValidTransaction() {
        // First create a transaction
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(1L);
        dto.setAmount(new BigDecimal("75.50"));
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.PURCHASE);
        dto.setMerchantId("M006");
        dto.setMerchantName("Test Store");

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(transactionUrl, dto, Map.class);
        String txnId = (String) createResponse.getBody().get("id");

        // Now retrieve it
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(transactionUrl + "/" + txnId, Map.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "Should return 200 OK");
        assertNotNull(getResponse.getBody(), "Response body should not be null");
        assertEquals(txnId, getResponse.getBody().get("id"), "Transaction ID should match");
        System.out.println("✓ Test 6 PASSED: Transaction retrieved: " + txnId);
    }

    // ========== TEST 7: Get Non-existent Transaction Returns 404 ==========
    @Test
    @DisplayName("Test 7: Get non-existent transaction → 404 Not Found")
    void test7_getNonexistentTransaction() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                transactionUrl + "/TXN_INVALID_12345", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Should return 404 for non-existent transaction");
        System.out.println("✓ Test 7 PASSED: Correctly returned 404 for non-existent transaction");
    }

    // ========== TEST 8: Settle Valid Transaction ==========
    @Test
    @DisplayName("Test 8: Settle valid transaction → 200 OK with SETTLED status")
    void test8_settleValidTransaction() {
        // Create a transaction first
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(1L);
        dto.setAmount(new BigDecimal("150.00"));
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.PURCHASE);
        dto.setMerchantId("M008");
        dto.setMerchantName("Settlement Test");

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(transactionUrl, dto, Map.class);
        String txnId = (String) createResponse.getBody().get("id");

        // Settle it
        Map<String, Object> settleRequest = new LinkedHashMap<>();
        settleRequest.put("transactionId", txnId);

        ResponseEntity<Map> settleResponse = restTemplate.postForEntity(settleUrl, settleRequest, Map.class);

        assertEquals(HttpStatus.OK, settleResponse.getStatusCode(), "Should return 200 OK");
        assertNotNull(settleResponse.getBody(), "Response body should not be null");
        
        Object status = settleResponse.getBody().get("status");
        assertTrue(status.toString().contains("SETTLED") || status.toString().equals("2"),
                "Transaction status should be SETTLED");
        System.out.println("✓ Test 8 PASSED: Transaction settled successfully");
    }

    // ========== TEST 9: Reject Double Settlement ==========
    @Test
    @DisplayName("Test 9: Reject settling already settled transaction → 409/422 Error")
    void test9_rejectDoubleSettlement() {
        // Create and settle a transaction first
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(1L);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDate(LocalDate.of(2026, 11, 10));
        dto.setStatus(Transaction.TransactionStatus.PENDING);
        dto.setType(Transaction.TransactionType.PURCHASE);
        dto.setMerchantId("M009");
        dto.setMerchantName("Double Settle Test");

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(transactionUrl, dto, Map.class);
        String txnId = (String) createResponse.getBody().get("id");

        Map<String, Object> settleRequest = new LinkedHashMap<>();
        settleRequest.put("transactionId", txnId);

        // First settlement (should succeed)
        restTemplate.postForEntity(settleUrl, settleRequest, Map.class);

        // Second settlement (should fail)
        ResponseEntity<Map> doubleSettleResponse = restTemplate.postForEntity(settleUrl, settleRequest, Map.class);

        assertTrue(doubleSettleResponse.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY ||
                   doubleSettleResponse.getStatusCode() == HttpStatus.CONFLICT ||
                   doubleSettleResponse.getStatusCode() == HttpStatus.BAD_REQUEST,
                "Should reject double settlement with 409/422/400");
        System.out.println("✓ Test 9 PASSED: Correctly rejected double settlement (Status: " + 
                          doubleSettleResponse.getStatusCode() + ")");
    }

    // ========== TEST 10: Settle Non-existent Transaction Returns 404 ==========
    @Test
    @DisplayName("Test 10: Settle non-existent transaction → 404 Not Found")
    void test10_settleNonexistentTransaction() {
        Map<String, Object> settleRequest = new LinkedHashMap<>();
        settleRequest.put("transactionId", "TXN_INVALID_99999");

        ResponseEntity<Map> response = restTemplate.postForEntity(settleUrl, settleRequest, Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Should return 404 for non-existent transaction");
        System.out.println("✓ Test 10 PASSED: Correctly returned 404 when settling non-existent transaction");
    }

    // ========== TEST 11: Generate Report ==========
    @Test
    @DisplayName("Test 11: Generate transaction report → 200 OK with aggregated data")
    void test11_generateReport() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                reportUrl + "?asOf=2026-11-10", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        System.out.println("✓ Test 11 PASSED: Report generated successfully");
        System.out.println("  Response: " + response.getBody());
    }
}
