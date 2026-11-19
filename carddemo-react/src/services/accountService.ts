import { apiClient } from './api'
import { Account, AccountDto, Transaction, Card } from '@/types'

/**
 * Account Service
 * Handles all account-related API calls
 */

export const accountService = {
  /**
   * GET /api/accounts
   * Get all accounts
   */
  getAccounts: async (): Promise<Account[]> => {
    const response = await apiClient.get<Account[]>('/api/accounts')
    return response.data
  },

  /**
   * GET /api/accounts/{id}
   */
  getAccount: async (id: string): Promise<Account> => {
    const response = await apiClient.get<Account>(`/api/accounts/${id}`)
    return response.data
  },

  /**
   * PUT /api/accounts/{id}
   * Update account details (creditLimit, currentBalance, activeStatus)
   */
  updateAccount: async (id: string, data: Partial<AccountDto>): Promise<Account> => {
    const response = await apiClient.put<Account>(`/api/accounts/${id}`, data)
    return response.data
  },

  /**
   * GET /api/accounts/{id}/transactions
   * Get transactions for an account with optional date filters
   */
  getAccountTransactions: async (
    id: string,
    options?: { from?: string; to?: string }
  ): Promise<Transaction[]> => {
    const response = await apiClient.get<Transaction[]>(`/api/accounts/${id}/transactions`, {
      params: options,
    })
    return response.data
  },

  /**
   * POST /api/accounts/{id}/balance
   * Update account balance (process payment/debit)
   */
  updateBalance: async (
    id: string,
    data: {
      amount: number
      type: 'PURCHASE' | 'PAYMENT' | 'CASH_ADVANCE' | 'FEE'
      merchantId: string
      merchantName: string
    }
  ): Promise<Account> => {
    const response = await apiClient.post<Account>(`/api/accounts/${id}/balance`, data)
    return response.data
  },

  /**
   * POST /api/accounts/{id}/reissue
   * Reissue a card for the account
   */
  reissueCard: async (
    id: string,
    data: {
      cardNumber: string
      expiryDate: string
    }
  ): Promise<Card> => {
    const response = await apiClient.post<Card>(`/api/accounts/${id}/reissue`, data)
    return response.data
  },

  /**
   * GET /api/accounts/{id}/cards
   * Get all cards associated with an account
   */
  getAccountCards: async (id: string): Promise<Card[]> => {
    const response = await apiClient.get<Card[]>(`/api/accounts/${id}/cards`)
    return response.data
  },
}
