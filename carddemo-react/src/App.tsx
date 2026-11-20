import React, { useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import ProtectedRoute from '@/components/ProtectedRoute'
import Login from '@/pages/Login'
import Signup from '@/pages/Signup'
import { Dashboard } from '@/pages/Dashboard'
import { AccountManagement } from '@/pages/AccountManagement'
import { TransactionManagement } from '@/pages/TransactionManagement'
import { AuthorizationWorkflow } from '@/pages/AuthorizationWorkflow'
import { Reports } from '@/pages/Reports'
import { Settings } from '@/pages/Settings'

function App() {
  const initializeAuth = useAuthStore((state) => state.initializeAuth)

  // Initialize auth on app load (check localStorage for saved token/user)
  useEffect(() => {
    initializeAuth()
  }, [initializeAuth])

  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/accounts"
          element={
            <ProtectedRoute>
              <AccountManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/transactions"
          element={
            <ProtectedRoute>
              <TransactionManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/authorizations"
          element={
            <ProtectedRoute>
              <AuthorizationWorkflow />
            </ProtectedRoute>
          }
        />
        <Route
          path="/reports"
          element={
            <ProtectedRoute>
              <Reports />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <Settings />
            </ProtectedRoute>
          }
        />

        {/* 404 Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  )
}

export default App