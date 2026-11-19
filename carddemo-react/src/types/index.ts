/**
 * Account-related types
 */
export type AccountStatus = boolean // Backend returns boolean for activeStatus

export interface Account {
  id: number // Backend returns Long (number)
  creditLimit: number
  currentBalance: number
  activeStatus: AccountStatus
  reissueDate: string | null
  openDate: string
  expirationDate: string | null
  cashCreditLimit?: number
  currentCycleDebit: number
  currentCycleCredit: number
  groupId?: string | null
}

export interface AccountDto {
  id: number
  creditLimit: number
  currentBalance: number
  activeStatus: AccountStatus
  reissueDate?: string | null
  openDate: string
  expirationDate?: string | null
  cashCreditLimit?: number
  currentCycleDebit: number
  currentCycleCredit: number
  groupId?: string | null
}

/**
 * Card-related types
 */
export type CardStatus = 'ACTIVE' | 'EXPIRED' | 'BLOCKED'

export interface Card {
  id: string
  number: string // PAN
  accountId: string
  status: CardStatus
  expiryDate: string
}

export interface CardDto {
  id: string
  number: string
  accountId: string
  status: CardStatus
  expiryDate: string
}

/**
 * Transaction-related types
 */
export type TransactionType = 'PURCHASE' | 'CASH_ADVANCE' | 'PAYMENT' | 'FEE' | 'INTEREST'
export type TransactionStatus = 'PENDING' | 'POSTED' | 'SETTLED'

export interface Transaction {
  id: string
  accountId: string
  cardNumber: string
  amount: number
  date: string
  postDate?: string | null
  type: TransactionType
  status: TransactionStatus
  merchantId: string
  merchantName: string
  processingCode?: string | null
}

export interface TransactionDto {
  id: string
  accountId: string
  cardNumber: string
  amount: number
  date: string
  postDate?: string | null
  type: TransactionType
  status: TransactionStatus
  merchantId: string
  merchantName: string
  processingCode?: string | null
}

export interface TransactionRequest {
  accountId: string
  cardNumber: string
  amount: number
  type: TransactionType
  merchantId: string
  merchantName: string
  date?: string
}

/**
 * Authorization-related types
 */
export type AuthorizationStatus = 'PENDING' | 'APPROVED' | 'DECLINED'

export interface Authorization {
  id: string
  accountId: string
  cardNumber: string
  amount: number
  merchantId: string
  merchantName: string
  requestDate: string
  responseDate?: string | null
  expiryDate: string
  status: AuthorizationStatus
  declineReasonCode?: string | null
  approvedAmount?: number | null
}

export interface AuthorizationDto {
  id: string
  accountId: string
  cardNumber: string
  amount: number
  merchantId: string
  merchantName: string
  requestDate: string
  responseDate?: string | null
  expiryDate: string
  status: AuthorizationStatus
  declineReasonCode?: string | null
  approvedAmount?: number | null
}

export interface AuthorizationRequest {
  accountId: string
  cardNumber: string
  amount: number
  merchantId: string
  merchantName: string
}

export interface AuthorizationReview {
  decision: 'APPROVED' | 'DECLINED'
  notes?: string
  approvedAmount?: number
}

/**
 * API Response types
 */
export interface ApiResponse<T> {
  data: T
  message?: string
  status: 'success' | 'error'
}

export interface ApiError {
  status: number
  message: string
  code: string
  timestamp?: string
}

/**
 * User/Auth types (placeholder for future OAuth2)
 */
export interface User {
  id: string
  username: string
  email: string
  role: 'ADMIN' | 'USER'
}

export interface AuthToken {
  accessToken: string
  refreshToken?: string
  expiresIn: number
}

/**
 * UI types
 */
export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: string
  message: string
  type: ToastType
  duration?: number
}

export type Theme = 'light' | 'dark'
