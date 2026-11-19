import React from 'react'

interface CardProps {
  children: React.ReactNode
  className?: string
}

interface CardHeaderProps {
  children: React.ReactNode
  className?: string
}

interface CardBodyProps {
  children: React.ReactNode
  className?: string
}

interface CardFooterProps {
  children: React.ReactNode
  className?: string
}

export const Card = ({ children, className = '' }: CardProps) => (
  <div className={`bg-white dark:bg-neutral-800 rounded-lg shadow-md border border-neutral-200 dark:border-neutral-700 ${className}`}>
    {children}
  </div>
)

export const CardHeader = ({ children, className = '' }: CardHeaderProps) => (
  <div className={`px-6 py-4 border-b border-neutral-200 dark:border-neutral-700 ${className}`}>{children}</div>
)

export const CardBody = ({ children, className = '' }: CardBodyProps) => (
  <div className={`px-6 py-4 ${className}`}>{children}</div>
)

export const CardFooter = ({ children, className = '' }: CardFooterProps) => (
  <div className={`px-6 py-4 border-t border-neutral-200 dark:border-neutral-700 flex justify-end gap-3 ${className}`}>
    {children}
  </div>
)
