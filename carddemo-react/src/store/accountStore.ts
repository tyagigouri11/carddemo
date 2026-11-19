import { create } from 'zustand'
import { Account } from '@/types'
import { accountService } from '@/services/accountService'
import { ApiError } from '@/types'

interface AccountState {
  selectedAccount: Account | null
  accounts: Account[]
  loading: boolean
  error: string | null

  // Actions
  setSelectedAccount: (account: Account | null) => void
  setAccounts: (accounts: Account[]) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void

  // Async loaders
  loadAccount: (id: string) => Promise<void>
  updateAccount: (id: string, updates: Partial<Account>) => Promise<void>
}

export const useAccountStore = create<AccountState>((set) => ({
  selectedAccount: null,
  accounts: [],
  loading: false,
  error: null,

  setSelectedAccount: (account) => set({ selectedAccount: account }),
  setAccounts: (accounts) => set({ accounts }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),

  loadAccount: async (id) => {
    set({ loading: true, error: null })
    try {
      const account = await accountService.getAccount(id)
      set({ selectedAccount: account, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load account'
      set({ error: message, loading: false })
    }
  },

  updateAccount: async (id, updates) => {
    set({ loading: true, error: null })
    try {
      const updated = await accountService.updateAccount(id, updates as Parameters<typeof accountService.updateAccount>[1])
      set({ selectedAccount: updated, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to update account'
      set({ error: message, loading: false })
    }
  },
}))
