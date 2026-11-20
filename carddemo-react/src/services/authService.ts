/**
 * authService.ts - Authentication API calls
 * Handles signup, login, logout, and token management
 */

const API_BASE_URL = 'http://localhost:8080/api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  username: string;
  email: string;
  message?: string;
}

export interface User {
  username: string;
  email: string;
}

/**
 * Sign up a new user
 */
export async function signup(data: SignupRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/signup`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Signup failed');
  }

  return response.json();
}

/**
 * Login a user and get JWT token
 */
export async function login(data: LoginRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Login failed');
  }

  return response.json();
}

/**
 * Store JWT token in localStorage
 */
export function storeToken(token: string): void {
  localStorage.setItem('jwt_token', token);
}

/**
 * Retrieve JWT token from localStorage
 */
export function getToken(): string | null {
  return localStorage.getItem('jwt_token');
}

/**
 * Remove JWT token from localStorage (logout)
 */
export function removeToken(): void {
  localStorage.removeItem('jwt_token');
}

/**
 * Check if user has a valid token
 */
export function isAuthenticated(): boolean {
  return !!getToken();
}

/**
 * Store user info in localStorage
 */
export function storeUser(user: User): void {
  localStorage.setItem('user', JSON.stringify(user));
}

/**
 * Retrieve user info from localStorage
 */
export function getUser(): User | null {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

/**
 * Clear all auth data (logout)
 */
export function clearAuthData(): void {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user');
}

/**
 * Decode JWT token to get expiration time (for frontend reference)
 * Note: This is only for display purposes. Real validation happens on backend.
 */
export function getTokenExpiration(): Date | null {
  const token = getToken();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return new Date(payload.exp * 1000);
  } catch {
    return null;
  }
}

/**
 * Check if token is about to expire (within 5 minutes)
 */
export function isTokenExpiring(): boolean {
  const expiration = getTokenExpiration();
  if (!expiration) return false;

  const now = new Date();
  const fiveMinutesFromNow = new Date(now.getTime() + 5 * 60 * 1000);
  
  return expiration < fiveMinutesFromNow;
}
