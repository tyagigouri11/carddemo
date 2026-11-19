import React from 'react'

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  required?: boolean
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, required, className = '', ...props }, ref) => {
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label className="text-sm font-medium text-neutral-700 dark:text-neutral-300">
            {label}
            {required && <span className="text-danger-600 ml-1">*</span>}
          </label>
        )}
        <input
          ref={ref}
          className={`px-3 py-2 border rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-neutral-800 dark:text-white dark:border-neutral-600 ${
            error ? 'border-danger-500' : 'border-neutral-300'
          } ${className}`}
          aria-invalid={!!error}
          aria-describedby={error ? `${props.id}-error` : undefined}
          {...props}
        />
        {error && (
          <p id={`${props.id}-error`} className="text-sm text-danger-600">
            {error}
          </p>
        )}
      </div>
    )
  }
)

Input.displayName = 'Input'
