import React, { useEffect } from 'react'
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-react'
import { Toast as ToastType } from '@/types'

interface ToastProps extends ToastType {
  onRemove: (id: string) => void
}

export const Toast: React.FC<ToastProps> = ({ id, message, type, duration = 3000, onRemove }) => {
  useEffect(() => {
    if (duration) {
      const timer = setTimeout(() => onRemove(id), duration)
      return () => clearTimeout(timer)
    }
  }, [id, duration, onRemove])

  const icons = {
    success: <CheckCircle className="text-success-600" size={20} />,
    error: <AlertCircle className="text-danger-600" size={20} />,
    warning: <AlertTriangle className="text-warning-600" size={20} />,
    info: <Info className="text-primary-600" size={20} />,
  }

  const bgColors = {
    success: 'bg-success-50 border-success-200 dark:bg-success-900 dark:border-success-700',
    error: 'bg-danger-50 border-danger-200 dark:bg-danger-900 dark:border-danger-700',
    warning: 'bg-warning-50 border-warning-200 dark:bg-warning-900 dark:border-warning-700',
    info: 'bg-primary-50 border-primary-200 dark:bg-primary-900 dark:border-primary-700',
  }

  const textColors = {
    success: 'text-success-800 dark:text-success-200',
    error: 'text-danger-800 dark:text-danger-200',
    warning: 'text-warning-800 dark:text-warning-200',
    info: 'text-primary-800 dark:text-primary-200',
  }

  return (
    <div
      className={`flex items-center gap-3 px-4 py-3 rounded-lg border ${bgColors[type]} ${textColors[type]} animate-slide-in-right`}
      role="status"
      aria-live="polite"
    >
      {icons[type]}
      <span className="flex-1 text-sm font-medium">{message}</span>
      <button
        onClick={() => onRemove(id)}
        className="text-current hover:opacity-70 transition-opacity"
        aria-label="Close notification"
      >
        <X size={18} />
      </button>
    </div>
  )
}

interface ToastContainerProps {
  toasts: ToastType[]
  onRemove: (id: string) => void
}

export const ToastContainer: React.FC<ToastContainerProps> = ({ toasts, onRemove }) => {
  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm">
      {toasts.map((toast) => (
        <Toast key={toast.id} {...toast} onRemove={onRemove} />
      ))}
    </div>
  )
}
