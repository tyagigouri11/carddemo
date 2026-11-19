# Setup Guide - CardDemo React Frontend

## Prerequisites

- **Node.js**: 18.0.0 or later
- **npm**: 9.0.0 or later
- **Phase 1 Backend**: Running on `http://localhost:8080`

### Check installed versions

```bash
node --version    # Should be v18.0.0 or higher
npm --version     # Should be 9.0.0 or higher
```

### Install Node.js

**Windows**:
- Download from https://nodejs.org/ (LTS recommended)
- Run installer, check "Add to PATH"
- Restart terminal/IDE

**macOS**:
```bash
brew install node@18
```

**Linux** (Ubuntu/Debian):
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
```

## Installation Steps

### 1. Clone/Navigate to Project

```bash
cd c:\Gouri\document\carddemo-react
```

### 2. Install Dependencies

```bash
npm install
```

This installs all packages listed in `package.json`. Typically takes 2-3 minutes.

**Troubleshooting**:
- **"npm: command not found"** → Node.js not installed or not in PATH. Restart terminal after installing.
- **"ERR! 404 Not Found"** → npm registry issue. Try: `npm cache clean --force && npm install`
- **Permission denied** → Try: `npm install --no-optional`

### 3. Verify Installation

```bash
npm run lint     # Check ESLint (should pass with clean code)
npx tsc --noEmit # Check TypeScript strict (should pass with no errors)
```

## Environment Configuration

### 1. Create `.env.development` (if not already present)

Copy from `.env.example`:

```bash
cp .env.example .env.development
```

Or manually create `c:\Gouri\document\carddemo-react\.env.development`:

```env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=CardDemo (Dev)
VITE_APP_VERSION=1.0.0-dev
```

### 2. Verify Backend URL

- Ensure Phase 1 backend runs on `http://localhost:8080`
- Test connectivity:
  ```bash
  curl http://localhost:8080/swagger-ui.html
  ```
  Should return Swagger UI page.

## Running Development Server

```bash
npm run dev
```

Output:
```
  VITE v5.0.7  ready in 123 ms

  ➜  Local:   http://localhost:5173/
  ➜  press h + enter to show help
```

Open http://localhost:5173 in browser.

### First Login

Use any email and password (mock auth enabled):
- Email: `demo@example.com`
- Password: `anything`

Redirects to Dashboard after login.

## Development Workflow

### Hot Reload

Vite automatically hot-reloads on file changes. No manual restart needed.

### File Structure

```
src/
├── pages/              # Each page = new route
├── components/         # Reusable components
├── services/          # API calls
├── store/             # Zustand state
├── types/             # TypeScript interfaces
└── App.tsx            # Routes defined here
```

### Adding a New Page

**Example: New page for Card Management**

1. Create `src/pages/CardManagement.tsx`:

```tsx
import React from 'react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Card, CardBody, CardHeader } from '@/components/common/Card'

export const CardManagement: React.FC = () => {
  return (
    <MainLayout title="Card Management">
      <Card>
        <CardHeader>
          <h2 className="text-lg font-semibold">Your Cards</h2>
        </CardHeader>
        <CardBody>
          {/* Your content here */}
        </CardBody>
      </Card>
    </MainLayout>
  )
}
```

2. Add route in `src/App.tsx`:

```tsx
import { CardManagement } from '@/pages/CardManagement'

// Inside Routes:
<Route
  path="/cards"
  element={
    <ProtectedRoute>
      <CardManagement />
    </ProtectedRoute>
  }
/>
```

3. Add sidebar nav item (optional) in `src/components/layout/Sidebar.tsx`:

```tsx
const navItems = [
  // ... existing items
  { path: '/cards', label: 'Cards', icon: CreditCard },
]
```

### Making API Calls

**Example: Load account data**

1. Check service exists in `src/services/accountService.ts`
2. Use store action in component:

```tsx
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const MyComponent = () => {
  const loadAccount = useAccountStore(s => s.loadAccount)
  const selectedAccount = useAccountStore(s => s.selectedAccount)
  const loading = useAccountStore(s => s.loading)
  const error = useAccountStore(s => s.error)

  useEffect(() => {
    loadAccount('account-id-123')
  }, [loadAccount])

  if (loading) return <Loading />
  if (error) return <ErrorBanner message={error} />
  if (!selectedAccount) return <p>No account found</p>

  return <div>{selectedAccount.creditLimit}</div>
}
```

### Handling Errors

All API errors auto-map to user-friendly messages via `mapErrorToMessage()` in `api.ts`:

- `400` → "Invalid input. Please check your data."
- `401` → "Session expired. Please log in again."
- `404` → "Resource not found."
- `500` → "Server error. Please try again later."

To show error to user:

```tsx
import { useUiStore } from '@/store/uiStore'

const addToast = useUiStore(s => s.addToast)

try {
  await postTransaction(data)
  addToast({ message: 'Success!', type: 'success' })
} catch (error) {
  addToast({ message: 'Failed to submit', type: 'error' })
}
```

## Testing

### Run Tests

