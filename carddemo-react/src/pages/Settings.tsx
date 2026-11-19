import React from 'react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { Button } from '@/components/common/Button'
import { useUiStore } from '@/store/uiStore'

export const Settings: React.FC = () => {
  const theme = useUiStore((s) => s.theme)
  const toggleTheme = useUiStore((s) => s.toggleTheme)

  return (
    <MainLayout title="Settings">
      <div className="max-w-2xl">
        <Card>
          <CardHeader>
            <h2 className="text-lg font-semibold">Preferences</h2>
          </CardHeader>
          <CardBody className="space-y-6">
            {/* Theme */}
            <div className="flex items-center justify-between py-4 border-b border-neutral-200 dark:border-neutral-700">
              <div>
                <h3 className="font-medium text-neutral-900 dark:text-white">Theme</h3>
                <p className="text-sm text-neutral-600 dark:text-neutral-400">Switch between light and dark mode</p>
              </div>
              <Button onClick={toggleTheme} variant="secondary">
                {theme === 'light' ? 'Dark Mode' : 'Light Mode'}
              </Button>
            </div>

            {/* About */}
            <div className="py-4">
              <h3 className="font-medium text-neutral-900 dark:text-white mb-2">About</h3>
              <dl className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <dt className="text-neutral-600 dark:text-neutral-400">Application</dt>
                  <dd className="font-medium text-neutral-900 dark:text-white">{import.meta.env.VITE_APP_NAME}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-neutral-600 dark:text-neutral-400">Version</dt>
                  <dd className="font-medium text-neutral-900 dark:text-white">{import.meta.env.VITE_APP_VERSION}</dd>
                </div>
              </dl>
            </div>
          </CardBody>
        </Card>
      </div>
    </MainLayout>
  )
}
