import { apiClient } from './api'
import { Card, CardDto } from '@/types'

/**
 * Card Service
 * Handles all card-related API calls
 */

export const cardService = {
  /**
   * GET /api/cards/{number}
   * Get card details by card number (PAN)
   */
  getCard: async (number: string): Promise<Card> => {
    const response = await apiClient.get<Card>(`/api/cards/${number}`)
    return response.data
  },

  /**
   * POST /api/cards
   * Issue a new card
   */
  issueCard: async (data: CardDto): Promise<Card> => {
    const response = await apiClient.post<Card>('/api/cards', data)
    return response.data
  },

  /**
   * GET /api/cards/account/{accountId}
   * Get all cards for a specific account
   */
  getCardsByAccount: async (accountId: string): Promise<Card[]> => {
    const response = await apiClient.get<Card[]>(`/api/cards/account/${accountId}`)
    return response.data
  },
}
