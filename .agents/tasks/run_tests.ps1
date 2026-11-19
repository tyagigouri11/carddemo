# Phase 3 Comprehensive Test Suite
# All 11 test cases for transaction management APIs

$testResults = @()
$txnIds = @{}
$baseUrl = "http://localhost:8080"

function LogTest($testNum, $testName, $expected, $actual, $status, $details) {
    $result = @{
        TestNum = $testNum
        TestName = $testName
        Expected = $expected
        Actual = $actual
        Status = $status
        Details = $details
    }
    $script:testResults += $result
    $color = if ($status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "TEST $testNum ($testName): $status" -ForegroundColor $color
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PHASE 3 COMPREHENSIVE TEST SUITE"
Write-Host "========================================" -ForegroundColor Cyan

# ==================== API 1: CREATE TRANSACTIONS ====================
Write-Host "`n[API 1] POST /api/transactions - Create Transactions" -ForegroundColor Yellow

# Test 1: Create PURCHASE
Write-Host "`nTest 1: Create PURCHASE transaction..."
$body = @{
    accountId = 1
    amount = 99.99
    date = "2026-11-10"
    status = "PENDING"
    type = "PURCHASE"
    merchantId = "M001"
    merchantName = "Coffee Shop"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -ErrorAction Stop
    $txnIds["test1"] = $response.id
    LogTest 1 "Create PURCHASE" "201 Created" "201" "PASS" "ID: $($response.id)"
    Write-Host "Response: $($response | ConvertTo-Json)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    LogTest 1 "Create PURCHASE" "201 Created" "$statusCode" "FAIL" "$($_.Exception.Message)"
}

# Test 2: Create PAYMENT
Write-Host "`nTest 2: Create PAYMENT transaction..."
$body = @{
    accountId = 2
    amount = 500.00
    date = "2026-11-10"
    status = "PENDING"
    type = "PAYMENT"
    merchantId = "M002"
    merchantName = "Payment Portal"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -ErrorAction Stop
    $txnIds["test2"] = $response.id
    LogTest 2 "Create PAYMENT" "201 Created" "201" "PASS" "ID: $($response.id)"
    Write-Host "Response: $($response | ConvertTo-Json)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    LogTest 2 "Create PAYMENT" "201 Created" "$statusCode" "FAIL" "$($_.Exception.Message)"
}

# Test 3: Create CASH_ADVANCE
Write-Host "`nTest 3: Create CASH_ADVANCE transaction..."
$body = @{
    accountId = 3
    amount = 200.00
    date = "2026-11-10"
    status = "PENDING"
    type = "CASH_ADVANCE"
    merchantId = "M003"
    merchantName = "ATM Withdrawal"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -ErrorAction Stop
    $txnIds["test3"] = $response.id
    LogTest 3 "Create CASH_ADVANCE" "201 Created" "201" "PASS" "ID: $($response.id)"
    Write-Host "Response: $($response | ConvertTo-Json)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    LogTest 3 "Create CASH_ADVANCE" "201 Created" "$statusCode" "FAIL" "$($_.Exception.Message)"
}

# Test 4: Create with NEGATIVE amount
Write-Host "`nTest 4: Create with NEGATIVE amount (should reject)..."
$body = @{
    accountId = 1
    amount = -50.00
    date = "2026-11-10"
    status = "PENDING"
    type = "PURCHASE"
    merchantId = "M004"
    merchantName = "Negative Test"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -ErrorAction Stop
    LogTest 4 "Negative Amount Validation" "400/422 Error" "200 Success" "FAIL" "Should have rejected negative amount"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400 -or $statusCode -eq 422) {
        LogTest 4 "Negative Amount Validation" "400/422 Error" "$statusCode" "PASS" "Correctly rejected negative amount"
    } else {
        LogTest 4 "Negative Amount Validation" "400/422 Error" "$statusCode" "FAIL" "Got unexpected status code"
    }
}

# Test 5: Create with MISSING fields
Write-Host "`nTest 5: Create with MISSING required fields (should reject)..."
$body = @{
    accountId = 1
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -ErrorAction Stop
    LogTest 5 "Missing Fields Validation" "400 Error" "200 Success" "FAIL" "Should have rejected missing fields"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400) {
        LogTest 5 "Missing Fields Validation" "400 Error" "$statusCode" "PASS" "Correctly rejected missing fields"
    } else {
        LogTest 5 "Missing Fields Validation" "400 Error" "$statusCode" "FAIL" "Got status $statusCode"
    }
}

# ==================== API 2: GET TRANSACTION ====================
Write-Host "`n[API 2] GET /api/transactions/{id} - Get Transaction" -ForegroundColor Yellow

# Test 6: Get valid transaction
Write-Host "`nTest 6: Get valid transaction (from Test 1)..."
if ($txnIds["test1"]) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions/$($txnIds['test1'])" -Method GET -ErrorAction Stop
        LogTest 6 "Get Valid Transaction" "200 OK" "200" "PASS" "Retrieved: $($response.id)"
        Write-Host "Response: $($response | ConvertTo-Json)"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        LogTest 6 "Get Valid Transaction" "200 OK" "$statusCode" "FAIL" "$($_.Exception.Message)"
    }
} else {
    LogTest 6 "Get Valid Transaction" "200 OK" "N/A" "SKIP" "Test 1 failed, no txn ID available"
}

