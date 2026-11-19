import { apiClient } from './api'
import { Transaction, TransactionDto, TransactionRequest } from '@/types'

/**
 * Transaction Service
 * Handles all transaction-related API calls
 */

export const transactionService = {
  /**
   * GET /api/transactions
   * Get all transactions
   */
  getAllTransactions: async (): Promise<Transaction[]> => {
    const response = await apiClient.get<Transaction[]>('/api/transactions')
    return response.data
  },

  /**
   * GET /api/transactions/{id}
   * Get transaction details by ID
   */
  getTransaction: async (id: string): Promise<Transaction> => {
    const response = await apiClient.get<Transaction>(`/api/transactions/${id}`)
    return response.data
  },

  /**
   * POST /api/transactions
   * Create a new transaction
   */
  postTransaction: async (data: TransactionRequest): Promise<Transaction> => {
    const response = await apiClient.post<Transaction>('/api/transactions', data)
    return response.data
  },

  /**
   * POST /api/transactions/settle
   * Settle/close a transaction
   */
  settleTransaction: async (transactionId: string): Promise<Transaction> => {
    const response = await apiClient.post<Transaction>('/api/transactions/settle', {
      transactionId,
    })
    return response.data
  },

  /**
   * GET /api/transactions/reports/transactions
   * Generate transaction report with optional date filter
   */
  generateReport: async (options?: { asOf?: string }): Promise<Record<string, unknown>> => {
    const response = await apiClient.get<Record<string, unknown>>(
      '/api/transactions/reports/transactions',
      {
        params: options,
      }
    )
    return response.data
  },
}
