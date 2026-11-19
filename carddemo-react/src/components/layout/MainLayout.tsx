import React from 'react'
import { Header } from './Header'
import { Sidebar } from './Sidebar'
import { Footer } from './Footer'
import { ToastContainer } from '@/components/common/Toast'
import { useUiStore } from '@/store/uiStore'

interface MainLayoutProps {
  children: React.ReactNode
  title?: string
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children, title }) => {
  const toastNotifications = useUiStore((state) => state.toastNotifications)
  const removeToast = useUiStore((state) => state.removeToast)

  return (
    <div className="min-h-screen bg-neutral-50 dark:bg-neutral-900 flex flex-col">
      {/* Header */}
      <Header title={title} />

      {/* Main Content Area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar */}
        <Sidebar />

        {/* Content */}
        <main className="flex-1 overflow-y-auto">
          <div className="p-4 sm:p-6">{children}</div>

          {/* Footer */}
          <Footer />
        </main>
      </div>

      {/* Toast Notifications */}
      <ToastContainer toasts={toastNotifications} onRemove={removeToast} />
    </div>
  )
}
