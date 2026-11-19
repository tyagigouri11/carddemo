# Implementation Plan: Replace H2 with MySQL

**Objective**: Replace H2 in-memory database with MySQL 8.0+ in the Spring Boot carddemo-modernization project. Maintain backward compatibility with all phases (Phase 1-4), ensure 22 tests pass, verify data persistence, and keep production-ready configuration.

**Key Decisions**:
- **MySQL Driver**: Use `mysql-connector-j` 8.4.0+ (newer JDBC 4.3 driver) as it's the official MySQL Connector/J replacement and recommended for Spring Boot 3.x
- **Character Set**: UTF-8 (utf8mb4) for international character support and emoji
- **Dialect**: Use `MySQL8Dialect` (official Hibernate support for MySQL 8.0+)
- **ddl-auto**: Change from `create-drop` (dev) to `update` (prod) for persistence; keep `create-drop` for test profile
- **Credentials**: Use environment variables `${DB_USERNAME:root}` and `${DB_PASSWORD:}` with safe defaults (no hard-coded passwords)
- **Connection Pooling**: Use HikariCP (Spring Boot default), set max pool size = 10, idle timeout = 10 mins
- **MySQL Availability Check**: Before running tests, verify MySQL server is running and accessible on localhost:3306 via `mysql -h localhost -u root -e "SELECT 1"`. If not reachable, provide clear error message and setup instructions.

---

## Implementation Steps

### Phase 0: Prerequisites and MySQL Server Verification

- [ ] **Step 1: Verify MySQL is installed and running locally**
  - Check: `mysql --version` to confirm MySQL 8.0+ is installed
  - Check: `mysql -h localhost -u root -e "SELECT 1"` succeeds (MySQL server is running)
  - If MySQL is not running: Instruct user to start MySQL service via MySQL Workbench or CLI (`mysql.server start` on macOS, `net start MySQL80` on Windows)
  - If connection fails: Create a standalone troubleshooting guide with MySQL Workbench connection steps
  - **Files**: None (diagnostic step)
  - **Verify**: Manual check — MySQL responds to connection

### Phase 1: Update pom.xml

- [ ] **Step 2: Remove H2 dependency and add MySQL Connector/J 8.4.0**
  - Remove: `<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId>...` (lines ~29-33)
  - Add MySQL JDBC driver after the removed H2 block:
    ```xml
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.4.0</version>
        <scope>runtime</scope>
    </dependency>
    ```
  - **Files**: `pom.xml`
  - **Verify**: `mvn clean compile` succeeds (Maven resolves mysql-connector-j without errors)

### Phase 2: Update application.yml (Default/Common Profile)

- [ ] **Step 3: Update application.yml to remove H2 references and add MySQL connection string**
  - Replace `spring.datasource` section with MySQL config:
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/carddemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
        driverClassName: com.mysql.cj.jdbc.Driver
        username: ${DB_USERNAME:root}
        password: ${DB_PASSWORD:}
        hikari:
          maximum-pool-size: 10
          connection-timeout: 30000
          idle-timeout: 600000
          max-lifetime: 1800000
      jpa:
        hibernate:
          ddl-auto: update
        database-platform: org.hibernate.dialect.MySQL8Dialect
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
            format_sql: true
    ```
  - Remove: `spring.h2` section entirely (H2 console config)
  - Add charset parameter `characterEncoding=utf8mb4` to JDBC URL
  - **Files**: `src/main/resources/application.yml`
  - **Verify**: YAML syntax is valid (no parsing errors when Spring Boot loads it)

### Phase 3: Update application-dev.yml (Development Profile)

- [ ] **Step 4: Update application-dev.yml to use MySQL for dev profile**
  - Replace `spring.datasource` and `spring.jpa` sections:
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/carddemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
        driverClassName: com.mysql.cj.jdbc.Driver
        username: ${DB_USERNAME:root}
        password: ${DB_PASSWORD:}
      jpa:
        database-platform: org.hibernate.dialect.MySQL8Dialect
        hibernate:
          ddl-auto: create-drop
        show-sql: false
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
            format_sql: true
    ```
  - Remove: `spring.h2` section entirely
  - Keep `create-drop` for dev to clean DB on each restart
  - **Files**: `src/main/resources/application-dev.yml`
  - **Verify**: YAML syntax is valid

