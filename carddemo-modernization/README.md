# CardDemo Modernization

Java 17 Spring Boot 3.x REST API backend for credit card account management system, converted from COBOL using AWS Transform reverse engineering documentation.

## Overview

This project modernizes a legacy COBOL credit card processing system into a contemporary Java/Spring Boot microservice. Phase 1 implements core REST endpoints for account, card, transaction, and authorization management with H2 (dev) and MySQL (prod) database support.

**Source COBOL Programs Implemented**:
- CBACT01C-04C: Account management, balance tracking, interest calculation
- CBTRN01C-03C: Transaction posting, settlement, reporting
- COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C: Authorization processing
- COCRDUPC: Card issuance and reissue
- COTRTUPC: Transaction type definitions

## Quick Start

### Prerequisites

- Java 17 or later
- Maven 3.8+
- MySQL 8.0+ (for production; H2 for development)

### Build

```bash
mvn clean package
```

Builds executable JAR with tests and code coverage validation (minimum 80%).

### Run (Development)

```bash
java -jar target/carddemo-*.jar --spring.profiles.active=dev
```

- H2 in-memory database: jdbc:h2:mem:carddemo
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Run (Production)

```bash
export MYSQL_USER=carddemo
export MYSQL_PASSWORD=SecurePassword123
java -jar target/carddemo-*.jar --spring.profiles.active=prod
```

MySQL database connection required; set credentials via environment variables or `application-prod.yml`.

## Project Structure

```
carddemo-modernization/
├── src/main/java/com/carddemo/
│   ├── entity/              # JPA entities (Account, Card, Transaction, Authorization, etc.)
│   ├── repository/          # Spring Data JPA repositories
│   ├── service/             # Business logic services
│   ├── controller/          # REST API endpoints
│   ├── dto/                 # Data transfer objects
│   ├── mapper/              # MapStruct entity ↔ DTO mappers
│   ├── exception/           # Custom exception classes
│   ├── config/              # Spring configuration (GlobalExceptionHandler, SwaggerConfig)
│   └── CarddemoApplication.java  # Main entry point
├── src/main/resources/
│   ├── application.yml      # Common configuration
│   ├── application-dev.yml  # Development (H2) profile
│   ├── application-prod.yml # Production (MySQL) profile
│   ├── schema.sql           # Database schema (DDL)
│   └── data.sql             # Sample data (DML)
├── src/test/java/           # JUnit 5 unit and integration tests
├── docs/
│   ├── data-model.md        # Entity relationships, PIC mappings, assumptions
│   ├── api-spec.md          # REST endpoint specifications
│   └── deployment-guide.md  # Build, deploy, troubleshoot instructions
├── pom.xml                  # Maven configuration
└── README.md                # This file
```

## API Endpoints (21 Total)

### Accounts (6 endpoints)
- `GET /api/accounts/{id}` – Retrieve account
- `PUT /api/accounts/{id}` – Update account
- `GET /api/accounts/{id}/transactions` – List account transactions
- `POST /api/accounts/{id}/balance` – Post debit/credit
- `POST /api/accounts/{id}/reissue` – Reissue card
- `GET /api/accounts/{id}/cards` – List account cards

### Cards (3 endpoints)
- `GET /api/cards/{number}` – Retrieve card
- `GET /api/cards/account/{accountId}` – List cards by account
- `POST /api/cards` – Issue new card

### Transactions (4 endpoints)
- `GET /api/transactions/{id}` – Retrieve transaction
- `POST /api/transactions` – Post new transaction
- `POST /api/transactions/settle` – Settle transaction
- `GET /api/reports/transactions` – Transaction report

### Authorizations (5 endpoints)
- `POST /api/authorizations` – Request authorization
- `GET /api/authorizations/{id}` – Retrieve authorization
- `GET /api/authorizations/pending` – List pending authorizations
- `PUT /api/authorizations/{id}/review` – Review authorization
- `POST /api/authorizations/expire` – Expire old authorizations

### Example: Authorize Transaction

```bash
curl -X POST http://localhost:8080/api/authorizations \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "cardNumber": "4111111111111111",
    "amount": 500.00,
    "merchantId": "M125",
    "merchantName": "Amazon"
  }'
```

Response (201 Created):
```json
{
  "id": "AUTH1730860200000",
  "accountId": 1,
  "cardNumber": "4111111111111111",
  "amount": 500.00,
  "requestDate": "2026-11-06",
  "responseDate": "2026-11-06",
  "expiryDate": "2026-11-09",
  "status": "APPROVED",
  "declineReasonCode": null,
  "approvedAmount": 500.00
}
```

## Data Model

### Entities

1. **Account** – Credit card account with balance and credit limit (CBACT01C-04C)
2. **Card** – Physical/virtual card linked to account (COCRDUPC)
3. **Customer** – Account holder profile (COUSR00C-03C)
4. **Transaction** – Debit/credit transaction history (CBTRN01C-03C)
5. **Authorization** – Transaction authorization requests (COPAUA0C)
6. **TransactionType** – Transaction type definitions (COTRTUPC)

### Entity Relationships

- Account 1:N → Card
- Account 1:N → Transaction
- Account 1:N → Authorization
- Card, Transaction, Authorization reference Account (foreign keys)

### PIC Clause Mappings

