import React, { useState, useEffect } from 'react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { Account } from '@/types'
import { accountService } from '@/services/accountService'

export const AccountManagement: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        setLoading(true)
        const data = await accountService.getAccounts()
        setAccounts(data)
        setError(null)
      } catch (err) {
        setError('Failed to load accounts')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    fetchAccounts()
  }, [])

  return (
    <MainLayout title="Account Management">
      <Card>
        <CardHeader>
          <h2 className="text-lg font-semibold">Accounts</h2>
        </CardHeader>
        <CardBody>
          {loading && <p className="text-neutral-600 dark:text-neutral-400">Loading accounts...</p>}
          {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
          {!loading && accounts.length === 0 && (
            <p className="text-neutral-600 dark:text-neutral-400">No accounts found.</p>
          )}
          {!loading && accounts.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-neutral-300 dark:border-neutral-700">
                    <th className="text-left px-4 py-2 font-semibold">ID</th>
                    <th className="text-left px-4 py-2 font-semibold">Credit Limit</th>
                    <th className="text-left px-4 py-2 font-semibold">Current Balance</th>
                    <th className="text-left px-4 py-2 font-semibold">Status</th>
                    <th className="text-left px-4 py-2 font-semibold">Open Date</th>
                  </tr>
                </thead>
                <tbody>
                  {accounts.map((account) => (
                    <tr
                      key={account.id}
                      className="border-b border-neutral-200 dark:border-neutral-800 hover:bg-neutral-50 dark:hover:bg-neutral-900"
                    >
                      <td className="px-4 py-2">{account.id}</td>
                      <td className="px-4 py-2">${account.creditLimit.toLocaleString()}</td>
                      <td className="px-4 py-2">${account.currentBalance.toLocaleString()}</td>
                      <td className="px-4 py-2">
                        <span
                          className={`px-3 py-1 rounded-full text-xs font-medium ${
                            account.activeStatus === true
                              ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                              : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                          }`}
                        >
                          {account.activeStatus ? 'ACTIVE' : 'INACTIVE'}
                        </span>
                      </td>
                      <td className="px-4 py-2">{new Date(account.openDate).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardBody>
      </Card>
    </MainLayout>
  )
}
