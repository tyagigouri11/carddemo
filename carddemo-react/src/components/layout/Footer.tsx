import React from 'react'

export const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear()
  const version = import.meta.env.VITE_APP_VERSION || '1.0.0'

  return (
    <footer className="bg-neutral-50 dark:bg-neutral-800 border-t border-neutral-200 dark:border-neutral-700 py-6 px-4">
      <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-neutral-600 dark:text-neutral-400">
        <p>&copy; {currentYear} CardDemo. All rights reserved.</p>
        <div className="flex items-center gap-6">
          <a href="#" className="hover:text-neutral-900 dark:hover:text-white transition-colors">
            Privacy Policy
          </a>
          <a href="#" className="hover:text-neutral-900 dark:hover:text-white transition-colors">
            Terms of Service
          </a>
          <span>v{version}</span>
        </div>
      </div>
    </footer>
  )
}
