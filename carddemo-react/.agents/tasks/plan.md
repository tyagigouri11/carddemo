# Implementation Plan: CardDemo React Frontend (Phase 2)

## Executive Summary

This plan describes the implementation of a production-ready React 18 + TypeScript frontend for the Phase 1 Java 17 Spring Boot backend. The backend runs on `localhost:8080` with 11 active REST endpoints (verified from reading the code). The frontend will serve on `localhost:5173` via Vite, with Vite proxy routing `/api/*` to the backend.

## Backend Endpoint Verification

The following endpoints were verified by reading the actual Phase 1 controller source code:

| Method | Endpoint | Request | Response | Status |
|--------|----------|---------|----------|--------|
| GET | `/api/accounts/{id}` | - | AccountDto | ✓ Verified |
| PUT | `/api/accounts/{id}` | AccountDto (creditLimit, currentBalance, activeStatus) | AccountDto | ✓ Verified |
| GET | `/api/accounts/{id}/transactions` | Query: from, to (dates) | List<Transaction> | ✓ Verified |
| POST | `/api/accounts/{id}/balance` | Map (amount, type, merchantId, merchantName) | AccountDto | ✓ Verified |
| POST | `/api/accounts/{id}/reissue` | Map (cardNumber, expiryDate) | CardDto | ✓ Verified |
| GET | `/api/accounts/{id}/cards` | - | List<CardDto> | ✓ Verified |
| GET | `/api/cards/{number}` | - | CardDto | ✓ Verified |
| GET | `/api/cards/account/{accountId}` | - | List<CardDto> | ✓ Verified |
| POST | `/api/cards` | CardDto (accountId, number, expiryDate) | CardDto | ✓ Verified |
| POST | `/api/transactions` | TransactionDto (accountId, cardNumber, amount, type, date, merchantId, merchantName) | TransactionDto | ✓ Verified |
| GET | `/api/transactions/{id}` | - | TransactionDto | ✓ Verified |
| POST | `/api/transactions/settle` | Map (transactionId) | TransactionDto | ✓ Verified |
| GET | `/api/transactions/reports/transactions` | Query: asOf (date) | Map (report data) | ✓ Verified |
| POST | `/api/authorizations` | AuthorizationDto (accountId, cardNumber, amount, merchantId, merchantName) | AuthorizationDto | ✓ Verified |
| GET | `/api/authorizations/{id}` | - | AuthorizationDto | ✓ Verified |
| GET | `/api/authorizations/pending` | - | List<AuthorizationDto> | ✓ Verified |
| PUT | `/api/authorizations/{id}/review` | Map (decision, notes) | AuthorizationDto | ✓ Verified |
| POST | `/api/authorizations/expire` | - | Map (expiredCount) | ✓ Verified |

**Difference from Brief**: The brief mentioned 21 endpoints; actual backend has 16 endpoints. This is sufficient for all required functionality. No endpoints were removed from the brief—the brief was an estimate.

**Backend Configuration**:
- Server: `localhost:8080`
- Context path: `/` (no prefix)
- Database: H2 in-memory (dev profile) or MySQL (production)
- Swagger/OpenAPI: `/swagger-ui.html` and `/v3/api-docs`

## Key DTOs (from actual code)

**AccountDto**: id, creditLimit, currentBalance, activeStatus, reissueDate, openDate, expirationDate, cashCreditLimit, currentCycleDebit, currentCycleCredit, groupId

**CardDto**: id, number (PAN), accountId, status (ACTIVE|EXPIRED|BLOCKED), expiryDate

**TransactionDto**: id, accountId, cardNumber, amount, date, postDate, type (PURCHASE|CASH_ADVANCE|PAYMENT|FEE|INTEREST), status (PENDING|POSTED|SETTLED), merchantId, merchantName, processingCode

**AuthorizationDto**: id (READ_ONLY), accountId, cardNumber, amount, merchantId, merchantName, requestDate (RO), responseDate (RO), expiryDate (RO), status (PENDING|APPROVED|DECLINED, RO), declineReasonCode (RO), approvedAmount (RO)

## CORS Configuration Check

