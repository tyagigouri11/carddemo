-- CardDemo Database Schema (H2 compatible)
-- COBOL to Java Migration: Phase 1
-- Generated from AWS Transform reverse-engineering documentation

-- Customer Table
-- Source: COUSR00C-03C (User/Admin programs), customer master data
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Account Table
-- Source: CBACT01C-04C (Account file processing, balance tracking, interest calculation)
-- PIC Mappings: creditLimit/balance/cashCreditLimit are COMP-3 money fields (9V99)
CREATE TABLE IF NOT EXISTS account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    credit_limit DECIMAL(12, 2) NOT NULL,
    current_balance DECIMAL(12, 2) NOT NULL DEFAULT 0,
    active_status BOOLEAN NOT NULL DEFAULT TRUE,
    reissue_date DATE,
    open_date DATE,
    expiration_date DATE,
    cash_credit_limit DECIMAL(12, 2),
    current_cycle_debit DECIMAL(12, 2) DEFAULT 0,
    current_cycle_credit DECIMAL(12, 2) DEFAULT 0,
    group_id VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_active_status ON account(active_status);
CREATE INDEX idx_reissue_date ON account(reissue_date);

-- Card Table
-- Source: COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC (Card issuance, updates)
-- PIC Mappings: cardNumber is 12-19 digit PIC X(19); expiryDate is YYYYMMDD
CREATE TABLE IF NOT EXISTS card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    number VARCHAR(19) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expiry_date DATE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);
CREATE INDEX idx_account_id ON card(account_id);
CREATE INDEX idx_status_expiry ON card(status, expiry_date);

-- Transaction Table
-- Source: CBTRN01C-03C (Daily transaction posting, settlement, reporting)
-- PIC Mappings: transactionId is 12-char PIC X(12); amount is COMP-3 9V99; date is YYYYMMDD
CREATE TABLE IF NOT EXISTS transaction (
    id VARCHAR(50) PRIMARY KEY,
    account_id BIGINT NOT NULL,
    card_number VARCHAR(19),
    amount DECIMAL(12, 2) NOT NULL,
    date DATE NOT NULL,
    post_date DATE,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    merchant_id VARCHAR(50),
    merchant_name VARCHAR(100),
    processing_code VARCHAR(10),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    FOREIGN KEY (card_number) REFERENCES card(number)
);
CREATE INDEX idx_transaction_account_id ON transaction(account_id);
CREATE INDEX idx_transaction_card_number ON transaction(card_number);
CREATE INDEX idx_transaction_date ON transaction(date);
CREATE INDEX idx_transaction_status ON transaction(status);
CREATE INDEX idx_transaction_status_date ON transaction(status, date);

-- Authorization Table
-- Source: COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C (Real-time authorization, pending auth management)
-- PIC Mappings: authId is 12-char PIC X(12); amount is COMP-3 9V99; dates are YYYYMMDD
-- Assumption: 3-day expiry default per CBPAUP0C business rules
CREATE TABLE IF NOT EXISTS "authorization" (
    id VARCHAR(50) PRIMARY KEY,
    account_id BIGINT NOT NULL,
    card_number VARCHAR(19),
    amount DECIMAL(12, 2) NOT NULL,
    request_date DATE NOT NULL,
    response_date DATE,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    decline_reason_code VARCHAR(10),
    approved_amount DECIMAL(12, 2),
    merchant_id VARCHAR(50),
    merchant_name VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    FOREIGN KEY (card_number) REFERENCES card(number)
);
CREATE INDEX idx_authorization_account_id ON "authorization"(account_id);
CREATE INDEX idx_authorization_status ON "authorization"(status);
CREATE INDEX idx_authorization_status_expiry ON "authorization"(status, expiry_date);
CREATE INDEX idx_authorization_expiry_date ON "authorization"(expiry_date);

-- Transaction Type Table
-- Source: COTRTUPC (Transaction type module, DB2 access)
CREATE TABLE IF NOT EXISTS transaction_type (
    id VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