```bash
npm run test              # Run once
npm run test:watch       # Run in watch mode (re-run on file change)
npm run test:coverage    # Show coverage report
```

### Write a Test

Create `src/components/common/__tests__/Button.test.tsx`:

```tsx
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Button } from '@/components/common/Button'

describe('Button', () => {
  it('renders button with text', () => {
    render(<Button>Click me</Button>)
    expect(screen.getByText('Click me')).toBeInTheDocument()
  })

  it('is disabled when disabled prop true', () => {
    render(<Button disabled>Click me</Button>)
    expect(screen.getByText('Click me')).toBeDisabled()
  })
})
```

Run tests:

```bash
npm run test
```

## Building for Production

### Build

```bash
npm run build
```

Generates optimized `dist/` folder (~150-200KB gzipped).

### Preview Build Locally

```bash
npm run preview
```

Serves `dist/` on http://localhost:4173.

**Test API calls**:
- Ensure backend runs on http://localhost:8080
- Open http://localhost:4173 in browser
- Verify requests reach backend (check Network tab in DevTools)

### Deploy to Production

See `DEPLOYMENT.md` for Docker, Nginx, and cloud deployment options.

## Linting and Type Checking

### ESLint

```bash
npm run lint        # Show violations
npm run lint:fix    # Auto-fix violations
```

**Common issues**:
- Unused variables: Prefix with `_` (e.g., `_unused`)
- React rules: Use hooks at top-level, not in conditionals
- Accessibility: Add ARIA labels to buttons/inputs

### TypeScript Strict Mode

```bash
npx tsc --noEmit
```

Verifies all TypeScript types without building. No errors = strict compliance.

**Common errors**:
- `Type 'X' is not assignable to type 'Y'` → Check prop types match interface
- `Cannot read property X of undefined` → Add null check or optional chaining (`?.`)

## Debugging

### Browser DevTools

1. Open http://localhost:5173
2. Press `F12` or `Ctrl+Shift+I`
3. **Console** tab: See JavaScript errors, logs
4. **Network** tab: Inspect API requests/responses
5. **Application** tab: Check localStorage (auth token, theme)

### VS Code Debugging

Install **Debugger for Chrome** extension, then:

1. Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "chrome",
      "request": "launch",
      "name": "Launch Chrome",
      "url": "http://localhost:5173",
      "webRoot": "${workspaceFolder}/src"
    }
  ]
}
```

2. Press `F5` to start debugging

### Common Issues

| Issue | Solution |
|-------|----------|
| "Cannot find module '@/services'" | Check path alias in `tsconfig.json` and `vite.config.ts` |
| API calls return CORS error | Ensure Vite proxy is set up (vite.config.ts) |
| Dark mode button does nothing | Check `uiStore.ts` and localStorage 'theme' key |
| Form not validating | Verify `react-hook-form` `register()` on input |
| Sidebar won't close on mobile | Check `useUiStore` and CSS media queries |

## Performance Tips

### Reduce Bundle Size

1. **Lazy-load routes** (not done yet, can optimize):

```tsx
import { lazy } from 'react'
const Dashboard = lazy(() => import('@/pages/Dashboard').then(m => ({ default: m.Dashboard })))
```

2. **Don't import unused components**
3. **Use React DevTools Profiler** to find slow renders

### Optimize API Calls

- Zustand stores cache data; avoid redundant calls
- Check Network tab for duplicate requests
- Implement pagination for large lists

## Tips & Tricks

### Quick format

```bash
npm run lint:fix
```

Auto-fixes most ESLint issues.

### Check all before committing

```bash
npm run lint
npx tsc --noEmit
npm run build
npm run test
```

### Clear cache if weird errors

```bash
rm -rf node_modules package-lock.json
npm install
```

## Next: OAuth2 Integration

Currently using mock auth (localStorage). To integrate real OAuth2:

1. Update `src/context/AuthContext.tsx`:
   - Replace mock `login()` with real API call to `/api/auth/login`
   - Expect `{ accessToken, refreshToken }`

2. Update `src/services/api.ts`:
   - Refresh token on 401 response
   - Handle refresh token expiry → redirect to /login

3. Update environment:
   - Add `VITE_AUTH_PROVIDER_URL` (e.g., Keycloak endpoint)

See backend documentation for OAuth2 endpoint details.

## Support

### Common Commands Reference

```bash
npm install              # Install dependencies
npm run dev              # Start dev server
npm run build            # Build production
npm run test             # Run tests
npm run lint             # Check code style
npm run lint:fix         # Auto-fix code style
npx tsc --noEmit         # Check TypeScript
npm run test:coverage    # Show test coverage
```

### File Locations

- **Env config**: `.env.development`, `.env.production`
- **Routes**: `src/App.tsx`
- **State**: `src/store/*.ts`
- **API**: `src/services/*.ts`
- **Components**: `src/components/`
- **Pages**: `src/pages/`

### Need Help?

1. Check the main `README.md`
2. Review component/service code comments
3. Check browser console for errors
4. Verify backend is running and healthy
