# CardDemo Phase 4 - Complete Swagger Testing Guide

**Date:** November 17, 2026  
**System:** CardDemo Authorization Management (Phase 4)  
**Backend URL:** http://localhost:8080  
**Swagger UI:** http://localhost:8080/swagger-ui.html

---

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Testing Sequence](#testing-sequence)
4. [Detailed Test Cases](#detailed-test-cases)
5. [Validation Rules](#validation-rules)
6. [Expected Responses](#expected-responses)
7. [Troubleshooting](#troubleshooting)

---

## Introduction

This document provides a complete step-by-step guide to test all CardDemo Phase 4 Authorization Management APIs using Swagger UI. All tests follow a logical sequence to build up test data and validate system behavior.

**System Capabilities:**
- Create accounts with credit limits and balances
- Create cards with daily/transaction limits
- Create authorizations with automatic approval/decline based on 6 validation rules
- Manually approve/decline authorizations
- View authorization statistics and reports

---

## Prerequisites

### Required Services Running

Verify these services are running:

1. **Backend Service** (Java Spring Boot)
   - Status: Running on port 8080
   - Test: Open http://localhost:8080/swagger-ui.html
   - Expected: Swagger UI loads with all endpoints

2. **Database** (H2 In-Memory)
   - Status: Auto-initialized on backend startup
   - Console: http://localhost:8080/h2-console
   - Credentials: Username=sa, Password=(blank)

### Browser Requirements
- Chrome, Firefox, or Edge (any modern browser)
- JavaScript enabled
- Cookies enabled

---

## Testing Sequence

Follow these steps in order:

1. Create Account (POST /api/accounts)
2. Create Card (POST /api/cards)
3. List Accounts (GET /api/accounts)
4. Get Account Details (GET /api/accounts/{id})
5. Create Authorization - Auto-Approved (POST /api/authorizations)
6. Get Authorization Details (GET /api/authorizations/{id})
7. List All Authorizations (GET /api/authorizations)
8. Create Authorization - Insufficient Balance (POST /api/authorizations)
9. Create Authorization - Transaction Limit Exceeded (POST /api/authorizations)
10. Manual Approve Authorization (PUT /api/authorizations/{id}/approve)
11. Manual Decline Authorization (PUT /api/authorizations/{id}/decline)
12. View Authorization Reports (GET /api/authorizations/reports/summary)

---

## Detailed Test Cases

---

### TEST 1: Create Account

**Objective:** Create a test account to use in subsequent tests

**Endpoint:** `POST /api/accounts`

**URL:** http://localhost:8080/swagger-ui.html

**Steps:**

1. Open http://localhost:8080/swagger-ui.html in browser
2. Scroll down to **"Accounts"** section
3. Find **POST /api/accounts** (shows "Create new account")
4. Click to expand the endpoint
5. Click **"Try it out"** button
6. In the request body field, replace the entire content with:

```json
{
  "creditLimit": 10000,
  "currentBalance": 5000,
  "activeStatus": true,
  "openDate": "2026-01-01",
  "expirationDate": "2027-01-01"
}
```

7. Click **"Execute"** button
8. Wait for response

**Expected Response:**

```
Response Code: 201 CREATED
```

Response Body:
```json
{
  "id": 1,
  "creditLimit": 10000,
  "currentBalance": 5000,
  "activeStatus": true,
  "openDate": "2026-01-01",
  "expirationDate": "2027-01-01",
  "currentCycleDebit": 0,
  "currentCycleCredit": 0
}
```

**Validation Points:**
- ✅ Response status is 201 CREATED
- ✅ Account ID is returned (should be 1)
- ✅ All fields match request
- ✅ Cycle debit/credit initialized to 0

**Important:** Save the account ID (should be **1**) for use in subsequent tests.

---

### TEST 2: Create Card

**Objective:** Create a test card for the account

**Endpoint:** `POST /api/cards`

**Steps:**

1. In Swagger UI, scroll to **"Cards"** section
2. Find **POST /api/cards** (shows "Create a new card")
3. Click to expand
4. Click **"Try it out"**
5. Replace request body with:

```json
{
  "number": "4111111111111111",
  "accountId": 1,
  "status": "ACTIVE",
  "expiryDate": "2026-12-31"
}
```

6. Click **"Execute"**

**Expected Response:**

```
Response Code: 201 CREATED
```

Response Body:
```json
{
  "id": 1,
  "number": "4111111111111111",
  "accountId": 1,
  "status": "ACTIVE",
  "expiryDate": "2026-12-31"
}
```

**Validation Points:**
- ✅ Response status is 201 CREATED
- ✅ Card ID is returned (should be 1)
- ✅ Card number matches request
- ✅ Card status is ACTIVE
- ✅ Account ID is 1

**Important:** Save the card ID (should be **1**) for reference.

---

### TEST 3: List All Accounts

**Objective:** Verify GET /api/accounts endpoint returns account list

**Endpoint:** `GET /api/accounts`

**Steps:**

1. In Swagger UI, scroll to **"Accounts"** section
2. Find **GET /api/accounts** (no {id} parameter)
3. Click to expand
4. Click **"Try it out"**
5. No parameters needed, just click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
[
  {
    "id": 1,
    "creditLimit": 10000,
    "currentBalance": 5000,
    "activeStatus": true,
    "openDate": "2026-01-01",
    "expirationDate": "2027-01-01",
    "currentCycleDebit": 0,
    "currentCycleCredit": 0
  }
]
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ Returns array with at least 1 account
- ✅ Account ID 1 is in the list
- ✅ Account fields are correct

---

### TEST 4: Get Account Details

**Objective:** Retrieve specific account by ID

**Endpoint:** `GET /api/accounts/{id}`

**Steps:**

1. In Swagger UI, scroll to **"Accounts"** section
2. Find **GET /api/accounts/{id}**
3. Click to expand
4. Click **"Try it out"**
5. In the **id** parameter field, enter: `1`
6. Click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
{
  "id": 1,
  "creditLimit": 10000,
  "currentBalance": 5000,
  "activeStatus": true,
  "openDate": "2026-01-01",
  "expirationDate": "2027-01-01",
  "currentCycleDebit": 0,
  "currentCycleCredit": 0
}
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ Correct account returned
- ✅ All fields match TEST 1

---

### TEST 5: Create Authorization (Auto-Approved) ⭐ PHASE 4

**Objective:** Create authorization that automatically approves based on validation rules

**Endpoint:** `POST /api/authorizations`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **POST /api/authorizations** (shows "Create authorization request")
3. Click to expand
4. Click **"Try it out"**
5. Replace request body with:

```json
{
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 500,
  "merchantId": "MERCHANT001",
  "merchantName": "Test Store"
}
```

6. Click **"Execute"**

**Expected Response:**

```
Response Code: 201 CREATED
```

Response Body:
```json
{
  "id": "AUTH1794898930207",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 500,
  "merchantId": "MERCHANT001",
  "merchantName": "Test Store",
  "requestDate": "2026-11-17",
  "responseDate": "2026-11-17",
  "expiryDate": "2026-11-20",
  "status": "APPROVED",
  "approvedAmount": 500
}
```

**Validation Points:**
- ✅ Response status is 201 CREATED
- ✅ Authorization ID generated (format: AUTH + timestamp)
- ✅ Status is **APPROVED** (green) ✅
- ✅ approvedAmount equals requested amount (500)
- ✅ All merchant details captured

**Why APPROVED?**
- ✅ Card exists (4111111111111111)
- ✅ Card is ACTIVE
- ✅ Card not expired (expires 2026-12-31)
- ✅ Balance sufficient ($5000 > $500)
- ✅ Daily limit OK (default $5000)
- ✅ Transaction limit OK (amount $500 < default $2000)

**Important:** Save the authorization ID for subsequent tests.

---

### TEST 6: Get Authorization Details

**Objective:** Retrieve specific authorization by ID

**Endpoint:** `GET /api/authorizations/{id}`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **GET /api/authorizations/{id}**
3. Click to expand
4. Click **"Try it out"**
5. In the **id** parameter field, enter the authorization ID from TEST 5
6. Example: `AUTH1794898930207`
7. Click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
{
  "id": "AUTH1794898930207",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 500,
  "merchantId": "MERCHANT001",
  "merchantName": "Test Store",
  "requestDate": "2026-11-17",
  "responseDate": "2026-11-17",
  "expiryDate": "2026-11-20",
  "status": "APPROVED",
  "approvedAmount": 500
}
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ Returned authorization matches TEST 5
- ✅ All fields intact

---

### TEST 7: List All Authorizations

**Objective:** Retrieve all authorizations with optional filters

**Endpoint:** `GET /api/authorizations`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **GET /api/authorizations**
3. Click to expand
4. Click **"Try it out"**
5. Leave all parameters empty (optional)
6. Click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
[
  {
    "id": "AUTH1794898930207",
    "accountId": 1,
    "cardNumber": "4111111111111111",
    "amount": 500,
    "merchantId": "MERCHANT001",
    "merchantName": "Test Store",
    "requestDate": "2026-11-17",
    "responseDate": "2026-11-17",
    "expiryDate": "2026-11-20",
    "status": "APPROVED",
    "approvedAmount": 500
  }
]
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ Returns array with at least 1 authorization
- ✅ Contains authorization from TEST 5

**Optional Filters:**
Try with query parameters:
- `status=APPROVED` - only approved
- `status=DECLINED` - only declined
- `status=PENDING` - only pending
- `cardId=1` - only for specific card

---

### TEST 8: Create Authorization - Insufficient Balance ❌

**Objective:** Test validation rule: authorization declines when balance insufficient

**Endpoint:** `POST /api/authorizations`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **POST /api/authorizations**
3. Click to expand
4. Click **"Try it out"**
5. Replace request body with:

```json
{
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 6000,
  "merchantId": "MERCHANT002",
  "merchantName": "Expensive Store"
}
```

Note: Amount is $6000 but balance is only $5000

6. Click **"Execute"**

**Expected Response:**

```
Response Code: 201 CREATED
```

Response Body:
```json
{
  "id": "AUTH1794898945123",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 6000,
  "merchantId": "MERCHANT002",
  "merchantName": "Expensive Store",
  "requestDate": "2026-11-17",
  "responseDate": "2026-11-17",
  "expiryDate": "2026-11-20",
  "status": "DECLINED",
  "declineReasonCode": "INSUFFICIENT_BALANCE",
  "approvedAmount": null
}
```

**Validation Points:**
- ✅ Response status is 201 CREATED (still created, but declined)
- ✅ Status is **DECLINED** (red) ❌
- ✅ declineReasonCode is "INSUFFICIENT_BALANCE"
- ✅ approvedAmount is null (not approved)

**Why DECLINED?**
- ✅ Card exists ✓
- ✅ Card is ACTIVE ✓
- ✅ Card not expired ✓
- ❌ Balance insufficient ($5000 < $6000) ✗ **FAILED**
- (Validations stop at first failure)

**Important:** Save this authorization ID for TEST 10 (manual approve).

---

### TEST 9: Create Authorization - Transaction Limit Exceeded ❌

**Objective:** Test validation rule: authorization declines when amount exceeds transaction limit

**Endpoint:** `POST /api/authorizations`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **POST /api/authorizations**
3. Click to expand
4. Click **"Try it out"**
5. Replace request body with:

```json
{
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 3000,
  "merchantId": "MERCHANT003",
  "merchantName": "High Limit Store"
}
```

Note: Amount is $3000 but transaction limit is $2000 (default)

6. Click **"Execute"**

**Expected Response:**

```
Response Code: 201 CREATED
```

Response Body:
```json
{
  "id": "AUTH1794898967456",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 3000,
  "merchantId": "MERCHANT003",
  "merchantName": "High Limit Store",
  "requestDate": "2026-11-17",
  "responseDate": "2026-11-17",
  "expiryDate": "2026-11-20",
  "status": "DECLINED",
  "declineReasonCode": "TRANSACTION_LIMIT_EXCEEDED",
  "approvedAmount": null
}
```

**Validation Points:**
- ✅ Response status is 201 CREATED
- ✅ Status is **DECLINED** (red) ❌
- ✅ declineReasonCode is "TRANSACTION_LIMIT_EXCEEDED"
- ✅ approvedAmount is null

**Why DECLINED?**
- ✅ Card exists ✓
- ✅ Card is ACTIVE ✓
- ✅ Card not expired ✓
- ✅ Balance sufficient ($5000 > $3000) ✓
- ✅ Daily limit OK (default $5000) ✓
- ❌ Transaction limit exceeded ($3000 > $2000) ✗ **FAILED**

**Important:** Save this authorization ID for TEST 11 (manual decline).

---

### TEST 10: Manual Approve Authorization

**Objective:** Manually approve a declined authorization (admin override)

**Endpoint:** `PUT /api/authorizations/{id}/approve`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **PUT /api/authorizations/{id}/approve**
3. Click to expand
4. Click **"Try it out"**
5. In the **id** parameter field, enter the authorization ID from TEST 8
6. Example: `AUTH1794898945123`
7. Leave request body as default or use: `{}`
8. Click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
{
  "id": "AUTH1794898945123",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 6000,
  "merchantId": "MERCHANT002",
  "merchantName": "Expensive Store",
  "requestDate": "2026-11-17",
  "responseDate": "2026-11-17",
  "expiryDate": "2026-11-20",
  "status": "APPROVED",
  "approvedAmount": 6000
}
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ Status changed from DECLINED to **APPROVED** (green) ✅
- ✅ approvedAmount now shows 6000 (was null)
- ✅ declineReasonCode removed

**Note:** Manual approval bypasses validation rules (admin override).

---

### TEST 11: Manual Decline Authorization

**Objective:** Manually decline an authorization with reason (admin override)

**Endpoint:** `PUT /api/authorizations/{id}/decline`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **PUT /api/authorizations/{id}/decline**
3. Click to expand
4. Click **"Try it out"**
5. In the **id** parameter field, enter the authorization ID from TEST 9
6. Example: `AUTH1794898967456`
7. In the request body, enter:

```json
{
  "reason": "Suspicious activity - requires manual review"
}
```

8. Click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
{
  "id": "AUTH1794898967456",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 3000,
  "merchantId": "MERCHANT003",
  "merchantName": "High Limit Store",
  "requestDate": "2026-11-17",
  "responseDate": "2026-11-17",
  "expiryDate": "2026-11-20",
  "status": "DECLINED",
  "declineReasonCode": "Suspicious activity - requires manual review",
  "approvedAmount": null
}
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ Status remains **DECLINED** (red) ❌
- ✅ declineReasonCode updated with custom reason
- ✅ Reason preserved and returned

---

### TEST 12: View Authorization Reports/Statistics

**Objective:** View aggregated authorization statistics

**Endpoint:** `GET /api/authorizations/reports/summary`

**Steps:**

1. In Swagger UI, scroll to **"Authorizations"** section
2. Find **GET /api/authorizations/reports/summary**
3. Click to expand
4. Click **"Try it out"**
5. No parameters needed
6. Click **"Execute"**

**Expected Response:**

```
Response Code: 200 OK
```

Response Body:
```json
{
  "totalCount": 4,
  "approvedCount": 2,
  "declinedCount": 2,
  "pendingCount": 0,
  "totalAmount": 10000.00,
  "averageAmount": 2500.00
}
```

**Validation Points:**
- ✅ Response status is 200 OK
- ✅ totalCount is 4 (all authorizations created)
- ✅ approvedCount is 2 (TEST 5 + TEST 10 manual approve)
- ✅ declinedCount is 2 (TEST 8 + TEST 9 auto-decline)
- ✅ pendingCount is 0 (no pending)
- ✅ totalAmount is 10000 (500 + 6000 + 3000 + ...)
- ✅ averageAmount calculated correctly

---

## Validation Rules

### Authorization Approval Logic (in order)

When you create an authorization, the system performs these checks:

| # | Rule | Check | Pass | Fail |
|---|------|-------|------|------|
| 1 | Card Exists | Card found by number | Continue | INVALID_CARD |
| 2 | Card Not Expired | expiryDate > today | Continue | CARD_EXPIRED |
| 3 | Card Is Active | status = ACTIVE | Continue | INVALID_CARD |
| 4 | Sufficient Balance | balance >= amount | Continue | INSUFFICIENT_BALANCE |
| 5 | Daily Limit | (daily spent + amount) <= dailyLimit | Continue | DAILY_LIMIT_EXCEEDED |
| 6 | Transaction Limit | amount <= maxTransactionAmount | APPROVED | TRANSACTION_LIMIT_EXCEEDED |

**Result:**
- ✅ If all 6 rules pass → **APPROVED** (auto-approval)
- ❌ If any rule fails → **DECLINED** (auto-decline)

---

## Expected Responses

### Success Responses (HTTP 200/201)

**Create Authorization (APPROVED)**
- HTTP 201 Created
- status: "APPROVED"
- approvedAmount: (requested amount)

**Create Authorization (DECLINED)**
- HTTP 201 Created (still created, but status declined)
- status: "DECLINED"
- declineReasonCode: (reason)
- approvedAmount: null

**Get Authorization**
- HTTP 200 OK
- Full authorization object

**List Authorizations**
- HTTP 200 OK
- Array of authorization objects

**Manual Approve/Decline**
- HTTP 200 OK
- Updated authorization object

**View Reports**
- HTTP 200 OK
- Summary statistics

### Error Responses (HTTP 4xx/5xx)

**Not Found (404)**
```
Response Code: 404 NOT FOUND
Body: (empty or error message)
```
Cause: Authorization ID doesn't exist

**Bad Request (400)**
```
Response Code: 400 BAD REQUEST
Body: (validation error details)
```
Cause: Invalid request body format

**Internal Server Error (500)**
```
Response Code: 500 INTERNAL SERVER ERROR
Body: (error message)
```
Cause: Server error (check backend logs)

---

## Troubleshooting

### Issue: "Cannot read properties of undefined"

**Cause:** One of the required fields is missing or has wrong type  
**Solution:** Verify all fields in request body match expected types

### Issue: Authorization always declines

**Check:**
1. Is card created first? (TEST 2)
2. Does card have same number in authorization request?
3. Is account balance sufficient?
4. Is card status ACTIVE?

### Issue: GET /api/accounts returns 405 Method Not Allowed

**Cause:** Endpoint didn't exist in initial version  
**Solution:** Backend was updated, refresh Swagger page (Ctrl+F5)

### Issue: Authorization ID format unexpected

**Format:** `AUTH` + system timestamp  
**Example:** `AUTH1794898930207`  
**Note:** Each authorization gets unique ID

### Issue: Manual approve/decline doesn't work

**Check:**
1. Authorization ID is correct
2. Using PUT method (not POST)
3. Request body format is correct (JSON)

### Verify Backend is Running

```
Open: http://localhost:8080/swagger-ui.html
Expected: Swagger UI loads with all endpoints listed
```

If page doesn't load:
1. Backend might not be running
2. Check terminal where backend started
3. Restart backend: `mvn spring-boot:run`

---

## Summary Checklist

- [ ] Backend running on port 8080
- [ ] Swagger UI loads at http://localhost:8080/swagger-ui.html
- [ ] TEST 1: Account created (ID: 1)
- [ ] TEST 2: Card created (ID: 1)
- [ ] TEST 3: GET accounts returns list
- [ ] TEST 4: GET account/{id} returns details
- [ ] TEST 5: Authorization auto-approved
- [ ] TEST 6: GET authorization/{id} returns details
- [ ] TEST 7: GET authorizations returns list
- [ ] TEST 8: Authorization declined (insufficient balance)
- [ ] TEST 9: Authorization declined (transaction limit)
- [ ] TEST 10: Manual approve works
- [ ] TEST 11: Manual decline works
- [ ] TEST 12: Reports show correct statistics

---

## Contact & Support

**Backend Repository:** c:\Gouri\document\carddemo-modernization  
**Frontend Repository:** c:\Gouri\document\carddemo-react  
**Documentation:** See .agents/tasks/ folders

---

**End of Testing Guide**
