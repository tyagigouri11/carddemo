import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ApiError } from '@/types'

/**
 * Error handler: Map HTTP status codes to user-friendly messages
 */
export const mapErrorToMessage = (status: number, message?: string): string => {
  const errorMap: Record<number, string> = {
    400: 'Invalid input. Please check your data.',
    401: 'Session expired. Please log in again.',
    403: 'You do not have permission to perform this action.',
    404: 'Resource not found.',
    409: 'Conflict. This resource may already exist.',
    422: 'Validation failed. Please check your input.',
    500: 'Server error. Please try again later.',
    502: 'Gateway error. Please try again later.',
    503: 'Service unavailable. Please try again later.',
    504: 'Gateway timeout. Please try again later.',
  }

  return message || errorMap[status] || 'An unexpected error occurred. Please try again.'
}

/**
 * Retry logic with exponential backoff
 */
const retryWithBackoff = async (
  config: InternalAxiosRequestConfig,
  retries: number = 3
): Promise<InternalAxiosRequestConfig> => {
  const maxRetries = retries
  let retryCount = 0

  const attempt = async (): Promise<InternalAxiosRequestConfig> => {
    try {
      return config
    } catch (error) {
      if (retryCount < maxRetries) {
        retryCount++
        const delay = Math.pow(2, retryCount) * 1000 // exponential backoff: 2s, 4s, 8s
        await new Promise((resolve) => setTimeout(resolve, delay))
        return attempt()
      }
      throw error
    }
  }

  return attempt()
}

/**
 * Create Axios instance with global configuration
 */
const createApiInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  /**
   * Request interceptor: Add auth token and prepare request
   */
  instance.interceptors.request.use(
    async (config) => {
      // Add auth token from localStorage (placeholder for future OAuth2)
      const token = localStorage.getItem('authToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }

      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  /**
   * Response interceptor: Handle errors and map to user-friendly messages
   */
  instance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const status = error.response?.status || 500
      const data = error.response?.data as Record<string, unknown> | undefined

      // Retry logic for 5xx errors and timeouts
      if ((status >= 500 || error.code === 'ECONNABORTED') && error.config) {
        const retryCount = (error.config as Record<string, unknown>)._retryCount as number | undefined
        if (!retryCount || retryCount < 3) {
          const config = error.config as Record<string, unknown>
          config._retryCount = (retryCount || 0) + 1
          await new Promise((resolve) => setTimeout(resolve, Math.pow(2, (retryCount || 0) + 1) * 1000))
          return instance(error.config as InternalAxiosRequestConfig)
        }
      }

      const apiError: ApiError = {
        status,
        message: mapErrorToMessage(status, data?.message as string | undefined),
        code: (data?.code as string) || 'UNKNOWN_ERROR',
        timestamp: new Date().toISOString(),
      }

      return Promise.reject(apiError)
    }
  )

  return instance
}

export const apiClient = createApiInstance()

/**
 * Export types for service usage
 */
export type { AxiosInstance, InternalAxiosRequestConfig }