| COBOL PIC | Meaning | Java Type |
|-----------|---------|-----------|
| 9(12) | Numeric | Long |
| COMP-3 9(9)V99 | Packed decimal with 2 decimals | BigDecimal(12,2) |
| X(50) | Character string | String |
| 9(8) YYYYMMDD | Date | LocalDate |
| X(1) Y/N | Boolean flag | Boolean |

See `docs/data-model.md` for full specification.

## Configuration

### Development Profile (H2)

Auto-initializes in-memory H2 database with schema and sample data on startup.

```yaml
spring.profiles.active=dev
spring.datasource.url=jdbc:h2:mem:carddemo
spring.jpa.hibernate.ddl-auto=create-drop
```

### Production Profile (MySQL)

```yaml
spring.profiles.active=prod
spring.datasource.url=jdbc:mysql://localhost:3306/carddemo
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

## Testing

### Run Tests

```bash
mvn clean test
```

### Generate Coverage Report

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

**Target**: ≥80% line and branch coverage

### Test Classes

- `CarddemoApplicationTest` – Context loads successfully
- `AccountRepositoryTest` – Repository finder methods
- `AccountServiceTest` – Service business logic with mocked repositories
- `AccountControllerTest` – REST endpoints with MockMvc

Additional test classes for Card, Transaction, Authorization services and controllers provided as templates.

## Documentation

- **data-model.md** – Entity relationships, PIC clause mappings, design assumptions
- **api-spec.md** – REST endpoint specifications, request/response examples, workflows
- **deployment-guide.md** – Build, deploy to dev/prod, Docker, troubleshooting

## Technologies

- **Java 17** – Target language (LTS release)
- **Spring Boot 3.3.5** – Application framework
- **Spring Data JPA** – Database abstraction
- **Hibernate ORM** – Object-relational mapping
- **MapStruct 1.5.5** – Entity ↔ DTO mapping
- **Lombok 1.18.30** – Reduce boilerplate (getters, setters, constructors)
- **Jakarta Validation** – Input validation
- **SpringDoc OpenAPI 2.0.4** – Swagger/OpenAPI documentation
- **JUnit 5** – Unit testing framework
- **Mockito** – Mocking framework
- **JaCoCo** – Code coverage measurement
- **H2 1.4.x** – In-memory database (dev profile)
- **MySQL Connector/J 8.0.35** – MySQL JDBC driver
- **Maven 3.8+** – Build tool

## Business Rules Implemented

### Account (CBACT01C-04C)
- R00001: Account lookup and balance tracking
- R00003: Active status controls transaction eligibility
- R00008: Cycle debit default 2525.00
- R00009: Cycle credit tracking for payments
- R00005: Cash limit ≤ credit limit

### Transaction (CBTRN01C-03C)
- R00001: Create transaction with status=PENDING
- R00005: Post date typically 1-2 days after transaction date
- R00006: Transaction status lifecycle (PENDING → POSTED → SETTLED)

### Authorization (COPAUA0C)
- R00013: Authorization Approval Decision logic
- R00016: Decline reason codes (3100, 4100, 4200, 4300, 5100, 5200, 9000)
- R00010: Pending authorization summary for available credit calculation

### Card Reissue (COCRDUPC)
- Block old cards, create new card with ACTIVE status

### Authorization Expiry (CBPAUP0C)
- 3-day default expiry window for pending authorizations

See `docs/data-model.md` for complete list of assumptions.

## Performance Considerations

- **Phase 1**: Single instance, modest scale (<1000 accounts, <10k transactions/day)
- **Phase 2**: Add distributed caching (Redis), horizontal scaling, database replication

## Security

- **Phase 1**: No authentication (dev/test only)
- **Phase 2**: OAuth2/JWT security, HTTPS/TLS, rate limiting, request signing

## Error Handling

All errors return standardized JSON response:

```json
{
  "timestamp": "2026-11-06T10:30:00",
  "code": "ACCOUNT_NOT_FOUND",
  "message": "Account not found with ID: 999",
  "path": "/api/accounts/999",
  "status": 404
}
```

HTTP status codes:
- 200: OK
- 201: Created
- 400: Bad Request (validation error)
- 404: Not Found
- 409: Conflict (insufficient funds, expired auth, duplicate)
- 500: Server Error

## Next Steps

### Phase 2 Enhancements

1. **React Front-End**: User interface for account management
2. **MQ Integration**: Real-time authorization message processing
3. **OAuth2 Security**: JWT token-based authentication
4. **Caching**: Redis for account/card lookups
5. **Horizontal Scaling**: Load balancing, database replication
6. **Batch Processing**: Spring Scheduler for interest calculation, authorization cleanup
7. **Advanced Monitoring**: ELK stack, Prometheus metrics, Datadog integration
8. **Database Migration**: Flyway for schema versioning

## Contributing

1. Create feature branch: `git checkout -b feature/xxx`
2. Write tests for new features (>80% coverage)
3. Ensure build passes: `mvn clean verify`
4. Commit changes: `git commit -m "feat: description"`
5. Push branch and create pull request

## License

Proprietary – CardDemo Modernization Project

---

**Version**: 1.0.0  
**Status**: Phase 1 Complete  
**Last Updated**: 2026-11-06  
**Target Deploy**: Spring 2026
