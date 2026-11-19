import React, { useState, useEffect } from 'react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { TransactionForm } from '@/components/forms/TransactionForm'
import { Transaction } from '@/types'
import { transactionService } from '@/services/transactionService'

export const TransactionManagement: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async () => {
    try {
      setLoading(true)
      const data = await transactionService.getAllTransactions()
      setTransactions(data)
      setError(null)
    } catch (err) {
      setError('Failed to load transactions')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions()
  }, [])

  return (
    <MainLayout title="Transactions">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <Card>
            <CardHeader>
              <h2 className="text-lg font-semibold">New Transaction</h2>
            </CardHeader>
            <CardBody>
              <TransactionForm onSuccess={fetchTransactions} />
            </CardBody>
          </Card>
        </div>
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <h2 className="text-lg font-semibold">Recent Transactions</h2>
            </CardHeader>
            <CardBody>
              {loading && <p className="text-neutral-600 dark:text-neutral-400">Loading transactions...</p>}
              {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
              {!loading && transactions.length === 0 && !error && (
                <div className="text-center py-8">
                  <p className="text-neutral-600 dark:text-neutral-400 mb-4">No transactions yet</p>
                  <p className="text-sm text-neutral-500 dark:text-neutral-500">Create a new transaction using the form on the left</p>
                </div>
              )}
              {!loading && transactions.length > 0 && (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-neutral-300 dark:border-neutral-700">
                        <th className="text-left px-4 py-2 font-semibold">ID</th>
                        <th className="text-left px-4 py-2 font-semibold">Account</th>
                        <th className="text-left px-4 py-2 font-semibold">Card</th>
                        <th className="text-left px-4 py-2 font-semibold">Amount</th>
                        <th className="text-left px-4 py-2 font-semibold">Type</th>
                        <th className="text-left px-4 py-2 font-semibold">Status</th>
                        <th className="text-left px-4 py-2 font-semibold">Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {transactions.map((tx) => (
                        <tr
                          key={tx.id}
                          className="border-b border-neutral-200 dark:border-neutral-800 hover:bg-neutral-50 dark:hover:bg-neutral-900"
                        >
                          <td className="px-4 py-2 text-xs">{tx.id}</td>
                          <td className="px-4 py-2">{tx.accountId}</td>
                          <td className="px-4 py-2 font-mono text-xs">{tx.cardNumber}</td>
                          <td className="px-4 py-2 font-semibold">${tx.amount.toLocaleString()}</td>
                          <td className="px-4 py-2">{tx.type}</td>
                          <td className="px-4 py-2">
                            <span
                              className={`px-2 py-1 rounded text-xs font-medium ${
                                tx.status === 'POSTED'
                                  ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                                  : tx.status === 'PENDING'
                                  ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
                                  : 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
                              }`}
                            >
                              {tx.status}
                            </span>
                          </td>
                          <td className="px-4 py-2 text-xs">{new Date(tx.date).toLocaleDateString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardBody>
          </Card>
        </div>
      </div>
    </MainLayout>
  )
}
