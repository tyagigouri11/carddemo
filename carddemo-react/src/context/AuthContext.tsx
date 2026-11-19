import React, { createContext, useState, useEffect } from 'react'
import { User, AuthToken } from '@/types'

interface AuthContextType {
  isAuthenticated: boolean
  user: User | null
  token: string | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  setToken: (token: string) => void
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined)

interface AuthProviderProps {
  children: React.ReactNode
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<User | null>(null)
  const [token, setTokenState] = useState<string | null>(null)

  // Initialize from localStorage
  useEffect(() => {
    const savedToken = localStorage.getItem('authToken')
    const savedUser = localStorage.getItem('authUser')
    if (savedToken) {
      setTokenState(savedToken)
      setIsAuthenticated(true)
      if (savedUser) {
        setUser(JSON.parse(savedUser))
      }
    }
  }, [])

  const login = async (email: string, _password: string) => {
    // Placeholder: In real app, this would call backend OAuth2 endpoint
    const mockUser: User = {
      id: '1',
      username: email,
      email,
      role: 'USER',
    }
    const mockToken = 'mock-token-' + Date.now()

    localStorage.setItem('authToken', mockToken)
    localStorage.setItem('authUser', JSON.stringify(mockUser))

    setTokenState(mockToken)
    setUser(mockUser)
    setIsAuthenticated(true)
  }

  const logout = () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('authUser')
    setTokenState(null)
    setUser(null)
    setIsAuthenticated(false)
  }

  const setToken = (newToken: string) => {
    localStorage.setItem('authToken', newToken)
    setTokenState(newToken)
    setIsAuthenticated(true)
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, token, login, logout, setToken }}>
      {children}
    </AuthContext.Provider>
  )
}
