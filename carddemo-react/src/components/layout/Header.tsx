import React from 'react'
import { Menu, LogOut } from 'lucide-react'
import { useUiStore } from '@/store/uiStore'
import { useAuth } from '@/hooks/useAuth'

interface HeaderProps {
  title?: string
}

export const Header: React.FC<HeaderProps> = ({ title = 'CardDemo' }) => {
  const toggleSidebar = useUiStore((state) => state.toggleSidebar)
  const { logout } = useAuth()

  return (
    <header className="bg-white dark:bg-neutral-800 border-b border-neutral-200 dark:border-neutral-700 sticky top-0 z-40">
      <div className="flex items-center justify-between h-16 px-4 sm:px-6">
        {/* Left: Menu toggle + Title */}
        <div className="flex items-center gap-4">
          <button
            onClick={toggleSidebar}
            className="p-2 hover:bg-neutral-100 dark:hover:bg-neutral-700 rounded-lg transition-colors lg:hidden"
            aria-label="Toggle menu"
          >
            <Menu size={24} className="text-neutral-600 dark:text-neutral-400" />
          </button>
          <h1 className="text-xl font-bold text-neutral-900 dark:text-white">{title}</h1>
        </div>

        {/* Right: User info */}
        <div className="flex items-center gap-4">
          <button
            onClick={logout}
            className="flex items-center gap-2 px-3 py-2 text-sm text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-700 rounded-lg transition-colors"
            aria-label="Logout"
          >
            <LogOut size={18} />
            <span className="hidden sm:inline">Logout</span>
          </button>
        </div>
      </div>
    </header>
  )
}
