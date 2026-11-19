# API Integration Guide

## Overview

This frontend integrates with Phase 1 Java Spring Boot backend via REST API. All endpoints verified from actual controller code.

**Backend URL**: `http://localhost:8080` (development)
**Proxy**: Vite routes `/api/*` to backend (see `vite.config.ts`)

## Verified Backend Endpoints

Total: **16 endpoints** across 4 domains.

### Account Management (6 endpoints)

| Method | Path | Request | Response | Notes |
|--------|------|---------|----------|-------|
| GET | `/api/accounts/{id}` | - | AccountDto | Retrieve account details |
| PUT | `/api/accounts/{id}` | AccountDto | AccountDto | Update creditLimit, currentBalance, activeStatus |
| GET | `/api/accounts/{id}/transactions` | Query: `from`, `to` (dates) | List[Transaction] | Get account transactions with optional date filter |
| POST | `/api/accounts/{id}/balance` | `{amount, type, merchantId, merchantName}` | AccountDto | Update balance (process payment/debit) |
| POST | `/api/accounts/{id}/reissue` | `{cardNumber, expiryDate}` | CardDto | Reissue card for account |
| GET | `/api/accounts/{id}/cards` | - | List[CardDto] | Get all cards for account |

### Card Management (3 endpoints)

| Method | Path | Request | Response | Notes |
|--------|------|---------|----------|-------|
| GET | `/api/cards/{number}` | - | CardDto | Get card by card number (PAN) |
| POST | `/api/cards` | CardDto | CardDto | Issue new card |
| GET | `/api/cards/account/{accountId}` | - | List[CardDto] | Get cards by account ID |

### Transaction Management (4 endpoints)

| Method | Path | Request | Response | Notes |
|--------|------|---------|----------|-------|
| POST | `/api/transactions` | TransactionRequest | TransactionDto | Post new transaction |
| GET | `/api/transactions/{id}` | - | TransactionDto | Get transaction details |
| POST | `/api/transactions/settle` | `{transactionId}` | TransactionDto | Settle/close transaction |
| GET | `/api/transactions/reports/transactions` | Query: `asOf` (date) | Map | Generate transaction report |

### Authorization Workflow (5 endpoints)

| Method | Path | Request | Response | Notes |
|--------|------|---------|----------|-------|
| POST | `/api/authorizations` | AuthorizationRequest | AuthorizationDto | Create authorization request |
| GET | `/api/authorizations/{id}` | - | AuthorizationDto | Get authorization details |
| GET | `/api/authorizations/pending` | - | List[AuthorizationDto] | Get all pending authorizations |
| PUT | `/api/authorizations/{id}/review` | `{decision, notes, approvedAmount}` | AuthorizationDto | Review and approve/decline |
| POST | `/api/authorizations/expire` | - | `{expiredCount}` | Expire old pending authorizations |

## Data Transfer Objects (DTOs)

### Account DTO

```typescript
interface AccountDto {
  id: string
  creditLimit: number
  currentBalance: number
  activeStatus: 'ACTIVE' | 'BLOCKED' | 'CLOSED'
  reissueDate?: string | null
  openDate: string
  expirationDate?: string | null
  cashCreditLimit: number
  currentCycleDebit: number
  currentCycleCredit: number
  groupId?: string | null
}
```

### Card DTO

```typescript
interface CardDto {
  id: string
  number: string               // PAN (Primary Account Number)
  accountId: string
  status: 'ACTIVE' | 'EXPIRED' | 'BLOCKED'
  expiryDate: string          // Format: MM/YY
}
```

### Transaction DTO

```typescript
interface TransactionDto {
  id: string
  accountId: string
  cardNumber: string
  amount: number
  date: string
  postDate?: string | null
  type: 'PURCHASE' | 'CASH_ADVANCE' | 'PAYMENT' | 'FEE' | 'INTEREST'
  status: 'PENDING' | 'POSTED' | 'SETTLED'
  merchantId: string
  merchantName: string
  processingCode?: string | null
}
```

### Authorization DTO

```typescript
interface AuthorizationDto {
  id: string
  accountId: string
  cardNumber: string
  amount: number
  merchantId: string
  merchantName: string
  requestDate: string
  responseDate?: string | null
  expiryDate: string
  status: 'PENDING' | 'APPROVED' | 'DECLINED'  // READ_ONLY
  declineReasonCode?: string | null
  approvedAmount?: number | null
}
```

## Service Modules

Each domain has a dedicated service module in `src/services/`:

### `accountService.ts`

