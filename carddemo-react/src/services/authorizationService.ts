import { apiClient } from './api'
import { Authorization, AuthorizationRequest } from '@/types'

/**
 * Authorization Service
 * Handles all authorization-related API calls
 */

export interface AuthorizationListParams {
  status?: string
  cardId?: string
  fromDate?: string
  toDate?: string
  page?: number
  size?: number
}

export interface AuthorizationSummary {
  totalCount: number
  approvedCount: number
  declinedCount: number
  pendingCount: number
  totalAmount: number
  averageAmount: number
}

export const authorizationService = {
  /**
   * GET /api/authorizations/{id}
   * Get authorization details by ID
   */
  getAuthorization: async (id: string): Promise<Authorization> => {
    const response = await apiClient.get<Authorization>(`/api/authorizations/${id}`)
    return response.data
  },

  /**
   * POST /api/authorizations
   * Create a new authorization request
   */
  createAuthorization: async (data: AuthorizationRequest): Promise<Authorization> => {
    const response = await apiClient.post<Authorization>('/api/authorizations', data)
    return response.data
  },

  /**
   * GET /api/authorizations
   * List authorizations with optional filters
   */
  listAuthorizations: async (params?: AuthorizationListParams): Promise<Authorization[]> => {
    const response = await apiClient.get<Authorization[]>('/api/authorizations', { params })
    return response.data
  },

  /**
   * PUT /api/authorizations/{id}/approve
   * Approve an authorization
   */
  approveAuthorization: async (id: string): Promise<Authorization> => {
    const response = await apiClient.put<Authorization>(`/api/authorizations/${id}/approve`, {})
    return response.data
  },

  /**
   * PUT /api/authorizations/{id}/decline
   * Decline an authorization with optional reason
   */
  declineAuthorization: async (id: string, reason?: string): Promise<Authorization> => {
    const response = await apiClient.put<Authorization>(`/api/authorizations/${id}/decline`, {
      reason,
    })
    return response.data
  },

  /**
   * GET /api/authorizations/reports/summary
   * Get authorization summary statistics
   */
  getSummaryReport: async (): Promise<AuthorizationSummary> => {
    const response = await apiClient.get<AuthorizationSummary>('/api/authorizations/reports/summary')
    return response.data
  },

  /**
   * GET /api/authorizations/pending
   * Get all pending authorizations
   */
  getPendingAuthorizations: async (): Promise<Authorization[]> => {
    const response = await apiClient.get<Authorization[]>('/api/authorizations/pending')
    return response.data
  },
}
