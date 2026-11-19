-- Sample data for CardDemo application
-- Inserted into H2 dev database on application startup

-- Clear existing data (for idempotency)
DELETE FROM "authorization" WHERE 1=1;
DELETE FROM transaction WHERE 1=1;
DELETE FROM card WHERE 1=1;
DELETE FROM account WHERE 1=1;
DELETE FROM customer WHERE 1=1;
DELETE FROM transaction_type WHERE 1=1;

-- Transaction Types
INSERT INTO transaction_type (id, description, category) VALUES
('PURCH', 'Purchase Transaction', 'DEBIT'),
('CASH', 'Cash Advance', 'DEBIT'),
('PMT', 'Payment Received', 'CREDIT'),
('FEE', 'Service Fee', 'DEBIT'),
('INT', 'Interest Charge', 'DEBIT');

-- Customers (5 sample customers)
INSERT INTO customer (id, name, contact) VALUES
(1, 'John Smith', 'john.smith@example.com'),
(2, 'Jane Doe', 'jane.doe@example.com'),
(3, 'Robert Johnson', 'robert.johnson@example.com'),
(4, 'Mary Wilson', 'mary.wilson@example.com'),
(5, 'Michael Brown', 'michael.brown@example.com');

-- Accounts (5 sample accounts with varying limits and balances)
-- Per gap-assessment: credit limits 5000-50000, balances 500-25000
INSERT INTO account (id, credit_limit, current_balance, active_status, reissue_date, open_date, expiration_date, cash_credit_limit, current_cycle_debit, current_cycle_credit, group_id) VALUES
(1, 50000.00, 25000.00, TRUE, '2026-06-01', '2020-01-15', '2028-01-15', 10000.00, 2500.00, 0.00, 'GROUP_A'),
(2, 25000.00, 12500.00, TRUE, '2026-07-15', '2021-03-20', '2028-03-20', 5000.00, 1250.00, 0.00, 'GROUP_A'),
(3, 15000.00, 7500.00, TRUE, '2026-08-01', '2022-06-10', '2028-06-10', 3000.00, 750.00, 0.00, 'GROUP_B'),
(4, 10000.00, 5000.00, TRUE, '2026-09-20', '2023-01-05', '2028-01-05', 2000.00, 500.00, 0.00, 'GROUP_B'),
(5, 5000.00, 500.00, TRUE, '2026-11-01', '2024-01-01', '2028-01-01', 1000.00, 100.00, 0.00, 'GROUP_C');

-- Cards (10 sample cards: 5 active, 3 expired, 2 blocked)
INSERT INTO card (id, number, account_id, status, expiry_date) VALUES
(1, '4111111111111111', 1, 'ACTIVE', '2028-12-31'),
(2, '5555555555554444', 1, 'ACTIVE', '2027-06-30'),
(3, '378282246310005', 2, 'ACTIVE', '2027-09-30'),
(4, '6011111111111117', 2, 'ACTIVE', '2027-03-31'),
(5, '3530111333300000', 3, 'ACTIVE', '2028-06-30'),
(6, '4532015112830366', 3, 'EXPIRED', '2025-12-31'),
(7, '5425233010103186', 4, 'EXPIRED', '2025-06-30'),
(8, '2221003100100101', 4, 'BLOCKED', '2026-03-31'),
(9, '6011000990139424', 5, 'BLOCKED', '2026-09-30'),
(10, '3714496353622689', 5, 'EXPIRED', '2025-09-30');

