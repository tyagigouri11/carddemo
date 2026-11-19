# CardDemo Deployment Guide

## Prerequisites

- **Java 17** or later (LTS)
- **Maven 3.8+**
- **Git** (optional, for version control)
- **MySQL 8.0+** (for production profile; H2 used for development)

## Installation

### 1. Clone or Download Repository

```bash
cd /path/to/carddemo-modernization
```

### 2. Build Project

```bash
mvn clean package
```

This will:
- Compile Java sources
- Run unit and integration tests
- Generate JaCoCo code coverage report (target/site/jacoco/index.html)
- Create executable JAR (target/carddemo-*.jar)
- Verify minimum 80% code coverage

**Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45 s
[INFO] Finished at: 2026-11-06T10:30:00Z
```

### 3. Verify JAR Creation

```bash
ls -la target/carddemo-*.jar
```

---

## Development Profile (H2 In-Memory Database)

### Start Application

```bash
java -jar target/carddemo-*.jar --spring.profiles.active=dev
```

**Expected Output**:
```
...
2026-11-06 10:30:00.123  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
2026-11-06 10:30:00.456  INFO 12345 --- [main] c.c.CarddemoApplication                  : Started CarddemoApplication in 5.123 seconds
```

### Access Application

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:carddemo`
  - Username: `sa`
  - Password: (blank)

### Test Endpoints

```bash
# Get account
curl http://localhost:8080/api/accounts/1

# Expected response
{
  "id": 1,
  "creditLimit": 50000.00,
  "currentBalance": 25000.00,
  "activeStatus": true,
  ...
}
```

### Sample Data

Sample data is automatically loaded from `data.sql` on startup:
- 5 customer records
- 5 account records
- 10 card records
- 20 transaction records
- 5 authorization records

**Query sample data in H2**:
```sql
SELECT * FROM account;
SELECT COUNT(*) FROM transaction WHERE status = 'PENDING';
SELECT * FROM authorization WHERE status = 'DECLINED';
```

---

## Production Profile (MySQL Database)

### 1. Create MySQL Database

**On MySQL server** (or Docker container):

```sql
CREATE DATABASE carddemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'carddemo'@'localhost' IDENTIFIED BY 'SecurePassword123';
GRANT ALL PRIVILEGES ON carddemo.* TO 'carddemo'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Initialize Schema

```bash
mysql -u carddemo -p carddemo < src/main/resources/schema.sql
mysql -u carddemo -p carddemo < src/main/resources/data.sql
```

Or let Spring Boot initialize automatically (set `ddl-auto=create` in application-prod.yml first):

```bash
java -jar target/carddemo-*.jar --spring.profiles.active=prod
```

**Note**: Change `ddl-auto` back to `validate` after first run to protect existing data.

### 3. Configure Environment Variables

Set MySQL credentials (replace with actual values):

**Linux/macOS**:
```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=carddemo
export MYSQL_USER=carddemo
export MYSQL_PASSWORD=SecurePassword123
```

**Windows (PowerShell)**:
```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DATABASE="carddemo"
$env:MYSQL_USER="carddemo"
$env:MYSQL_PASSWORD="SecurePassword123"
```

**Or update application-prod.yml directly**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/carddemo?useSSL=false&serverTimezone=UTC
    username: carddemo
    password: SecurePassword123
```

### 4. Start Application

```bash
java -jar target/carddemo-*.jar --spring.profiles.active=prod
```

### 5. Verify Connection

```bash
curl http://localhost:8080/api/accounts/1

# Should return account data from MySQL
```

---

## Docker Deployment

### 1. Build Docker Image

Create `Dockerfile` in project root:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/carddemo-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
```

Build image:
```bash
docker build -t carddemo:1.0.0 .
```

### 2. Run with Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: carddemo
      MYSQL_USER: carddemo
      MYSQL_PASSWORD: SecurePassword123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
      - ./src/main/resources/data.sql:/docker-entrypoint-initdb.d/data.sql

  carddemo:
    image: carddemo:1.0.0
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/carddemo?useSSL=false
      SPRING_DATASOURCE_USERNAME: carddemo
      SPRING_DATASOURCE_PASSWORD: SecurePassword123
    depends_on:
      - mysql
    restart: unless-stopped

volumes:
  mysql_data:
```

Start services:
```bash
docker-compose up -d
```

