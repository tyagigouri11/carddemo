import { create } from 'zustand'
import { Authorization, AuthorizationStatus, AuthorizationRequest } from '@/types'
import { authorizationService, AuthorizationListParams, AuthorizationSummary } from '@/services/authorizationService'
import { ApiError } from '@/types'

interface AuthorizationFilter {
  status?: AuthorizationStatus
  cardId?: string
  dateRange?: {
    from: string
    to: string
  }
}

interface AuthorizationState {
  selectedAuthorization: Authorization | null
  authorizations: Authorization[]
  filter: AuthorizationFilter
  loading: boolean
  error: string | null
  summary: AuthorizationSummary | null

  // Actions
  setSelectedAuthorization: (auth: Authorization | null) => void
  setAuthorizations: (auths: Authorization[]) => void
  setFilter: (filter: AuthorizationFilter) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  clearError: () => void
  setSummary: (summary: AuthorizationSummary | null) => void

  // Async loaders
  loadAuthorization: (id: string) => Promise<void>
  loadAuthorizations: (params?: AuthorizationListParams) => Promise<void>
  createAuthorization: (data: AuthorizationRequest) => Promise<void>
  approveAuthorization: (id: string) => Promise<void>
  declineAuthorization: (id: string, reason?: string) => Promise<void>
  loadSummaryReport: () => Promise<void>
  loadPendingAuthorizations: () => Promise<void>
}

export const useAuthorizationStore = create<AuthorizationState>((set) => ({
  selectedAuthorization: null,
  authorizations: [],
  filter: {},
  loading: false,
  error: null,
  summary: null,

  setSelectedAuthorization: (auth) => set({ selectedAuthorization: auth }),
  setAuthorizations: (auths) => set({ authorizations: auths }),
  setFilter: (filter) => set({ filter }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  clearError: () => set({ error: null }),
  setSummary: (summary) => set({ summary }),

  loadAuthorization: async (id) => {
    set({ loading: true, error: null })
    try {
      const auth = await authorizationService.getAuthorization(id)
      set({ selectedAuthorization: auth, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load authorization'
      set({ error: message, loading: false })
    }
  },

  loadAuthorizations: async (params) => {
    set({ loading: true, error: null })
    try {
      const auths = await authorizationService.listAuthorizations(params)
      set({ authorizations: auths, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load authorizations'
      set({ error: message, loading: false })
    }
  },

  createAuthorization: async (data) => {
    set({ loading: true, error: null })
    try {
      const auth = await authorizationService.createAuthorization(data)
      set((state) => ({
        authorizations: [auth, ...state.authorizations],
        selectedAuthorization: auth,
        loading: false,
      }))
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to create authorization'
      set({ error: message, loading: false })
      throw error
    }
  },

  approveAuthorization: async (id) => {
    set({ loading: true, error: null })
    try {
      const auth = await authorizationService.approveAuthorization(id)
      set((state) => ({
        authorizations: state.authorizations.map((a) => (a.id === id ? auth : a)),
        selectedAuthorization: state.selectedAuthorization?.id === id ? auth : state.selectedAuthorization,
        loading: false,
      }))
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to approve authorization'
      set({ error: message, loading: false })
      throw error
    }
  },

  declineAuthorization: async (id, reason) => {
    set({ loading: true, error: null })
    try {
      const auth = await authorizationService.declineAuthorization(id, reason)
      set((state) => ({
        authorizations: state.authorizations.map((a) => (a.id === id ? auth : a)),
        selectedAuthorization: state.selectedAuthorization?.id === id ? auth : state.selectedAuthorization,
        loading: false,
      }))
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to decline authorization'
      set({ error: message, loading: false })
      throw error
    }
  },

  loadSummaryReport: async () => {
    set({ loading: true, error: null })
    try {
      const summary = await authorizationService.getSummaryReport()
      set({ summary, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load summary report'
      set({ error: message, loading: false })
    }
  },

  loadPendingAuthorizations: async () => {
    set({ loading: true, error: null })
    try {
      const auths = await authorizationService.getPendingAuthorizations()
      set({ authorizations: auths, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load pending authorizations'
      set({ error: message, loading: false })
    }
  },
}))
