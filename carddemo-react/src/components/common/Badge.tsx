import React from 'react'
import { CardStatus, TransactionStatus, AuthorizationStatus, AccountStatus } from '@/types'

type BadgeStatus = CardStatus | TransactionStatus | AuthorizationStatus | AccountStatus

interface BadgeProps {
  status: BadgeStatus
  className?: string
}

export const Badge: React.FC<BadgeProps> = ({ status, className = '' }) => {
  const statusColorMap: Record<BadgeStatus, string> = {
    ACTIVE: 'bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200',
    BLOCKED: 'bg-danger-100 text-danger-800 dark:bg-danger-900 dark:text-danger-200',
    CLOSED: 'bg-neutral-100 text-neutral-800 dark:bg-neutral-900 dark:text-neutral-200',
    EXPIRED: 'bg-warning-100 text-warning-800 dark:bg-warning-900 dark:text-warning-200',
    PENDING: 'bg-primary-100 text-primary-800 dark:bg-primary-900 dark:text-primary-200',
    POSTED: 'bg-neutral-100 text-neutral-800 dark:bg-neutral-900 dark:text-neutral-200',
    SETTLED: 'bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200',
    APPROVED: 'bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200',
    DECLINED: 'bg-danger-100 text-danger-800 dark:bg-danger-900 dark:text-danger-200',
    PURCHASE: 'bg-neutral-100 text-neutral-800 dark:bg-neutral-900 dark:text-neutral-200',
    CASH_ADVANCE: 'bg-primary-100 text-primary-800 dark:bg-primary-900 dark:text-primary-200',
    PAYMENT: 'bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200',
    FEE: 'bg-warning-100 text-warning-800 dark:bg-warning-900 dark:text-warning-200',
    INTEREST: 'bg-warning-100 text-warning-800 dark:bg-warning-900 dark:text-warning-200',
  }

  const colorClass = statusColorMap[status as BadgeStatus] || 'bg-neutral-100 text-neutral-800'

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colorClass} ${className}`}>
      {status.replace(/_/g, ' ')}
    </span>
  )
}
