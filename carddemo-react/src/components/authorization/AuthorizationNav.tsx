import React from 'react'
import { Button } from '@/components/common/Button'

export type AuthorizationView = 'create' | 'list' | 'reports'

interface AuthorizationNavProps {
  activeView: AuthorizationView
  onViewChange: (view: AuthorizationView) => void
}

export const AuthorizationNav: React.FC<AuthorizationNavProps> = ({ activeView, onViewChange }) => {
  const views = [
    { id: 'create', label: 'Create Authorization', icon: '➕' },
    { id: 'list', label: 'View Authorizations', icon: '📋' },
    { id: 'reports', label: 'Reports', icon: '📊' },
  ] as const

  return (
    <div className="flex flex-wrap gap-2 pb-6 border-b border-neutral-200 dark:border-neutral-700">
      {views.map((view) => (
        <Button
          key={view.id}
          variant={activeView === view.id ? 'primary' : 'secondary'}
          size="md"
          onClick={() => onViewChange(view.id)}
          className="flex items-center gap-2"
        >
          <span>{view.icon}</span>
          {view.label}
        </Button>
      ))}
    </div>
  )
}
