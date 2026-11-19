INSERT INTO account (id, active_status, credit_limit, current_balance, cash_credit_limit, current_cycle_debit, current_cycle_credit, open_date, expiration_date, reissue_date, created_date, updated_date, group_id) VALUES
(1, 1, 50000, 5000, 10000, 500, 45000, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), CURDATE(), NOW(), NOW(), 'GROUP001'),
(2, 1, 75000, 15000, 15000, 1000, 74000, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), CURDATE(), NOW(), NOW(), 'GROUP001'),
(3, 1, 100000, 25000, 20000, 2000, 98000, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), CURDATE(), NOW(), NOW(), 'GROUP001');

INSERT INTO card (id, account_id, number, expiry_date, status, created_date, updated_date) VALUES
(1, 1, '4111111111111111', DATE_ADD(CURDATE(), INTERVAL 5 YEAR), 'ACTIVE', NOW(), NOW()),
(2, 2, '4111111111111112', DATE_ADD(CURDATE(), INTERVAL 5 YEAR), 'ACTIVE', NOW(), NOW()),
(3, 3, '4111111111111113', DATE_ADD(CURDATE(), INTERVAL 5 YEAR), 'ACTIVE', NOW(), NOW());
