import { create } from 'zustand'
import { Transaction, TransactionStatus } from '@/types'
import { transactionService } from '@/services/transactionService'
import { ApiError } from '@/types'

interface TransactionFilter {
  dateRange?: {
    from: string
    to: string
  }
  status?: TransactionStatus
  accountId?: string
}

interface TransactionState {
  selectedTransaction: Transaction | null
  transactions: Transaction[]
  filter: TransactionFilter
  loading: boolean
  error: string | null

  // Actions
  setSelectedTransaction: (tx: Transaction | null) => void
  setTransactions: (txs: Transaction[]) => void
  setFilter: (filter: TransactionFilter) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  clearError: () => void

  // Async loaders
  loadTransaction: (id: string) => Promise<void>
  postTransaction: (data: Parameters<typeof transactionService.postTransaction>[0]) => Promise<void>
  settleTransaction: (id: string) => Promise<void>
  generateReport: (options?: { asOf?: string }) => Promise<Record<string, unknown>>
}

export const useTransactionStore = create<TransactionState>((set) => ({
  selectedTransaction: null,
  transactions: [],
  filter: {},
  loading: false,
  error: null,

  setSelectedTransaction: (tx) => set({ selectedTransaction: tx }),
  setTransactions: (txs) => set({ transactions: txs }),
  setFilter: (filter) => set({ filter }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  clearError: () => set({ error: null }),

  loadTransaction: async (id) => {
    set({ loading: true, error: null })
    try {
      const tx = await transactionService.getTransaction(id)
      set({ selectedTransaction: tx, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load transaction'
      set({ error: message, loading: false })
    }
  },

  postTransaction: async (data) => {
    set({ loading: true, error: null })
    try {
      const tx = await transactionService.postTransaction(data)
      set((state) => ({
        transactions: [tx, ...state.transactions],
        selectedTransaction: tx,
        loading: false,
      }))
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to post transaction'
      set({ error: message, loading: false })
    }
  },

  settleTransaction: async (id) => {
    set({ loading: true, error: null })
    try {
      const tx = await transactionService.settleTransaction(id)
      set((state) => ({
        transactions: state.transactions.map((t) => (t.id === id ? tx : t)),
        selectedTransaction: tx,
        loading: false,
      }))
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to settle transaction'
      set({ error: message, loading: false })
    }
  },

  generateReport: async (options) => {
    set({ loading: true, error: null })
    try {
      const report = await transactionService.generateReport(options)
      set({ loading: false })
      return report
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to generate report'
      set({ error: message, loading: false })
      throw error
    }
  },
}))
