import React from 'react'
import { AlertCircle, X } from 'lucide-react'

interface ErrorBannerProps {
  title?: string
  message: string
  onDismiss?: () => void
}

export const ErrorBanner: React.FC<ErrorBannerProps> = ({ title = 'Error', message, onDismiss }) => {
  return (
    <div
      className="bg-danger-50 dark:bg-danger-900 border border-danger-200 dark:border-danger-700 rounded-lg p-4 flex items-start gap-3"
      role="alert"
    >
      <AlertCircle className="text-danger-600 dark:text-danger-400 flex-shrink-0 mt-0.5" size={20} />
      <div className="flex-1">
        <h3 className="font-semibold text-danger-900 dark:text-danger-100">{title}</h3>
        <p className="text-sm text-danger-800 dark:text-danger-200 mt-1">{message}</p>
      </div>
      {onDismiss && (
        <button
          onClick={onDismiss}
          className="text-danger-600 hover:text-danger-700 dark:text-danger-400 dark:hover:text-danger-300 transition-colors flex-shrink-0"
          aria-label="Dismiss error"
        >
          <X size={20} />
        </button>
      )}
    </div>
  )
}