# Test 7: Get non-existent transaction
Write-Host "`nTest 7: Get non-existent transaction (should return 404)..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions/TXN_INVALID_12345" -Method GET -ErrorAction Stop
    LogTest 7 "Non-existent Transaction" "404 Not Found" "200 Success" "FAIL" "Should have returned 404"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        LogTest 7 "Non-existent Transaction" "404 Not Found" "$statusCode" "PASS" "Correctly returned 404"
    } else {
        LogTest 7 "Non-existent Transaction" "404 Not Found" "$statusCode" "FAIL" "Got unexpected status $statusCode"
    }
}

# ==================== API 3: SETTLE TRANSACTION ====================
Write-Host "`n[API 3] POST /api/transactions/settle - Settle Transaction" -ForegroundColor Yellow

# Test 8: Settle valid transaction
Write-Host "`nTest 8: Settle transaction (from Test 1)..."
if ($txnIds["test1"]) {
    $settleBody = @{ transactionId = $txnIds["test1"] } | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions/settle" -Method POST -Headers @{"Content-Type"="application/json"} -Body $settleBody -ErrorAction Stop
        if ($response.status -eq "SETTLED" -or $response.status -eq "2") {
            LogTest 8 "Settle Valid Transaction" "200 OK + SETTLED status" "200 + SETTLED" "PASS" "Settled: $($response.id)"
        } else {
            LogTest 8 "Settle Valid Transaction" "200 OK + SETTLED status" "200 + $($response.status)" "FAIL" "Wrong status returned"
        }
        Write-Host "Response: $($response | ConvertTo-Json)"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        LogTest 8 "Settle Valid Transaction" "200 OK + SETTLED status" "$statusCode" "FAIL" "$($_.Exception.Message)"
    }
} else {
    LogTest 8 "Settle Valid Transaction" "200 OK + SETTLED status" "N/A" "SKIP" "Test 1 failed, no txn ID"
}

# Test 9: Try settling same transaction again (should fail)
Write-Host "`nTest 9: Settle already settled transaction (should reject)..."
if ($txnIds["test1"]) {
    $settleBody = @{ transactionId = $txnIds["test1"] } | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions/settle" -Method POST -Headers @{"Content-Type"="application/json"} -Body $settleBody -ErrorAction Stop
        LogTest 9 "Double Settle Validation" "422 Unprocessable Entity" "200 Success" "FAIL" "Should reject settling already settled transaction"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 422 -or $statusCode -eq 400) {
            LogTest 9 "Double Settle Validation" "422 Unprocessable Entity" "$statusCode" "PASS" "Correctly rejected double settle"
        } else {
            LogTest 9 "Double Settle Validation" "422 Unprocessable Entity" "$statusCode" "FAIL" "Got status $statusCode"
        }
    }
} else {
    LogTest 9 "Double Settle Validation" "422 Unprocessable Entity" "N/A" "SKIP" "Test 1 failed"
}

# Test 10: Try settling non-existent transaction
Write-Host "`nTest 10: Settle non-existent transaction (should return 404)..."
$settleBody = @{ transactionId = "TXN_INVALID_99999" } | ConvertTo-Json
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions/settle" -Method POST -Headers @{"Content-Type"="application/json"} -Body $settleBody -ErrorAction Stop
    LogTest 10 "Settle Non-existent" "404 Not Found" "200 Success" "FAIL" "Should return 404"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        LogTest 10 "Settle Non-existent" "404 Not Found" "$statusCode" "PASS" "Correctly returned 404"
    } else {
        LogTest 10 "Settle Non-existent" "404 Not Found" "$statusCode" "FAIL" "Got status $statusCode"
    }
}

# ==================== API 4: GENERATE REPORT ====================
Write-Host "`n[API 4] GET /api/transactions/reports/transactions - Generate Report" -ForegroundColor Yellow

# Test 11: Generate report for date 2026-11-10
Write-Host "`nTest 11: Generate transaction report for date 2026-11-10..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/transactions/reports/transactions?asOf=2026-11-10" -Method GET -ErrorAction Stop
    LogTest 11 "Generate Report" "200 OK with aggregated data" "200" "PASS" "Report generated"
    Write-Host "Response: $($response | ConvertTo-Json)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    LogTest 11 "Generate Report" "200 OK with aggregated data" "$statusCode" "FAIL" "$($_.Exception.Message)"
}

# ==================== SUMMARY ====================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY REPORT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$skipCount = ($testResults | Where-Object { $_.Status -eq "SKIP" }).Count

Write-Host "`nResults:"
Write-Host "  PASS: $passCount"
Write-Host "  FAIL: $failCount"
Write-Host "  SKIP: $skipCount"
Write-Host "  TOTAL: $($testResults.Count)"

Write-Host "`nDetailed Results:" -ForegroundColor Yellow
foreach ($result in $testResults) {
    $color = switch ($result.Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "SKIP" { "Yellow" }
    }
    Write-Host "Test $($result.TestNum): $($result.TestName) - $($result.Status)" -ForegroundColor $color
    Write-Host "  Expected: $($result.Expected)"
    Write-Host "  Actual: $($result.Actual)"
    Write-Host "  Details: $($result.Details)"
}

# Export results to JSON
$testResults | ConvertTo-Json | Out-File -FilePath "c:\Gouri\document\.agents\tasks\test_results.json" -Force
Write-Host "`nResults exported to: c:\Gouri\document\.agents\tasks\test_results.json"

# Export transaction IDs
$txnIds | ConvertTo-Json | Out-File -FilePath "c:\Gouri\document\.agents\tasks\txn_ids.json" -Force
Write-Host "Transaction IDs exported to: c:\Gouri\document\.agents\tasks\txn_ids.json"