```typescript
// Fetch account
const account = await accountService.getAccount('acc-123')

// Update account
await accountService.updateAccount('acc-123', { creditLimit: 5000 })

// Get account transactions
const txs = await accountService.getAccountTransactions('acc-123', {
  from: '2024-01-01',
  to: '2024-01-31'
})

// Update balance
await accountService.updateBalance('acc-123', {
  amount: -50.00,
  type: 'PAYMENT',
  merchantId: 'merch-1',
  merchantName: 'Merchant Name'
})

// Reissue card
await accountService.reissueCard('acc-123', {
  cardNumber: '4532123456789010',
  expiryDate: '12/26'
})

// Get account cards
const cards = await accountService.getAccountCards('acc-123')
```

### `cardService.ts`

```typescript
// Get card by number
const card = await cardService.getCard('4532123456789010')

// Issue new card
await cardService.issueCard({
  accountId: 'acc-123',
  number: '4532123456789010',
  expiryDate: '12/26'
})

// Get cards by account
const cards = await cardService.getCardsByAccount('acc-123')
```

### `transactionService.ts`

```typescript
// Get transaction
const tx = await transactionService.getTransaction('tx-123')

// Post transaction
const newTx = await transactionService.postTransaction({
  accountId: 'acc-123',
  cardNumber: '4532123456789010',
  amount: 99.99,
  type: 'PURCHASE',
  merchantId: 'merch-1',
  merchantName: 'Store Name'
})

// Settle transaction
await transactionService.settleTransaction('tx-123')

// Generate report
const report = await transactionService.generateReport({
  asOf: '2024-01-31'
})
```

### `authorizationService.ts`

```typescript
// Get authorization
const auth = await authorizationService.getAuthorization('auth-123')

// Request authorization
const newAuth = await authorizationService.authorizeTransaction({
  accountId: 'acc-123',
  cardNumber: '4532123456789010',
  amount: 500,
  merchantId: 'merch-1',
  merchantName: 'Store Name'
})

// Get pending authorizations
const pending = await authorizationService.getPendingAuthorizations()

// Review authorization (approve/decline)
await authorizationService.processAuthorizationReview('auth-123', {
  decision: 'APPROVED',
  approvedAmount: 500,
  notes: 'Approved by manager'
})

// Expire old authorizations
const result = await authorizationService.expireAuthorizations()
// { expiredCount: 5 }
```

## Using Services in Components

### Option 1: Zustand Store (Recommended)

```tsx
import { useAccountStore } from '@/store/accountStore'
import { useUiStore } from '@/store/uiStore'

export const MyComponent = () => {
  const loadAccount = useAccountStore(s => s.loadAccount)
  const selectedAccount = useAccountStore(s => s.selectedAccount)
  const loading = useAccountStore(s => s.loading)
  const error = useAccountStore(s => s.error)
  const addToast = useUiStore(s => s.addToast)

  useEffect(() => {
    loadAccount('acc-123')
  }, [])

  if (loading) return <Loading />
  if (error) return <ErrorBanner message={error} onDismiss={() => {}} />

  return <div>{selectedAccount?.creditLimit}</div>
}
```

### Option 2: Direct Service Call

```tsx
import { accountService } from '@/services/accountService'

export const MyComponent = () => {
  const [account, setAccount] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    accountService
      .getAccount('acc-123')
      .then(setAccount)
      .catch(err => setError((err as ApiError).message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Loading />
  if (error) return <ErrorBanner message={error} onDismiss={() => {}} />

  return <div>{account?.creditLimit}</div>
}
```

## Error Handling

### Error Types

All API errors wrapped in `ApiError` interface:

```typescript
interface ApiError {
  status: number
  message: string        // User-friendly message
  code: string          // Error code from backend
  timestamp?: string
}
```

### Status Code Mapping

| Status | Message | Reason |
|--------|---------|--------|
| 400 | Invalid input. Please check your data. | Validation failed |
| 401 | Session expired. Please log in again. | Auth token invalid/expired |
| 403 | You do not have permission to perform this action. | Authorization failed |
| 404 | Resource not found. | Entity doesn't exist |
| 409 | Conflict. This resource may already exist. | Duplicate entity |
| 422 | Validation failed. Please check your input. | Business validation |
| 500 | Server error. Please try again later. | Backend error |
| 502 | Gateway error. Please try again later. | Nginx/proxy error |
| 503 | Service unavailable. Please try again later. | Maintenance/overload |
| Network timeout | Your network may be down. Check your connection. | Connection lost |

### Handling Errors in Components

```tsx
try {
  await postTransaction(data)
  addToast({ message: 'Success!', type: 'success' })
} catch (error) {
  const apiError = error as ApiError
  addToast({ message: apiError.message, type: 'error' })
}
```

## Request/Response Examples

### Example: Post Transaction

**Request**:
```
POST /api/transactions
Content-Type: application/json
Authorization: Bearer <token>

{
  "accountId": "acc-123",
  "cardNumber": "4532123456789010",
  "amount": 99.99,
  "type": "PURCHASE",
  "merchantId": "merch-abc",
  "merchantName": "Store ABC"
}
```