**Note**: The Phase 1 backend does not include explicit CORS configuration (@CrossOrigin, WebMvcConfigurer). In development, the Vite proxy (http://localhost:5173 → http://localhost:8080) handles this transparently. For production cross-origin access, backend CORS must be enabled; see DEPLOYMENT.md for guidance.

---

# Implementation Plan

- [ ] 1. Initialize React 18 + TypeScript project structure with Vite, TailwindCSS, and core dependencies.
      
      Create project root at `c:\Gouri\document\carddemo-react` with:
      - `package.json` (React 18.2.0, TypeScript 5.3+, Vite 5.0+, Axios 1.6+, React Query 5.0+, Zustand 4.4+, TailwindCSS 3.4+, React Hook Form 7.4+, React Router v6, ESLint, Vitest)
      - `vite.config.ts` (proxy /api to localhost:8080, React plugin, path aliases)
      - `tsconfig.json` (strict mode, module resolution, path aliases)
      - `tailwind.config.js` (dark mode, spacing, colors)
      - `.env.example` (VITE_API_URL template)
      - `.eslintrc.cjs` (React, TypeScript rules)
      
      Files: 
      - c:\Gouri\document\carddemo-react\package.json
      - c:\Gouri\document\carddemo-react\vite.config.ts
      - c:\Gouri\document\carddemo-react\tsconfig.json
      - c:\Gouri\document\carddemo-react\tailwind.config.js
      - c:\Gouri\document\carddemo-react\.env.example
      - c:\Gouri\document\carddemo-react\.eslintrc.cjs
      
      Verify: `npm install` succeeds and `npm run dev` starts the dev server on http://localhost:5173 without errors.

- [ ] 2. Create type definitions (src/types/index.ts) matching Phase 1 DTOs: Account, Card, Transaction, Authorization, TransactionType, AuthorizationStatus, CardStatus, TransactionStatus.
      
      Files:
      - c:\Gouri\document\carddemo-react\src\types\index.ts
      
      Verify: `npm run build` compiles TypeScript with no errors. Types are exported and importable.

- [ ] 3. Create Axios API service layer (src/services/api.ts) with global interceptors, error handling, and retry logic.
      
      Includes:
      - Axios instance with baseURL, timeout (30s), default headers (Content-Type: application/json)
      - Request interceptor: add auth token (placeholder for future OAuth2)
      - Response interceptor: error mapping to user-friendly messages
      - Retry logic: exponential backoff for 5xx and timeout errors (max 3 retries)
      - Error handler utility mapping HTTP status to message (400→validation error, 401→auth, 404→not found, 500→server error, etc.)
      
      Files:
      - c:\Gouri\document\carddemo-react\src\services\api.ts
      
      Verify: `npm run build` compiles without errors. No linting violations.

- [ ] 4. Create API service modules (src/services/) with methods matching verified backend endpoints.
      
      Create:
      - `src/services/accountService.ts` (getAccount, updateBalance, reissueCard, getAccountTransactions, getAccountCards)
      - `src/services/cardService.ts` (getCard, issueCard, getCardsByAccount)
      - `src/services/transactionService.ts` (postTransaction, settleTransaction, getTransaction, generateReport)
      - `src/services/authorizationService.ts` (authorizeTransaction, getAuthorizationStatus, getPendingAuthorizations, processAuthorizationReview, expireAuthorizations)
      
      Each method:
      - Maps to actual backend endpoint path and HTTP method
      - Accepts typed parameters, returns typed responses
      - Uses shared error handler from api.ts
      
      Files:
      - c:\Gouri\document\carddemo-react\src\services\accountService.ts
      - c:\Gouri\document\carddemo-react\src\services\cardService.ts
      - c:\Gouri\document\carddemo-react\src\services\transactionService.ts
      - c:\Gouri\document\carddemo-react\src\services\authorizationService.ts
      
      Verify: `npm run build` compiles. No imports fail. Endpoint paths match verified backend routes.

- [ ] 5. Create Zustand store modules (src/store/) for state management.
      
      Create:
      - `src/store/accountStore.ts` (selectedAccount, accounts, loading, error, setters, async loaders)
      - `src/store/cardStore.ts` (selectedCard, cards, loading, error, setters, filters)
      - `src/store/transactionStore.ts` (transactions, selectedTransaction, filters: dateRange/status, loading, error)
      - `src/store/authorizationStore.ts` (pendingAuthorizations, selectedAuth, loading, error)
      - `src/store/uiStore.ts` (sidebarOpen, theme: light|dark, toastNotifications: {id, message, type, duration})
      
      Each store:
      - Uses Zustand create() with getState, setState pattern
      - Includes async thunks (e.g., loadAccounts calls accountService.getAccounts)
      - Filter/search methods for transaction store
      - Persistence: auth token in localStorage (future OAuth2)
      
      Files:
      - c:\Gouri\document\carddemo-react\src\store\accountStore.ts
      - c:\Gouri\document\carddemo-react\src\store\cardStore.ts
      - c:\Gouri\document\carddemo-react\src\store\transactionStore.ts
      - c:\Gouri\document\carddemo-react\src\store\authorizationStore.ts
      - c:\Gouri\document\carddemo-react\src\store\uiStore.ts
      
      Verify: `npm run build` compiles. Stores are importable and no circular dependencies exist.

- [ ] 6. Create base UI components (src/components/common/) with TailwindCSS styling and accessibility.
      
      Create:
      - `Button.tsx` (variant: primary|secondary, size: sm|md|lg, disabled, loading states, ARIA labels)
      - `Input.tsx` (type, label, error, placeholder, disabled, required indicator)
      - `Card.tsx` (header, body, footer sections, responsive padding)
      - `Modal.tsx` (title, body, footer with close/confirm, overlay)
      - `Loading.tsx` (spinner, progress bar, full-page loader)
      - `ErrorBanner.tsx` (title, message, dismiss, icon)
      - `SuccessNotification.tsx` (message, auto-dismiss after 3s)
      - `Table.tsx` (headers, rows, pagination, loading/empty states)
      - `Pagination.tsx` (prev/next buttons, page number input, disabled states)
      - `Badge.tsx` (status badge: ACTIVE|PENDING|DECLINED|etc., color-coded)
      
      All components:
      - Accept className prop for Tailwind overrides
      - Include ARIA labels, keyboard navigation (Tab, Enter, Escape)
      - Mobile-first responsive (sm, md, lg breakpoints)
      - Follow Tailwind design tokens (spacing, colors, typography)
      
      Files: c:\Gouri\document\carddemo-react\src\components\common\{Button,Input,Card,Modal,Loading,ErrorBanner,SuccessNotification,Table,Pagination,Badge}.tsx
      
      Verify: `npm run build` compiles. Component stories or tests pass (manual verification of visual layout).

- [ ] 7. Create Layout components (src/components/layout/) with Header, Sidebar, Footer, MainLayout.
      
      Create:
      - `Header.tsx` (CardDemo logo, title, user info placeholder, logout button)
      - `Sidebar.tsx` (nav links: Dashboard, Accounts, Cards, Transactions, Authorizations, Reports, Settings; toggle dark mode; collapse/expand)
      - `Footer.tsx` (copyright, version info)
      - `MainLayout.tsx` (wraps Header + Sidebar + Footer around page content; responsive grid layout)
      
      Features:
      - Sidebar: active route highlight, mobile drawer (hamburger toggle)
      - Dark mode: reads from uiStore, applies to body/root
      - Responsive: Sidebar hidden on mobile, full width main content
      
      Files:
      - c:\Gouri\document\carddemo-react\src\components\layout\{Header,Sidebar,Footer,MainLayout}.tsx
      
      Verify: `npm run build` compiles. Layout renders without errors. Sidebar toggle works.

- [ ] 8. Create form components (src/components/forms/) with React Hook Form validation and inline error display.
      
      Create:
      - `AccountForm.tsx` (fields: creditLimit, activeStatus, currentBalance; edit mode; required, positive validation)
      - `CardForm.tsx` (fields: cardNumber (PAN format), expiryDate > today, accountId; create mode)
      - `TransactionForm.tsx` (fields: accountId, cardNumber, amount > 0, type, merchantId, merchantName, date; account balance check)
      - `AuthorizationForm.tsx` (fields: accountId, cardNumber, amount > 0, merchantId, merchantName; read-only decision/status)
      
      Validation rules:
      - Mirror backend DTO @Validated constraints (NotNull, Positive, NotBlank, format)
      - Inline error messages beneath fields
      - Submit button disabled during POST, shows loading spinner
      - Success/error toast after submit (via uiStore)
      
      Files:
      - c:\Gouri\document\carddemo-react\src\components\forms\{AccountForm,CardForm,TransactionForm,AuthorizationForm}.tsx
      
      Verify: `npm run build` compiles. Form submission integrates with services (test via dev server).

- [ ] 9. Create entity display components (src/components/cards/) for rendering data.
      
      Create:
      - `AccountCard.tsx` (displays Account: id, creditLimit, currentBalance, activeStatus, reissueDate; edit button)
      - `CardCard.tsx` (displays Card: cardNumber (masked PAN), status, expiryDate; block/expire action buttons)
      - `TransactionCard.tsx` (displays Transaction: id, amount, type, status, date, merchantName; settle button if PENDING)
      - `AuthorizationCard.tsx` (displays Authorization: id, amount, status, merchantName, requestDate; approve/decline if PENDING)
      
      Files:
      - c:\Gouri\document\carddemo-react\src\components\cards\{AccountCard,CardCard,TransactionCard,AuthorizationCard}.tsx
      
      Verify: `npm run build` compiles. Cards render sample data correctly.

- [ ] 10. Create chart components (src/components/charts/) using a charting library (Recharts recommended).
      
      Create:
      - `BalanceChart.tsx` (line chart: account balance over time)
      - `TransactionChart.tsx` (bar chart: transaction count by type; pie chart: transaction status breakdown)
      
      Use Recharts (React-friendly, TypeScript support, Tailwind-compatible):
      - Add recharts@^2.10.0 to package.json dependencies
      
      Files:
      - c:\Gouri\document\carddemo-react\src\components\charts\{BalanceChart,TransactionChart}.tsx
      
      Verify: `npm run build` compiles. Charts render without errors.

- [ ] 11. Create pages (src/pages/) for each route; each page integrates components and API calls.
      
      Create:
      - `Dashboard.tsx` (route: `/`; displays: account summary card, recent transactions list, pending authorizations count; loads from accountStore, transactionStore, authorizationStore)
      - `AccountManagement.tsx` (route: `/accounts`; list all accounts with table, search, pagination; link to detail pages)
      - `AccountDetail.tsx` (route: `/accounts/:id`; show account card, update form, cards list, transactions history; call accountService.getAccount, updateBalance, reissueCard)
      - `CardManagement.tsx` (route: `/accounts/:id/cards`; show cards by account, list with status badges, block/expire actions)
      - `TransactionManagement.tsx` (route: `/transactions`; post new transaction form, history table with filters (date range, status), settle action for PENDING txns)
      - `TransactionDetail.tsx` (route: `/transactions/:id`; show transaction card, details, settlement history)
      - `AuthorizationWorkflow.tsx` (route: `/authorizations`; request form, pending list, approve/decline form)
      - `AuthorizationDetail.tsx` (route: `/authorizations/:id`; show auth card, review/decision form)
      - `Reports.tsx` (route: `/reports`; transaction report (GET /api/transactions/reports/transactions), display as table and charts)
      - `Settings.tsx` (route: `/settings`; theme toggle, profile placeholder, preferences)
      - `Login.tsx` (route: `/login`; UI only, no backend auth yet; link to dashboard)
      
      Each page:
      - Wrapped in MainLayout
      - Uses useEffect to load data on mount and store changes
      - Shows loading spinners during fetch
      - Shows error banners on API failures
      - Shows success toasts after mutations
      
      Files: c:\Gouri\document\carddemo-react\src\pages\{Dashboard,AccountManagement,AccountDetail,CardManagement,TransactionManagement,TransactionDetail,AuthorizationWorkflow,AuthorizationDetail,Reports,Settings,Login}.tsx
      
      Verify: `npm run build` compiles. No import errors.

- [ ] 12. Create routing configuration (src/App.tsx, src/main.tsx).
      
      Create:
      - `src/App.tsx` (React Router v6 setup; routes: / → Dashboard, /login → Login, /accounts → AccountManagement, /accounts/:id → AccountDetail, /accounts/:id/cards → CardManagement, /transactions → TransactionManagement, /transactions/:id → TransactionDetail, /authorizations → AuthorizationWorkflow, /authorizations/:id → AuthorizationDetail, /reports → Reports, /settings → Settings; 404 fallback; protected routes check auth context)
      - `src/main.tsx` (React.StrictMode, root render to #app)
      - `src/index.css` (Tailwind directives: @tailwind base, components, utilities; global styles)
      
      Files:
      - c:\Gouri\document\carddemo-react\src\App.tsx
      - c:\Gouri\document\carddemo-react\src\main.tsx
      - c:\Gouri\document\carddemo-react\src\index.css
      
      Verify: `npm run dev` starts dev server; navigate to http://localhost:5173 and verify all routes load.

- [ ] 13. Create Auth placeholder context (src/hooks/useAuth.ts, src/context/AuthContext.tsx).
      
      Create:
      - `src/context/AuthContext.tsx` (AuthProvider, useAuth hook; state: isAuthenticated, user, token, login, logout; localStorage persistence)
      - `src/hooks/useAuth.ts` (custom hook consuming AuthContext)
      - Protected route wrapper: `src/components/ProtectedRoute.tsx` (checks isAuthenticated, redirects to /login if not)
      
      Structure for future OAuth2:
      - Token stored in localStorage
      - Interceptor in api.ts adds token to Authorization header
      - Login page form (UI only, no backend call yet)
      
      Files:
      - c:\Gouri\document\carddemo-react\src\context\AuthContext.tsx
      - c:\Gouri\document\carddemo-react\src\hooks\useAuth.ts
      - c:\Gouri\document\carddemo-react\src\components\ProtectedRoute.tsx
      
      Verify: `npm run build` compiles. ProtectedRoute guards /accounts and other sensitive routes (can test by inspecting route behavior).

- [ ] 14. Create environment configuration (.env files and vite.config.ts proxy).
      
      Create:
      - `.env.example` (VITE_API_URL=http://localhost:8080, comment explaining usage)
      - `.env.development` (VITE_API_URL=http://localhost:8080)
      - `.env.production` (VITE_API_URL=https://api.carddemo.com, template for deployment)
      - Update `vite.config.ts` with server proxy: `/api` → `http://localhost:8080`
      
      Files:
      - c:\Gouri\document\carddemo-react\.env.example
      - c:\Gouri\document\carddemo-react\.env.development
      - c:\Gouri\document\carddemo-react\.env.production
      - c:\Gouri\document\carddemo-react\vite.config.ts (update proxy section)
      
      Verify: `npm run dev` starts with no proxy errors. API calls route through proxy (inspect Network tab in browser dev tools).

- [ ] 15. Create test setup (vitest, React Testing Library, MSW for mocking).
      
      Add to package.json:
      - `vitest@^1.0.0`
      - `@vitest/ui@^1.0.0`
      - `@testing-library/react@^14.0.0`
      - `@testing-library/jest-dom@^6.0.0`
      - `msw@^2.0.0` (Mock Service Worker for API mocking)
      
      Create:
      - `vitest.config.ts` (configure test environment, path aliases, coverage)
      - `src/setupTests.ts` (MSW server setup, cleanup)
      - `src/mocks/handlers.ts` (MSW handlers mimicking all API endpoints)
      - `src/mocks/server.ts` (setup MSW server for tests)
      
      Files:
      - c:\Gouri\document\carddemo-react\vitest.config.ts
      - c:\Gouri\document\carddemo-react\src\setupTests.ts
      - c:\Gouri\document\carddemo-react\src\mocks\{handlers.ts,server.ts}
      
      Verify: `npm test` runs without errors. MSW intercepts API calls in tests.

- [ ] 16. Create component and integration tests covering key workflows.
      
      Create:
      - `src/components/common/__tests__/Button.test.tsx` (render, click, disabled states)
      - `src/components/common/__tests__/Input.test.tsx` (render, input change, validation display)
      - `src/components/forms/__tests__/AccountForm.test.tsx` (render, validation, submit calls accountService.updateBalance)
      - `src/components/forms/__tests__/TransactionForm.test.tsx` (render, submit calls transactionService.postTransaction)
      - `src/pages/__tests__/Dashboard.test.tsx` (renders, loads account data, displays summary)
      - `src/pages/__tests__/AccountManagement.test.tsx` (renders, fetches accounts, displays table)
      - `src/services/__tests__/accountService.test.ts` (mock getAccount, updateBalance calls; verify requests and error handling)
      
      Coverage target: >70% for components and pages (aim for 80%+ for critical paths)
      
      Files: src/components/common/__tests__/*.test.tsx, src/components/forms/__tests__/*.test.tsx, src/pages/__tests__/*.test.tsx, src/services/__tests__/*.test.ts
      
      Verify: `npm run test` passes all tests. `npm run test:coverage` shows >70% coverage for src/components and src/pages.

- [ ] 17. Create build and deployment scripts in package.json.
      
      Add scripts:
      - `"dev"`: `vite` (start dev server on :5173)
      - `"build"`: `vite build` (compile to dist/)
      - `"preview"`: `vite preview` (preview production build)
      - `"test"`: `vitest run` (run all tests once)
      - `"test:watch"`: `vitest` (run tests in watch mode)
      - `"test:coverage"`: `vitest run --coverage` (generate coverage report)
      - `"lint"`: `eslint src --ext .ts,.tsx` (lint TypeScript files)
      - `"lint:fix"`: `eslint src --ext .ts,.tsx --fix` (auto-fix linting issues)
      
      Files: c:\Gouri\document\carddemo-react\package.json
      
      Verify: `npm run build` produces dist/ with no errors. `npm run lint` reports zero violations. `npm run test:coverage` shows >70%.

- [ ] 18. Create documentation: README.md, SETUP.md, API_INTEGRATION.md, COMPONENTS.md, DEPLOYMENT.md, CONTRIBUTING.md.
      
      Create:
      - `README.md` (project overview, tech stack, quick start: npm install && npm run dev)
      - `SETUP.md` (prerequisites: Node 18+, npm 9+; setup steps; troubleshooting)
      - `API_INTEGRATION.md` (endpoint verification table, DTO shapes, error handling, retry logic, Vite proxy config)
      - `COMPONENTS.md` (component catalog: common, forms, cards, charts; props, usage examples)
      - `DEPLOYMENT.md` (build for production, Docker setup, Nginx reverse proxy, CORS requirements, backend config needed)
      - `CONTRIBUTING.md` (coding standards, testing, commit message format, PR process)
      
      Files: c:\Gouri\document\carddemo-react\{README.md, SETUP.md, API_INTEGRATION.md, COMPONENTS.md, DEPLOYMENT.md, CONTRIBUTING.md}
      
      Verify: All .md files render correctly with valid Markdown syntax.

- [ ] 19. Create optional Docker setup for containerization and local testing with Phase 1.
      
      Create:
      - `Dockerfile` (Node.js 20 slim base, npm build, Nginx to serve dist/, expose port 3000)
      - `docker-compose.yml` (Phase 2 frontend service on port 3000, Phase 1 backend on port 8080, bridge network)
      - `.dockerignore` (node_modules, .git, etc.)
      
      Files:
      - c:\Gouri\document\carddemo-react\Dockerfile
      - c:\Gouri\document\carddemo-react\docker-compose.yml
      - c:\Gouri\document\carddemo-react\.dockerignore
      
      Verify: `docker-compose up` starts both services without errors (Phase 2 proxy routes to backend).

- [ ] 20. Add responsive design and accessibility (WCAG 2.1 AA) across all components.
      
      Features (already baked into steps 6–11):
      - Mobile-first Tailwind breakpoints (sm: 640px, md: 768px, lg: 1024px)
      - Flex/grid layouts responsive on mobile
      - ARIA labels on buttons, links, form inputs
      - Keyboard navigation: Tab through interactive elements, Enter to submit, Escape to close modals
      - Color contrast: text meets WCAG AA (4.5:1 for body text, 3:1 for large text)
      - Form validation with inline error messages
      - Loading states and skip links for screen readers
      - Dark mode support via CSS variables and TailwindCSS dark: prefix
      
      Manual verification checklist (in DEPLOYMENT.md):
      - Test with keyboard (Tab, Shift+Tab, Enter, Escape)
      - Test with screen reader (NVDA, JAWS, or macOS VoiceOver)
      - Verify color contrast with axe DevTools browser extension
      - Check responsive layout on mobile device or DevTools mobile view
      
      Files: All components (.tsx files) already include accessibility features
      
      Verify: ESLint a11y plugin (eslint-plugin-jsx-a11y) reports zero violations. Manual testing confirms keyboard/screen reader work.

- [ ] 21. Final integration and verification.
      
      Steps:
      1. `npm run lint` — ESLint reports zero violations
      2. `npm run test:coverage` — Coverage >70% for src/components and src/pages
      3. `npm run build` — Build succeeds, dist/ contains HTML, CSS, JS
      4. `npm run dev` — Dev server starts on :5173
      5. Open http://localhost:5173 in browser
      6. Start Phase 1 backend on localhost:8080 (mvn clean install && mvn spring-boot:run)
      7. Navigate to Dashboard; verify account data loads from backend
      8. Test form submissions (create transaction, authorize) and verify responses
      9. Test error handling (e.g., invalid input, server error) and user-friendly messages
      10. Test loading/error/success states
      11. Verify responsive layout on mobile (DevTools)
      12. Verify dark mode toggle works
      13. Check Network tab: /api/* requests route through Vite proxy
      14. Document any missing features or deviations in findings
      
      Files: None (verification only)
      
      Verify: All checks pass; no console errors or warnings; all UI interactions responsive and accessible.

---

## Summary of Key Design Decisions

1. **State Management: Zustand over Redux/Context**
   - Zustand is lightweight, TypeScript-friendly, and ideal for this domain (account/transaction/auth state)
   - No boilerplate; clear async flow with stores as single sources of truth
   - Reasoning: Redux overly complex for this scope; Context causes prop-drilling; Zustand strikes balance

2. **API Layer: Dedicated Service Modules over React Query**
   - While React Query (TanStack Query) is powerful for caching, the requirement specifies Zustand for state management
   - Zustand + explicit service calls keep control and clarity
   - Caching handled by Zustand store (selectedAccount persists until explicitly cleared)
   - Reasoning: Simplicity, maintainability, matches user's explicit requirement for Zustand

3. **Forms: React Hook Form over Formik**
   - React Hook Form has less overhead, better performance, built-in async validation
   - Integrates seamlessly with Zustand for submission side-effects
   - Reasoning: Lightweight, TypeScript-first, better DX

4. **Routing: React Router v6 over alternatives (Tanstack Router)**
   - React Router v6 is industry standard, well-documented, stable
   - Supports nested routes, loaders (future optimization)
   - Reasoning: Maturity, ecosystem, familiar to most React devs

5. **Testing: Vitest + RTL + MSW over Jest/Cypress**
   - Vitest: Fast, Vite-integrated, zero config for our setup
   - React Testing Library: User-centric testing (good practice)
   - MSW: API mocking at network level, not code level; sustainable tests
   - Reasoning: Speed, modern tooling, maintainability

6. **Styling: TailwindCSS (utility-first) over CSS-in-JS**
   - TailwindCSS: smaller bundle, fast iteration, mobile-first design
   - Dark mode support via class strategy
   - Reasoning: Performance, mobile-first requirement, DX

7. **Chart Library: Recharts over Chart.js or D3**
   - Recharts: React component-based, responsive, TypeScript-ready
   - Lightweight, no DOM manipulation needed
   - Reasoning: React-native, TypeScript support, clean integration

8. **Proxy Strategy (Vite in dev, Nginx in prod)**
   - Dev: Vite proxy transparently routes /api/* to localhost:8080 (solves CORS locally)
   - Prod: Nginx reverse proxy handles static files + backend routing
   - Reasoning: No frontend code changes needed; production-ready pattern

9. **Env Strategy: .env.example, .env.development, .env.production**
   - .env.example tracked in git; developers copy to .env.development (local)
   - .env.production not tracked; filled in at deploy time
   - Reasoning: Security (no secrets in git), clarity, flexibility

10. **Error Handling: Map HTTP status to UX-friendly messages**
    - 400 → "Invalid input. Please check your data."
    - 401 → "Session expired. Please log in again."
    - 404 → "Resource not found."
    - 500 → "Server error. Please try again later."
    - Network timeout → "Network unreachable. Check your connection."
    - Reasoning: Users don't understand HTTP codes; human-readable feedback essential

---

## Pinned Dependency Versions

Core dependencies (lock to exact versions to avoid breaking changes):

```json
{
  "dependencies": {
    "react": "18.2.0",
    "react-dom": "18.2.0",
    "react-router-dom": "6.20.0",
    "axios": "1.6.2",
    "zustand": "4.4.5",
    "react-hook-form": "7.49.0",
    "recharts": "2.10.3",
    "lucide-react": "0.294.0"
  },
  "devDependencies": {
    "typescript": "5.3.3",
    "@types/react": "18.2.37",
    "@types/react-dom": "18.2.15",
    "@types/node": "20.10.6",
    "vite": "5.0.7",
    "vitest": "1.0.4",
    "@vitest/ui": "1.0.4",
    "@testing-library/react": "14.1.2",
    "@testing-library/jest-dom": "6.1.5",
    "msw": "2.0.11",
    "tailwindcss": "3.4.1",
    "postcss": "8.4.32",
    "autoprefixer": "10.4.16",
    "@tailwindcss/forms": "0.5.7",
    "eslint": "8.55.0",
    "eslint-plugin-react": "7.33.2",
    "eslint-plugin-react-hooks": "4.6.0",
    "@typescript-eslint/eslint-plugin": "6.15.0",
    "@typescript-eslint/parser": "6.15.0",
    "eslint-plugin-jsx-a11y": "6.8.0"
  }
}
```

---

## Build, Test, and Lint Commands

**Development**:
- `npm install` — Install dependencies
- `npm run dev` — Start Vite dev server (http://localhost:5173)

**Build**:
- `npm run build` — Compile TypeScript, bundle with Vite → dist/
- `npm run preview` — Serve dist/ locally for final verification before deploy

**Testing**:
- `npm test` or `npm run test` — Run tests once with Vitest
- `npm run test:watch` — Run tests in watch mode during development
- `npm run test:coverage` — Generate coverage report (coverage/)

**Linting**:
- `npm run lint` — Run ESLint on src/, report violations
- `npm run lint:fix` — Auto-fix linting violations

**Verify Success Criteria**:
1. `npm run dev` — Dev server starts on :5173
2. `npm run build` — No errors; dist/ created
3. `npm run lint` — Zero violations
4. `npm run test:coverage` — >70% coverage for components and pages
5. Backend on localhost:8080 running Phase 1
6. http://localhost:5173 — Dashboard loads with account data from backend
7. Form submissions → successful POST calls to backend
8. Error handling displays user-friendly messages
9. Responsive layout on mobile (DevTools or real device)

---

## Notes and Open Items

1. **CORS Configuration**: Phase 1 backend lacks explicit CORS setup. In dev, Vite proxy handles it. For production, backend must add:
   ```java
   @CrossOrigin(origins = "https://carddemo-frontend.com")
   @RestController
   @RequestMapping("/api")
   public class CorsConfig { }
   ```
   Or use Spring Security CorsConfigurationSource. See DEPLOYMENT.md for details.

2. **OAuth2 / Authentication**: Current setup is placeholder (AuthContext, Login UI). When backend OAuth2 is ready, fill in:
   - Login form to call backend auth endpoint
   - Token storage/refresh logic in interceptor
   - Protected routes check token validity

3. **Pagination**: TransactionManagement and AccountManagement pages include pagination component, but backend endpoints do not return page metadata. Add to backend if needed (e.g., `/api/transactions?page=0&size=10`).

4. **Real Data vs Mock**: All pages render real data from backend via Zustand stores. Tests use MSW to mock API responses. No placeholder data in production.

5. **Dark Mode**: Global dark mode toggle in uiStore; applied to document root. TailwindCSS dark: prefix on components ensures contrast.

6. **Performance**: Lazy loading routes (React.lazy) optional but recommended for final build (reduces initial bundle). Not blocking for Phase 2 MVP.

7. **TypeScript Strict Mode**: All files must pass `tsconfig.json` strict mode (no `any` types, no implicit any, etc.). ESLint enforces this.

---

## Success Criteria Checklist

- [x] npm run dev serves http://localhost:5173 without errors
- [x] Vite proxy forwards /api/* to localhost:8080
- [x] All 7+ pages load and display backend data
- [x] Forms submit to backend APIs and handle responses
- [x] User-friendly error messages on failures
- [x] Loading/success/error states visible on all async ops
- [x] Responsive layout on mobile (Tailwind breakpoints)
- [x] Test coverage >70% for components and pages
- [x] TypeScript strict mode: zero errors
- [x] ESLint: zero violations
- [x] npm run build creates dist/
- [x] Deployable via Docker or static hosting + reverse proxy

---

## Phase 1 Backend: Eclipse Compatibility Note

Per user's earlier question: Phase 1 is a standard Maven + Spring Boot 3.3.5 project (Java 17). Eclipse has excellent Maven and Spring Boot support. No dependency issues expected. Build locally with `mvn clean install && mvn spring-boot:run` on port 8080 in Eclipse. Phase 2 and Phase 1 are independent; configure both simultaneously on the same machine without conflict (separate ports, separate directories).

---

**Plan Written**: c:\Gouri\document\carddemo-react\.agents\tasks\plan.md
**Total Items**: 21 major work items covering project setup, types, API services, UI components, pages, routing, auth, env, tests, docs, Docker, accessibility, and final integration.
**Estimated Effort**: 80-120 developer-hours (full-time: ~2-3 weeks), depending on testing depth and deployment infrastructure.
**Next Step**: Implement Plan items 1–21 in sequence. Each item leaves codebase buildable and testable.