Check logs:
```bash
docker-compose logs -f carddemo
```

---

## Health Checks

### Application Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status": "UP"}
```

### Database Connectivity

Query via API:
```bash
curl http://localhost:8080/api/accounts/1
```

Query via database client:
```bash
mysql -u carddemo -p carddemo -e "SELECT COUNT(*) FROM account;"
```

---

## Configuration

### application.yml (Common)

```yaml
spring:
  application:
    name: carddemo
  jpa:
    hibernate:
      ddl-auto: validate  # change to create only for first run
    show-sql: false
  jackson:
    default-property-inclusion: non_null

server:
  port: 8080
  servlet:
    context-path: /

logging:
  level:
    root: INFO
    com.carddemo: DEBUG
```

### application-dev.yml (Development)

H2 in-memory database with auto-initialization.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:carddemo;MODE=MySQL
    driverClassName: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql
      schema-locations: classpath:schema.sql
```

### application-prod.yml (Production)

MySQL connection with validation (not creation).

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/carddemo?useSSL=false&serverTimezone=UTC
    driverClassName: com.mysql.cj.jdbc.Driver
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
```

---

## Monitoring & Logs

### Log Files

Logs output to console by default. To log to file, modify `logback-spring.xml`:

```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <file>logs/carddemo.log</file>
  <encoder>
    <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
  </encoder>
</appender>
```

### Actuator Endpoints (Phase 2)

Enable additional endpoints in `application-prod.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

Access: http://localhost:8080/actuator

---

## Testing

### Run All Tests

```bash
mvn clean test
```

### Run Specific Test

```bash
mvn test -Dtest=AccountServiceTest
```

### Generate Coverage Report

```bash
mvn clean test jacoco:report
```

Open report:
- Linux/macOS: `open target/site/jacoco/index.html`
- Windows: `start target\site\jacoco\index.html`

Expected: ≥80% line and branch coverage

---

## Troubleshooting

### Port 8080 Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Or use different port
java -jar target/carddemo-*.jar --spring.profiles.active=dev --server.port=8081
```

### MySQL Connection Refused

```bash
# Verify MySQL is running
mysql -u carddemo -p -e "SELECT 1;"

# Check connection string in application-prod.yml
```

### H2 Console Not Accessible

Ensure `spring.h2.console.enabled=true` in `application-dev.yml`

### Tests Fail with Timeout

Increase test timeout in `pom.xml`:
```xml
<maven.failsafe.timeout>120000</maven.failsafe.timeout>
```

---

## Performance Tuning

### Increase Heap Size

```bash
java -Xmx512m -Xms256m -jar target/carddemo-*.jar --spring.profiles.active=prod
```

### Enable Query Caching (MySQL)

```sql
SET GLOBAL query_cache_size = 268435456;
SET GLOBAL query_cache_type = 1;
```

### Add Indexes

Indexes are defined in `schema.sql`; key indexes:
- Account: `idx_active_status`, `idx_reissue_date`
- Card: `idx_account_id`, `idx_status_expiry`
- Transaction: `idx_status_date`, `idx_account_id`
- Authorization: `idx_status_expiry`

---

## Rollback Procedure

### If Deployment Fails

1. Stop application: `Ctrl+C` or `docker-compose down`
2. Revert to previous JAR version
3. Restart with previous version

### Database Backup Before Deployment

```bash
# MySQL backup
mysqldump -u carddemo -p carddemo > carddemo_backup.sql

# Restore if needed
mysql -u carddemo -p carddemo < carddemo_backup.sql
```

---

## Security Considerations (Phase 2)

- Phase 1: No authentication/authorization (dev/test only)
- Phase 2: Implement OAuth2/JWT
- Phase 2: Use HTTPS/TLS for production
- Phase 2: Implement request signing for sensitive operations
- Phase 2: Add API rate limiting
- Phase 2: Use Vault for secrets management

---

## Next Steps

1. **Verify build**: `mvn clean verify` passes with ≥80% coverage
2. **Test endpoints**: Use Swagger UI or curl to test all CRUD operations
3. **Load testing**: Benchmark with 100+ concurrent users (Phase 2)
4. **Security audit**: Penetration testing for production deployment (Phase 2)

---

**Deployment Guide Version**: 1.0.0
**Last Updated**: 2026-11-06
