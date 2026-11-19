# CardDemo React Frontend (Phase 2)

Production-ready React 18 + TypeScript frontend for Phase 1 Java Spring Boot backend.

## Quick Start

### Prerequisites
- Node.js 18+ and npm 9+
- Phase 1 backend running on `http://localhost:8080`

### Installation

```bash
# Clone or navigate to project
cd c:\Gouri\document\carddemo-react

# Install dependencies
npm install

# Start development server
npm run dev
```

Visit http://localhost:5173 in your browser. Default credentials: any email + password (mock auth).

## Tech Stack

- **React 18.2** - UI framework
- **TypeScript 5.3** - Strict typing
- **Vite 5.0** - Build tool, dev server
- **TailwindCSS 3.4** - Utility-first CSS, dark mode
- **Zustand 4.4** - State management
- **React Hook Form 7.49** - Form handling with validation
- **Axios 1.6** - API client with retry logic
- **React Router v6** - Client-side routing
- **Vitest / React Testing Library** - Unit & integration tests
- **MSW** - API mocking for tests

## Project Structure

```
src/
├── components/          # Reusable React components
│   ├── common/         # UI primitives (Button, Input, Card, Modal, etc.)
│   ├── forms/          # Form components with validation
│   ├── layout/         # Page layout (Header, Sidebar, Footer, MainLayout)
│   └── ProtectedRoute.tsx
├── services/           # API clients
│   ├── api.ts         # Axios instance, interceptors, retry logic
│   ├── accountService.ts
│   ├── cardService.ts
│   ├── transactionService.ts
│   └── authorizationService.ts
├── store/             # Zustand state management
│   ├── accountStore.ts
│   ├── cardStore.ts
│   ├── transactionStore.ts
│   ├── authorizationStore.ts
│   └── uiStore.ts
├── pages/             # Page components (Dashboard, Login, etc.)
├── hooks/             # Custom React hooks (useAuth, etc.)
├── context/           # React Context (AuthContext)
├── types/             # TypeScript interfaces matching backend DTOs
├── mocks/             # MSW mock handlers
├── App.tsx            # Routes and router setup
├── main.tsx           # React root
└── index.css          # Tailwind directives, global styles
```

## Available Scripts

```bash
npm run dev              # Start Vite dev server (http://localhost:5173)
npm run build            # Build for production (./dist)
npm run preview          # Preview production build locally
npm run test             # Run tests once
npm run test:watch       # Run tests in watch mode
npm run test:coverage    # Generate coverage report
npm run lint             # Check ESLint violations
npm run lint:fix         # Auto-fix ESLint issues
```

## API Integration

The frontend connects to Phase 1 backend via `/api` proxy (dev) or Nginx (prod).

**Base URL**: http://localhost:8080 (dev)

**Proxy setup**: Vite rewrites `/api/*` to backend endpoints.

**Verified endpoints** (16 total):
- Accounts: GET, PUT, GET transactions, POST balance, POST reissue, GET cards
- Cards: GET, POST, GET by account
- Transactions: POST, GET, settle, report
- Authorizations: POST, GET, GET pending, PUT review, POST expire

See `plan.md` for full endpoint table.

## Routing

| Route | Component | Public |
|-------|-----------|--------|
| `/` | Dashboard | ❌ Protected |
| `/login` | Login | ✅ Public |
| `/accounts` | AccountManagement | ❌ Protected |
| `/transactions` | TransactionManagement | ❌ Protected |
| `/authorizations` | AuthorizationWorkflow | ❌ Protected |
| `/reports` | Reports | ❌ Protected |
| `/settings` | Settings | ❌ Protected |
| `*` | 404 → Redirect to `/` | - |

## Authentication

**Current**: Mock localStorage-based auth (no backend call yet)
- Login page: Any email + password accepted
- Token stored in localStorage
- Interceptor adds token to Authorization header
- Protected routes redirect to /login if not authenticated

**For OAuth2 integration**: See `SETUP.md` for configuration steps.

## Features

### State Management
- Zustand stores for account, card, transaction, authorization, and UI state
- Async loading + error handling in each store
- Automatic localStorage persistence for auth token

### API Client
- Axios interceptors for auth, error mapping, retry logic
- Exponential backoff: 2s, 4s, 8s delays (max 3 retries)
- Human-readable error messages (no HTTP codes to users)
- 30s request timeout

### Forms
- React Hook Form with built-in validation
- Inline error messages, required indicators
- Loading state during submission
- Success/error toasts via uiStore

### UI/UX
- Responsive design (mobile-first, sm/md/lg breakpoints)
- Dark mode toggle (persisted in localStorage)
- Sidebar navigation with mobile drawer
- Toast notifications auto-dismiss (3s default)
- Loading spinners, error banners, empty states

### Accessibility (WCAG 2.1 AA)
- ARIA labels on all interactive elements
- Keyboard navigation: Tab, Shift+Tab, Enter, Escape
- Color contrast meets AA standard
- Focus indicators for keyboard users
- Form validation with error linking
- Modal with proper semantics

## Development Workflow

### Start development server
```bash
npm run dev
```

Open http://localhost:5173. Vite hot-reloads on file changes.

### Add a new page
1. Create file in `src/pages/YourPage.tsx`
2. Add route in `src/App.tsx`
3. Wrap with `<ProtectedRoute>` if private

### Add API call
1. Create or update service in `src/services/`
2. Use Zustand store action in component
3. Handle loading + error states

### Run tests
```bash
npm run test              # Run once
npm run test:watch       # Watch mode
npm run test:coverage    # Coverage report (target: >70%)
```

### Lint and type check
```bash
npm run lint             # ESLint
npx tsc --noEmit         # TypeScript strict check
```

## Environment Variables

Copy `.env.example` to `.env.development`:

```
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=CardDemo (Dev)
VITE_APP_VERSION=1.0.0-dev
```

For production, use `.env.production`:

```
VITE_API_URL=https://api.carddemo.com
VITE_APP_NAME=CardDemo
VITE_APP_VERSION=1.0.0
```

## Building for Production

```bash
npm run build
```

Outputs to `dist/`. Test with `npm run preview`.

For deployment:
- Use Nginx to serve `dist/` and reverse-proxy `/api` to backend
- See `DEPLOYMENT.md` for Docker setup

## Troubleshooting

### Proxy not working?
- Ensure backend runs on http://localhost:8080
- Check Vite dev server console for proxy errors
- Inspect Network tab in browser DevTools

### Dark mode not persisting?
- Check localStorage for 'theme' key
- Clear browser cache, refresh

### Form validation not showing?
- Ensure input has `name` attribute
- Check React Hook Form `register()` and error object

### Build errors?
- Run `npm install` to ensure all deps installed
- Check TypeScript errors: `npx tsc --noEmit`
- Check ESLint: `npm run lint`

## Documentation

- `SETUP.md` - Detailed setup and configuration
- `API_INTEGRATION.md` - Backend endpoint reference, error handling
- `COMPONENTS.md` - Component catalog with usage examples
- `DEPLOYMENT.md` - Production deployment, Docker setup
- `CONTRIBUTING.md` - Development standards, commit message format

## License

MIT

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review documentation files
3. Check browser console for errors
4. Verify backend is running and accessible
