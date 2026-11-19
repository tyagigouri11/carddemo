# Implementation Plan: React UI for Authorization Management (Phase 4)

## Overview
Build a complete React UI for authorization management aligned with Phase 4 backend endpoints. The UI will follow existing CardManagement and TransactionManagement patterns, use Tailwind CSS, integrate into the main app routing and navigation, and provide form creation, list view with filtering, detail modal, and summary reports.

## Backend API Contract
**Base URL:** `http://localhost:8080/api/authorizations`

**Key Endpoints:**
- `POST /api/authorizations` – Create authorization (auto-approval)
- `GET /api/authorizations/{id}` – Get details by ID
- `PUT /api/authorizations/{id}/approve` – Manual approve
- `PUT /api/authorizations/{id}/decline` – Manual decline (with `reason` in request body)
- `GET /api/authorizations` – List with query filters (status, cardId, fromDate, toDate)
- `GET /api/authorizations/reports/summary` – Statistics (totalCount, approvedCount, declinedCount, pendingCount, totalAmount, averageAmount)

**DTO Fields (from AuthorizationDto.java):**
- Request: `accountId` (Long), `cardNumber` (String), `amount` (BigDecimal), `merchantId` (String), `merchantName` (String)
- Response: id, accountId, cardNumber, amount, merchantId, merchantName, requestDate, responseDate, expiryDate, status (PENDING/APPROVED/DECLINED), declineReasonCode, approvedAmount

---

## Architecture Decisions

1. **State Management:** Use existing zustand pattern via `useAuthorizationStore` (already exists in codebase). The service layer (`authorizationService`) and store are already stubbed; implementation will complete them.

2. **Form Validation:** Use react-hook-form (already in project) with Zod-like custom validation. Require: cardNumber, merchantId, merchantName, amount > 0.

3. **Data Fetching:** Axios via `apiClient` (already configured). Requests include retry logic and error mapping (already in place).

4. **UI Components:** Reuse existing Button, Input, Card, CardBody, CardHeader, CardFooter. Use Tailwind CSS utility classes matching existing styling (neutral colors, primary blue, success green, danger red, warning yellow).

5. **Navigation & Routing:** Route already exists at `/authorizations`; page already mounted as `AuthorizationWorkflow`. Sub-navigation will be handled via component state (view switching) not sub-routes, since the task specifies "sub-items" in the menu, not separate pages.

6. **Error Handling:** Leverage existing error interceptor in apiClient + toast notifications via `useUiStore.addToast()`.

7. **Loading States:** Show spinners during API calls. Use Button's `loading` prop for submit buttons; use loading overlay or skeleton for lists.

8. **Responsive Design:** Use Tailwind's `grid`, `lg:`, `sm:` breakpoints matching existing layout patterns.

---

## Implementation Plan

- [ ] **1. Complete authorizationService.ts with all endpoints.**
      Add missing methods: `listAuthorizations()`, `getAuthorizationsByStatus()`, `approveAuthorization()`, `declineAuthorization()`, `getSummaryReport()`.
      Files: `src/services/authorizationService.ts`
      Verify: `npm run lint` passes (no type errors); existing transactionService pattern is a template.

- [ ] **2. Create AuthorizationForm component (create/request authorization).**
      Form with fields: cardNumber, merchantId, merchantName, amount (all required). 
      Validations: non-empty strings, amount > 0, number validation for amount. 
      On submit: POST to /api/authorizations. 
      On success: show toast, clear form, call store action to refresh list. 
      On error: show error toast. 
      Files: `src/components/forms/AuthorizationForm.tsx`
      Verify: Run build `npm run build` with no type errors; form renders and submits without console errors (manual test via dev server).

- [ ] **3. Create AuthorizationList component (list with filters and search).**
      Table with columns: ID, Card Number, Amount, Merchant, Status (with color badges: green APPROVED, red DECLINED, yellow PENDING), Date.
      Filters: All/Approved/Declined/Pending toggle buttons. 
      Search: input for card number or ID (client-side filter). 
      Pagination: 10 items/page. 
      Actions: Click row to open detail modal, Refresh button, Loading spinner during fetch.
      Optional: Date range filter and CSV export (may be deferred if scope is tight).
      Files: `src/components/authorization/AuthorizationList.tsx`
      Verify: Build passes, list renders, filter buttons toggle, search filters results (manual test).

