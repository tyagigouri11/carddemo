import React, { useEffect } from 'react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { Loading } from '@/components/common/Loading'
import { ErrorBanner } from '@/components/common/ErrorBanner'
import { useAccountStore } from '@/store/accountStore'
import { useAuthorizationStore } from '@/store/authorizationStore'

export const Dashboard: React.FC = () => {
  const selectedAccount = useAccountStore((s) => s.selectedAccount)
  const accountLoading = useAccountStore((s) => s.loading)
  const accountError = useAccountStore((s) => s.error)
  const authorizations = useAuthorizationStore((s) => s.authorizations) || []
  const authLoading = useAuthorizationStore((s) => s.loading)

  useEffect(() => {
    // Load initial data if needed
  }, [])

  // Filter pending authorizations
  const pendingAuthorizations = authorizations.filter((auth) => auth.status === 'PENDING')

  return (
    <MainLayout title="Dashboard">
      <div className="grid grid-cols-1 gap-6">
        {/* Welcome Card */}
        <Card>
          <CardHeader>
            <h2 className="text-lg font-semibold">Welcome to CardDemo</h2>
          </CardHeader>
          <CardBody>
            <p className="text-neutral-600 dark:text-neutral-400">
              Select a menu item from the left to get started. Use the <strong>Authorizations</strong> menu to create and manage card authorizations.
            </p>
          </CardBody>
        </Card>

        {/* Account Summary */}
        {selectedAccount && (
          <Card>
            <CardHeader>
              <h2 className="text-lg font-semibold">Account Summary</h2>
            </CardHeader>
            <CardBody>
              {accountLoading && <Loading />}
              {accountError && <ErrorBanner message={accountError} />}
              {selectedAccount && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-neutral-600 dark:text-neutral-400">Account ID</p>
                    <p className="text-lg font-semibold text-neutral-900 dark:text-white">{selectedAccount.id}</p>
                  </div>
                  <div>
                    <p className="text-sm text-neutral-600 dark:text-neutral-400">Credit Limit</p>
                    <p className="text-lg font-semibold text-neutral-900 dark:text-white">
                      ${selectedAccount.creditLimit.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-neutral-600 dark:text-neutral-400">Current Balance</p>
                    <p className="text-lg font-semibold text-neutral-900 dark:text-white">
                      ${selectedAccount.currentBalance.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-neutral-600 dark:text-neutral-400">Status</p>
                    <p className="text-lg font-semibold text-success-600">{selectedAccount.activeStatus}</p>
                  </div>
                </div>
              )}
            </CardBody>
          </Card>
        )}

        {/* Pending Authorizations */}
        <Card>
          <CardHeader>
            <h2 className="text-lg font-semibold">Pending Authorizations</h2>
          </CardHeader>
          <CardBody>
            {authLoading && <Loading />}
            {pendingAuthorizations.length === 0 ? (
              <p className="text-neutral-600 dark:text-neutral-400">No pending authorizations</p>
            ) : (
              <div className="space-y-3">
                {pendingAuthorizations.slice(0, 5).map((auth) => (
                  <div key={auth.id} className="p-3 bg-primary-50 dark:bg-primary-900 rounded-lg">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-semibold text-neutral-900 dark:text-white">{auth.merchantName}</p>
                        <p className="text-sm text-neutral-600 dark:text-neutral-400">${auth.amount.toFixed(2)}</p>
                      </div>
                      <span className="text-xs bg-primary-200 dark:bg-primary-700 text-primary-900 dark:text-primary-100 px-2 py-1 rounded">
                        PENDING
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </MainLayout>
  )
}
