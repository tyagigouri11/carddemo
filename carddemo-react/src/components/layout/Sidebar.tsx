import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { X, Moon, Sun } from 'lucide-react'
import {
  LayoutDashboard,
  Banknote,
  CreditCard,
  ArrowRightLeft,
  CheckSquare,
  BarChart3,
  Settings,
} from 'lucide-react'
import { useUiStore } from '@/store/uiStore'

const navItems = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/accounts', label: 'Accounts', icon: Banknote },
  { path: '/transactions', label: 'Transactions', icon: ArrowRightLeft },
  { path: '/authorizations', label: 'Authorizations', icon: CheckSquare },
  { path: '/reports', label: 'Reports', icon: BarChart3 },
  { path: '/settings', label: 'Settings', icon: Settings },
]

export const Sidebar: React.FC = () => {
  const location = useLocation()
  const sidebarOpen = useUiStore((state) => state.sidebarOpen)
  const toggleSidebar = useUiStore((state) => state.toggleSidebar)
  const theme = useUiStore((state) => state.theme)
  const toggleTheme = useUiStore((state) => state.toggleTheme)

  return (
    <>
      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-neutral-900 dark:bg-neutral-950 text-white border-r border-neutral-800 transform transition-transform duration-200 ease-in-out lg:static lg:transform-none ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        role="navigation"
      >
        <div className="h-screen flex flex-col overflow-y-auto">
          {/* Logo / Brand */}
          <div className="h-16 flex items-center justify-between px-6 border-b border-neutral-800">
            <div className="flex items-center gap-2">
              <CreditCard size={24} className="text-primary-500" />
              <span className="font-bold text-lg">CardDemo</span>
            </div>
            <button
              onClick={toggleSidebar}
              className="p-2 hover:bg-neutral-800 rounded-lg transition-colors lg:hidden"
              aria-label="Close menu"
            >
              <X size={20} />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-6 space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = location.pathname === item.path
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => {
                    if (window.innerWidth < 1024) {
                      toggleSidebar()
                    }
                  }}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-primary-600 text-white'
                      : 'text-neutral-400 hover:text-white hover:bg-neutral-800'
                  }`}
                  aria-current={isActive ? 'page' : undefined}
                >
                  <Icon size={20} />
                  <span className="font-medium">{item.label}</span>
                </Link>
              )
            })}
          </nav>

          {/* Theme toggle */}
          <div className="px-4 py-6 border-t border-neutral-800">
            <button
              onClick={toggleTheme}
              className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg bg-neutral-800 text-neutral-300 hover:bg-neutral-700 hover:text-white transition-colors"
              aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`}
            >
              {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
              <span className="text-sm font-medium">{theme === 'light' ? 'Dark' : 'Light'} Mode</span>
            </button>
          </div>
        </div>
      </aside>

      {/* Overlay for mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-20 bg-black bg-opacity-50 lg:hidden"
          onClick={toggleSidebar}
          aria-hidden="true"
          role="presentation"
        />
      )}
    </>
  )
}