- [ ] **4. Create AuthorizationDetail component (modal for detail view and actions).**
      Modal/card showing: ID, Card Number, Amount, Merchant, Status, Reason (if declined), dates (request, response, expiry).
      Actions: 
        - If DECLINED: show "Approve" button (PUT /approve) behind a confirmation.
        - If APPROVED: show "Decline" button with text input for reason (PUT /decline with reason in body) behind a confirmation.
        - Close button (X).
      Loading and error states during action. 
      On action success: update store, refresh list, close modal, show success toast.
      Files: `src/components/authorization/AuthorizationDetail.tsx`
      Verify: Build passes, modal opens/closes, approve/decline buttons trigger actions (manual test).

- [ ] **5. Create AuthorizationReports component (summary statistics).**
      Cards for: Total Count, Approved Count, Declined Count, Pending Count, Total Amount, Average Amount. 
      Each card shows count/amount with icon and color (green success, red danger, yellow warning, blue primary). 
      Auto-refresh every 30 seconds or manual refresh button. 
      Loading state. 
      GET /api/authorizations/reports/summary.
      Optional: Bar chart using recharts (project has it); may be deferred if scope is tight.
      Files: `src/components/authorization/AuthorizationReports.tsx`
      Verify: Build passes, cards display, refresh fetches new data (manual test).

- [ ] **6. Create AuthorizationNav component (sub-navigation for Authorization Management).**
      A simple navbar or tab component with buttons/links: Create Authorization, View Authorizations, Reports.
      Click handlers switch the main view (state in parent AuthorizationManagement).
      Active indicator highlights current view.
      Files: `src/components/authorization/AuthorizationNav.tsx`
      Verify: Build passes, buttons toggle views (manual test).

- [ ] **7. Create AuthorizationManagement container component.**
      Main page that orchestrates: AuthorizationNav + view switcher (conditional render).
      Manages state: `currentView` (create | list | reports).
      Wraps with MainLayout.
      Files: `src/components/authorization/AuthorizationManagement.tsx`
      Verify: Build passes, container renders nav and view switcher (manual test).

- [ ] **8. Update AuthorizationWorkflow page to use AuthorizationManagement.**
      Replace placeholder content with `<AuthorizationManagement />`.
      Files: `src/pages/AuthorizationWorkflow.tsx`
      Verify: Build passes, page renders without errors (manual test).

- [ ] **9. Update Sidebar and Header for Authorization Management menu integration.**
      Sidebar already has link to `/authorizations` with icon; ensure active highlight works.
      Header should display breadcrumbs for Authorization > Create/View/Reports based on current view.
      Files: `src/components/layout/Sidebar.tsx`, `src/components/layout/Header.tsx` (if needed)
      Verify: Build passes, sidebar link highlights when on /authorizations, breadcrumbs update (manual test).

- [ ] **10. Run full build and integration test.**
      Build entire app: `npm run build` (check for TypeScript errors, bundle size).
      Dev server test: `npm run dev`, navigate to authorizations, test flow: create → list → detail → approve/decline, reports.
      Verify loading states, error handling, responsive layout on mobile (browser dev tools).
      Files: (no new files)
      Verify: Build succeeds, all pages load, no console errors, API calls succeed (backend must be running on :8080).

---

## Notes

- **API Backend Requirement:** Backend must be running on `http://localhost:8080` with authorization endpoints live.
- **Database:** Backend should use PostgreSQL/MySQL (not H2) per Phase 4 requirements.
- **Field Mapping:** TypeScript types in `src/types/index.ts` already define Authorization interface; it matches the backend DTO (accountId: Long becomes string in frontend per convention, but should be cast appropriately).
- **Testing:** Full E2E testing deferred; manual browser testing is primary verification step.
- **Accessibility:** Components use semantic HTML, ARIA labels where needed (Button, Input already support this).
- **Performance:** List pagination (10/page) avoids loading all records at once. Auto-refresh reports every 30s can be made configurable.

---

## Dependency Order

1. authorizationService (1) → enables store actions
2. AuthorizationForm, AuthorizationList, AuthorizationDetail, AuthorizationReports (2-5) → independent components
3. AuthorizationNav (6) → uses other components
4. AuthorizationManagement (7) → composes all components
5. AuthorizationWorkflow (8) → renders container
6. Navigation integration (9) → already in place; may need minor tweaks
7. Full test (10) → validates entire flow

All items must build and tests must pass before committing.
