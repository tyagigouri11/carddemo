import { create } from 'zustand'
import { Theme, Toast } from '@/types'

interface UiState {
  sidebarOpen: boolean
  theme: Theme
  toastNotifications: Toast[]

  // Actions
  setSidebarOpen: (open: boolean) => void
  toggleSidebar: () => void
  setTheme: (theme: Theme) => void
  toggleTheme: () => void
  addToast: (toast: Omit<Toast, 'id'>) => void
  removeToast: (id: string) => void
  clearToasts: () => void
}

export const useUiStore = create<UiState>((set) => ({
  sidebarOpen: true,
  theme: (localStorage.getItem('theme') as Theme) || 'light',
  toastNotifications: [],

  setSidebarOpen: (open) => set({ sidebarOpen: open }),
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),

  setTheme: (theme) => {
    localStorage.setItem('theme', theme)
    // Apply theme to document
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    set({ theme })
  },

  toggleTheme: () =>
    set((state) => {
      const newTheme: Theme = state.theme === 'light' ? 'dark' : 'light'
      localStorage.setItem('theme', newTheme)
      if (newTheme === 'dark') {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
      return { theme: newTheme }
    }),

  addToast: (toast) => {
    const id = Math.random().toString(36).substr(2, 9)
    const newToast: Toast = { ...toast, id }

    set((state) => ({
      toastNotifications: [...state.toastNotifications, newToast],
    }))

    // Auto-remove toast after duration (default 3s)
    setTimeout(() => {
      set((state) => ({
        toastNotifications: state.toastNotifications.filter((t) => t.id !== id),
      }))
    }, toast.duration || 3000)
  },

  removeToast: (id) =>
    set((state) => ({
      toastNotifications: state.toastNotifications.filter((t) => t.id !== id),
    })),

  clearToasts: () => set({ toastNotifications: [] }),
}))