### Phase 4: Update application-prod.yml (Production Profile)

- [ ] **Step 5: Update application-prod.yml to use MySQL with environment variables and update for production**
  - Replace entire config:
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/carddemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
        driverClassName: com.mysql.cj.jdbc.Driver
        username: ${DB_USERNAME:root}
        password: ${DB_PASSWORD:}
        hikari:
          maximum-pool-size: 20
          connection-timeout: 30000
          idle-timeout: 600000
          max-lifetime: 1800000
      jpa:
        database-platform: org.hibernate.dialect.MySQL8Dialect
        hibernate:
          ddl-auto: validate
        show-sql: false
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
            format_sql: true
    
    logging:
      level:
        root: INFO
        com.carddemo: INFO
        org.springframework.web: WARN
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics
      endpoint:
        health:
          show-details: when-authorized
    ```
  - Key changes: ddl-auto=`validate` (no schema mutations in prod), credentials from env vars, higher connection pool (20)
  - Credentials MUST come from env vars, NOT hard-coded
  - **Files**: `src/main/resources/application-prod.yml`
  - **Verify**: YAML syntax is valid and credentials are env var refs only

### Phase 5: Create/Update MySQL Schema

- [ ] **Step 6: Create MySQL-specific schema.sql and replace H2 version**
  - Create new `src/main/resources/schema-mysql.sql` (MySQL-specific DDL):
    - Drop existing tables if they exist (for dev/test safety)
    - Explicitly set UTF-8 charset for all tables: `CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
    - Ensure all foreign keys have `ON DELETE CASCADE`
    - Add indexes as per current schema (account: idx_active_status, idx_reissue_date; card: idx_account_id, idx_status_expiry; transaction: idx_transaction_*, authorization: idx_authorization_*)
    - Include all six entities: customer, account, card, transaction, authorization, transaction_type
    - Add Phase 4 fields: `daily_limit` and `max_transaction_amount` on card table with DEFAULT 5000.00 and 2000.00
    - Schema should be idempotent: use `CREATE TABLE IF NOT EXISTS` and `DROP TABLE IF EXISTS` where safe
  - Content: Full MySQL schema with all Phase 1-4 tables and constraints
  - **Files**: `src/main/resources/schema-mysql.sql`
  - **Verify**: MySQL syntax valid; can be executed in MySQL Workbench: `mysql -u root -e "source src/main/resources/schema-mysql.sql"`

- [ ] **Step 7: Create standalone MySQL Workbench setup script**
  - Create `src/main/resources/mysql-setup.sql` for manual database creation in MySQL Workbench:
    - Create database `carddemo` with UTF-8 charset and collation
    - Grant privileges: `GRANT ALL PRIVILEGES ON carddemo.* TO 'carddemo'@'localhost' IDENTIFIED BY 'carddemo123'; FLUSH PRIVILEGES;`
    - Can be run directly in MySQL Workbench Query window
  - This allows users to set up MySQL independently before running Spring Boot
  - **Files**: `src/main/resources/mysql-setup.sql`
  - **Verify**: File exists and syntax is valid MySQL

### Phase 6: Test Profile Configuration