**Response** (200 OK):
```json
{
  "id": "tx-456",
  "accountId": "acc-123",
  "cardNumber": "4532123456789010",
  "amount": 99.99,
  "date": "2024-01-15T10:30:00Z",
  "postDate": null,
  "type": "PURCHASE",
  "status": "PENDING",
  "merchantId": "merch-abc",
  "merchantName": "Store ABC",
  "processingCode": null
}
```

**Response** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Invalid input. Please check your data.",
  "code": "VALIDATION_ERROR",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Example: Authorize Transaction

**Request**:
```
POST /api/authorizations
Content-Type: application/json
Authorization: Bearer <token>

{
  "accountId": "acc-123",
  "cardNumber": "4532123456789010",
  "amount": 5000,
  "merchantId": "merch-def",
  "merchantName": "Hotel XYZ"
}
```

**Response** (200 OK):
```json
{
  "id": "auth-789",
  "accountId": "acc-123",
  "cardNumber": "4532123456789010",
  "amount": 5000,
  "merchantId": "merch-def",
  "merchantName": "Hotel XYZ",
  "requestDate": "2024-01-15T10:30:00Z",
  "responseDate": null,
  "expiryDate": "2024-01-22T10:30:00Z",
  "status": "PENDING",
  "declineReasonCode": null,
  "approvedAmount": null
}
```

## Proxy Configuration

Vite dev server proxies `/api/*` to backend:

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
}
```

This means:
- Frontend request: `GET /api/accounts/123`
- Proxied to: `GET http://localhost:8080/accounts/123`

**For production** (no proxy):
- Update `.env.production`: `VITE_API_URL=https://api.carddemo.com`
- Deploy Nginx to reverse-proxy `/api` to backend
- See `DEPLOYMENT.md` for details

## Retry Logic

API client automatically retries on:
- `5xx` errors (server, gateway errors)
- Connection timeouts

**Retry strategy**: Exponential backoff
- 1st retry: 2 seconds
- 2nd retry: 4 seconds
- 3rd retry: 8 seconds
- Max retries: 3

Example: If a request times out, it will retry automatically 3 times before failing.

## Authentication

Currently using placeholder mock auth (localStorage).

**For OAuth2 integration**:

1. Update `AuthContext.tsx`:

```typescript
const login = async (email: string, password: string) => {
  const response = await apiClient.post('/auth/login', { email, password })
  const { accessToken } = response.data
  localStorage.setItem('authToken', accessToken)
  setToken(accessToken)
}
```

2. Backend must provide `/api/auth/login` endpoint returning:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

3. Token automatically added to all requests:

```
Authorization: Bearer <accessToken>
```

## CORS Configuration

**Development**: Vite proxy handles CORS (transparent).

**Production**: Backend must enable CORS. Add to Spring Boot:

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedOrigins("https://carddemo.com")
      .allowedMethods("GET", "POST", "PUT", "DELETE")
      .allowedHeaders("*")
      .maxAge(3600);
  }
}
```

Or use `@CrossOrigin` on controllers:

```java
@CrossOrigin(origins = "https://carddemo.com")
@RestController
@RequestMapping("/api/accounts")
public class AccountController { ... }
```

## Testing API Integration

### Manual Testing (Browser)

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `npm run dev`
3. Open http://localhost:5173
4. Open DevTools → Network tab
5. Click around, watch API requests

**Check**:
- Requests go to `/api/*`
- Responses have correct data
- Errors show user-friendly messages

### Automated Testing

MSW (Mock Service Worker) mocks API calls in tests:

```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw'

export const handlers = [
  http.get('/api/accounts/:id', ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      creditLimit: 5000,
      currentBalance: 1234.56,
      activeStatus: 'ACTIVE'
    })
  }),
]
```

Tests use MSW automatically; no backend needed.

## Troubleshooting API Issues

### "Failed to fetch / Network error"

1. Check backend running: `curl http://localhost:8080/swagger-ui.html`
2. Check firewall not blocking port 8080
3. Check vite.config.ts proxy configured correctly
4. Restart dev server: `npm run dev`

### "401 Unauthorized"

1. Token in localStorage? Check DevTools → Application → Local Storage
2. Token valid? Login again
3. Backend expecting different auth header? Check backend docs

### "404 Not Found"

1. Check endpoint path matches service module
2. Check backend has endpoint (verify in swagger-ui)
3. Check URL parameters formatted correctly

### "500 Server Error"

1. Check backend logs for stack trace
2. Verify request data matches DTO fields
3. Check database connection (backend)
4. Restart backend

## Summary

- **16 verified endpoints** via REST API
- **Zustand stores** manage state and API calls
- **Automatic retry** on 5xx/timeout errors
- **User-friendly errors** (no HTTP codes)
- **Type-safe** with TypeScript DTOs matching backend
- **Proxy** handles CORS in development
- **Mock auth** ready for OAuth2 integration