-- Transactions (20 sample transactions spanning last 90 days)
-- Mix of PURCHASE, PAYMENT, FEE, INTEREST types and PENDING, POSTED, SETTLED statuses
INSERT INTO transaction (id, account_id, card_number, amount, date, post_date, type, status, merchant_id, merchant_name, processing_code) VALUES
('TXN001', 1, '4111111111111111', 150.00, '2026-10-15', '2026-10-15', 'PURCHASE', 'SETTLED', 'M123', 'Walmart', '000000'),
('TXN002', 1, '4111111111111111', 75.50, '2026-10-16', '2026-10-16', 'PURCHASE', 'SETTLED', 'M124', 'Best Buy', '000000'),
('TXN003', 1, '5555555555554444', 200.00, '2026-10-18', '2026-10-18', 'PURCHASE', 'POSTED', 'M125', 'Amazon', '000000'),
('TXN004', 1, '4111111111111111', 500.00, '2026-10-20', NULL, 'PURCHASE', 'PENDING', 'M126', 'Delta Airlines', '000000'),
('TXN005', 1, '4111111111111111', 1000.00, '2026-10-22', '2026-10-22', 'PAYMENT', 'SETTLED', 'PAYMENT', 'Manual Payment', '000000'),
('TXN006', 2, '378282246310005', 125.75, '2026-10-10', '2026-10-10', 'PURCHASE', 'SETTLED', 'M201', 'Starbucks', '000000'),
('TXN007', 2, '5425233010103186', 50.00, '2026-10-12', '2026-10-12', 'PURCHASE', 'SETTLED', 'M202', 'Shell Gas', '000000'),
('TXN008', 2, '378282246310005', 300.00, '2026-10-17', '2026-10-17', 'CASH_ADVANCE', 'SETTLED', 'ATM001', 'ATM Withdrawal', '000001'),
('TXN009', 2, '6011111111111117', 75.00, '2026-10-21', NULL, 'PURCHASE', 'PENDING', 'M203', 'Nike Store', '000000'),
('TXN010', 2, '378282246310005', 25.00, '2026-10-23', NULL, 'FEE', 'PENDING', 'FEE', 'Monthly Fee', '000000'),
('TXN011', 3, '3530111333300000', 99.99, '2026-10-08', '2026-10-08', 'PURCHASE', 'SETTLED', 'M301', 'Target', '000000'),
('TXN012', 3, '3530111333300000', 45.00, '2026-10-14', '2026-10-14', 'PURCHASE', 'SETTLED', 'M302', 'Costco', '000000'),
('TXN013', 3, '3530111333300000', 250.00, '2026-10-19', NULL, 'PAYMENT', 'POSTED', 'PAYMENT', 'Automatic Payment', '000000'),
('TXN014', 4, '6011000990139424', 35.50, '2026-10-09', '2026-10-09', 'PURCHASE', 'SETTLED', 'M401', 'Subway', '000000'),
('TXN015', 4, '2221003100100101', 180.00, '2026-10-15', '2026-10-15', 'PURCHASE', 'SETTLED', 'M402', 'Home Depot', '000000'),
('TXN016', 4, '6011000990139424', 40.00, '2026-10-22', NULL, 'PURCHASE', 'PENDING', 'M403', 'Coffee Shop', '000000'),
('TXN017', 5, '3714496353622689', 25.00, '2026-10-05', '2026-10-05', 'PURCHASE', 'SETTLED', 'M501', 'McDonald''s', '000000'),
('TXN018', 5, '6011000990139424', 60.00, '2026-10-11', '2026-10-11', 'PURCHASE', 'SETTLED', 'M502', 'Pizza Hut', '000000'),
('TXN019', 5, '3714496353622689', 100.00, '2026-10-13', NULL, 'PAYMENT', 'POSTED', 'PAYMENT', 'Manual Payment', '000000'),
('TXN020', 5, '6011000990139424', 5.00, '2026-10-24', NULL, 'FEE', 'PENDING', 'FEE', 'Late Payment Fee', '000000');

-- Authorizations (5 sample authorizations: 2 approved, 2 declined, 1 pending)
-- Per gap-assessment: 3-day expiry default, status PENDING/APPROVED/DECLINED
-- Decline reason codes: 3100=Card Not Found, 4100=Insufficient Funds, 4200=Expired Card, 4300=Card Blocked, 5100=Account Closed, 5200=Account Suspended, 9000=System Error
INSERT INTO "authorization" (id, account_id, card_number, amount, request_date, response_date, expiry_date, status, decline_reason_code, approved_amount) VALUES
('AUTH001', 1, '4111111111111111', 500.00, '2026-10-20', '2026-10-20', '2026-10-23', 'APPROVED', NULL, 500.00),
('AUTH002', 2, '378282246310005', 1000.00, '2026-10-21', '2026-10-21', '2026-10-24', 'DECLINED', '4100', NULL),
('AUTH003', 3, '3530111333300000', 750.00, '2026-10-22', '2026-10-22', '2026-10-25', 'APPROVED', NULL, 750.00),
('AUTH004', 4, '2221003100100101', 200.00, '2026-10-23', '2026-10-23', '2026-10-26', 'DECLINED', '4300', NULL),
('AUTH005', 5, '3714496353622689', 100.00, '2026-10-24', NULL, '2026-10-27', 'PENDING', NULL, NULL);