- [ ] **Step 8: Create application-test.yml for test environment**
  - Create `src/main/resources/application-test.yml`:
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/carddemo_test?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
        driverClassName: com.mysql.cj.jdbc.Driver
        username: ${DB_USERNAME:root}
        password: ${DB_PASSWORD:}
      jpa:
        database-platform: org.hibernate.dialect.MySQL8Dialect
        hibernate:
          ddl-auto: create-drop
        show-sql: false
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
            format_sql: true
    
    logging:
      level:
        root: INFO
        com.carddemo: DEBUG
        org.springframework.test: DEBUG
    ```
  - Tests run against separate `carddemo_test` database to avoid polluting production data
  - Uses `create-drop` to ensure clean state for each test run
  - **Files**: `src/main/resources/application-test.yml`
  - **Verify**: File exists with valid YAML

### Phase 7: Maven Test Execution

- [ ] **Step 9: Run full test suite against MySQL**
  - Pre-test: Verify MySQL server is running (`mysql -u root -e "SELECT 1"`)
  - Pre-test: Drop and recreate test database: `mysql -u root -e "DROP DATABASE IF EXISTS carddemo_test; CREATE DATABASE carddemo_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"`
  - Command: `mvn clean test -Dspring.profiles.active=test`
  - Expected: All 22 tests pass (11 Phase 4 Authorization tests + 11 Phase 3 Transaction tests)
  - Verify each test class:
    - `AuthorizationControllerTest` (11 tests): POST /authorizations, GET /authorizations/{id}, approval/decline logic, listing, expiry rules
    - `TransactionControllerTest` (11 tests): POST /transactions, GET /transactions/{id}, settlement, reporting
  - **Files**: None (execution step)
  - **Verify**: Build output shows `[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`

### Phase 8: Data Persistence Verification

- [ ] **Step 10: Verify data persists across test runs and queries work**
  - Run: Start Spring Boot app with dev profile: `java -jar target/carddemo-*.jar --spring.profiles.active=dev`
  - Verify: Swagger UI loads at `http://localhost:8080/swagger-ui.html`
  - Verify: Use MySQL CLI or MySQL Workbench to query data:
    ```sql
    SELECT COUNT(*) as account_count FROM account;
    SELECT COUNT(*) as card_count FROM card;
    SELECT * FROM authorization WHERE status = 'APPROVED' LIMIT 5;
    SELECT SUM(amount) as total_transactions FROM transaction;
    ```
  - Create a test account via API: `POST /api/accounts` with creditLimit=10000, currentBalance=5000, activeStatus=true
  - Verify: Query MySQL to confirm account row exists: `SELECT * FROM account WHERE credit_limit = 10000;`
  - Stop app, restart app, query again to confirm data persists (not lost like H2 in-memory)
  - **Files**: None (verification step)
  - **Verify**: Queries return expected data; data survives app restart

### Phase 9: Documentation

- [ ] **Step 11: Create MySQL connection and setup documentation**
  - Create `docs/MYSQL_SETUP.md`:
    - MySQL installation prerequisites (MySQL 8.0+)
    - MySQL Workbench connection steps: Host=localhost, Port=3306, Username=root, Password=[as set]
    - Database creation: `CREATE DATABASE carddemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
    - Connection string format: `jdbc:mysql://localhost:3306/carddemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4`
    - Environment variable setup: `export DB_USERNAME=root; export DB_PASSWORD=...` (or Windows SET)
    - Example MySQL queries for verification
    - Troubleshooting: Common connection errors and fixes
  - **Files**: `docs/MYSQL_SETUP.md`
  - **Verify**: File exists and contains setup instructions

- [ ] **Step 12: Create test verification report documentation**
  - Create `docs/MYSQL_TEST_RESULTS.md`:
    - Test execution command and results
    - List of all 22 tests with Phase info (11 Phase 3, 11 Phase 4)
    - Connection details verified
    - Data persistence check results
    - Query examples executed
    - Any issues encountered and resolutions
  - **Files**: `docs/MYSQL_TEST_RESULTS.md`
  - **Verify**: File exists and contains test results

- [ ] **Step 13: Update README.md with MySQL configuration**
  - Add MySQL section to Prerequisites
  - Update Build and Run sections to note MySQL requirement for prod/test profiles
  - Add link to `docs/MYSQL_SETUP.md` for detailed setup
  - **Files**: `README.md`
  - **Verify**: README reflects MySQL configuration and H2 is marked as legacy/dev-only

### Phase 10: Backward Compatibility Check

- [ ] **Step 14: Verify all phases (1-4) work with MySQL**
  - Phase 1 (Accounts & Cards): Run account and card tests
  - Phase 2 (Customers): Verify customer entity and repository work
  - Phase 3 (Transactions): Run TransactionControllerTest (11 tests)
  - Phase 4 (Authorizations): Run AuthorizationControllerTest (11 tests)
  - All constraints, indexes, and foreign keys must be present
  - **Files**: None (verification step)
  - **Verify**: All 22 tests pass

### Phase 11: Build Artifact Verification

- [ ] **Step 15: Create production-ready JAR and verify it works**
  - Command: `mvn clean package`
  - Verify: `target/carddemo-1.0.0.jar` is created
  - Verify: No H2 dependencies in JAR (check: `jar tf target/carddemo-1.0.0.jar | grep h2` returns nothing)
  - Verify: MySQL driver is included: `jar tf target/carddemo-1.0.0.jar | grep mysql-connector`
  - Test JAR with MySQL: `java -jar target/carddemo-1.0.0.jar --spring.profiles.active=prod`
  - Verify: App starts successfully and connects to MySQL
  - **Files**: None (build step)
  - **Verify**: JAR builds, contains MySQL driver, app starts with prod profile

---

## Error Handling & Recovery

**If MySQL server is not running:**
- Provide: `MySQL server is not accessible on localhost:3306. Please start MySQL service.`
- For Windows: `net start MySQL80` (as Administrator)
- For macOS: `brew services start mysql` or `mysql.server start`
- For Linux: `sudo systemctl start mysql` or `sudo service mysql start`
- Retry connection check after service starts

**If tests fail with connection errors:**
- Check MySQL credentials: `mysql -u [username] -p[password] -h localhost -e "SELECT 1"`
- Check test database exists: `mysql -u [username] -p[password] -e "SHOW DATABASES LIKE 'carddemo_test'"`
- If missing, create: `mysql -u root -e "CREATE DATABASE carddemo_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"`

**If tests fail with schema errors:**
- Verify schema.sql is valid: `mysql -u root carddemo_test < src/main/resources/schema-mysql.sql`
- Check Hibernate dialect is set to MySQL8Dialect in all profiles
- Verify ddl-auto=create-drop for test profile (so schema auto-initializes)

**If data does not persist:**
- Verify ddl-auto is NOT `create-drop` in prod profile (should be `update` or `validate`)
- Check transaction is not rolled back: Review Spring transaction boundaries
- Verify MySQL is accepting writes: Test with `INSERT` via MySQL CLI directly

---

## Files Summary

**Modified:**
1. `pom.xml` — Remove H2, add MySQL driver
2. `src/main/resources/application.yml` — MySQL connection for all profiles
3. `src/main/resources/application-dev.yml` — MySQL + dev-specific settings
4. `src/main/resources/application-prod.yml` — MySQL + prod-specific settings (env var credentials)

**Created:**
1. `src/main/resources/schema-mysql.sql` — MySQL-specific DDL (UTF-8, all tables, indexes, FK)
2. `src/main/resources/application-test.yml` — Test profile with separate test database
3. `src/main/resources/mysql-setup.sql` — MySQL Workbench manual setup script
4. `docs/MYSQL_SETUP.md` — MySQL installation, Workbench setup, troubleshooting
5. `docs/MYSQL_TEST_RESULTS.md` — Test execution results and verification evidence
6. Updated `README.md` — Reference to MySQL setup and configuration

---

## Verification Checklist

- [ ] MySQL server runs and is reachable (`mysql -u root -e "SELECT 1"` succeeds)
- [ ] pom.xml has no H2 dependency and includes mysql-connector-j 8.4.0+
- [ ] application.yml uses MySQL connection string with UTF-8 charset
- [ ] application-dev.yml uses MySQL with create-drop for dev
- [ ] application-prod.yml uses MySQL with environment variables and validate mode
- [ ] application-test.yml exists and points to carddemo_test database
- [ ] schema-mysql.sql is valid MySQL syntax and includes all Phase 1-4 tables
- [ ] All 22 tests pass: `mvn clean test -Dspring.profiles.active=test` → 22 passed, 0 failed
- [ ] Data persists across app restart (verified with SELECT queries)
- [ ] No H2 references remain in code or config
- [ ] JAR file includes mysql-connector-j and excludes H2
- [ ] Documentation updated with MySQL setup and connection details

---

## Rollback/Fallback Plan

If MySQL migration fails irreparably:
1. Revert pom.xml to include H2, remove MySQL driver
2. Revert application.yml, application-dev.yml to H2 JDBC URLs and H2Dialect
3. Keep application-prod.yml with MySQL config for production-only use
4. Switch to dev profile: `mvn clean test -Dspring.profiles.active=dev` uses H2 again

**Recommendation**: Keep Git history so any branch can be reverted.

