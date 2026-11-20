/**
 * authStore.ts - Zustand store for authentication state
 * Manages user authentication state and actions globally
 */

import { create } from 'zustand';
import * as authService from '../services/authService';

export interface User {
  username: string;
  email: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (username: string, password: string) => Promise<void>;
  signup: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  initializeAuth: () => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  /**
   * Login user with username and password
   */
  login: async (username: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.login({ username, password });
      
      // Store token and user
      authService.storeToken(response.token);
      authService.storeUser({
        username: response.username,
        email: response.email,
      });

      set({
        user: {
          username: response.username,
          email: response.email,
        },
        token: response.token,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Login failed';
      set({
        error: errorMessage,
        isLoading: false,
      });
      throw error;
    }
  },

  /**
   * Sign up new user
   */
  signup: async (username: string, email: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      await authService.signup({ username, email, password });
      
      set({
        error: null,
        isLoading: false,
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Signup failed';
      set({
        error: errorMessage,
        isLoading: false,
      });
      throw error;
    }
  },

  /**
   * Logout user - clear token and user data
   */
  logout: () => {
    authService.clearAuthData();
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      error: null,
    });
  },

  /**
   * Initialize auth - load token and user from localStorage on app start
   */
  initializeAuth: () => {
    const token = authService.getToken();
    const user = authService.getUser();

    if (token && user) {
      set({
        token,
        user,
        isAuthenticated: true,
      });
    } else {
      set({
        token: null,
        user: null,
        isAuthenticated: false,
      });
    }
  },

  /**
   * Clear error message
   */
  clearError: () => {
    set({ error: null });
  },
}));
