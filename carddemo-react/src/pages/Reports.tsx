import React, { useState, useEffect } from 'react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { transactionService } from '@/services/transactionService'
import { authorizationService } from '@/services/authorizationService'

interface TransactionReport {
  totalTransactions: number
  totalAmount: number
  [key: string]: unknown
}

interface AuthorizationSummary {
  totalCount: number
  approvedCount: number
  declinedCount: number
  pendingCount: number
  totalAmount: number
  averageAmount: number
}

export const Reports: React.FC = () => {
  const [transactionReport, setTransactionReport] = useState<TransactionReport | null>(null)
  const [authorizationSummary, setAuthorizationSummary] = useState<AuthorizationSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchReports = async () => {
      try {
        setLoading(true)
        const [txReport, authSummary] = await Promise.all([
          transactionService.generateReport(),
          authorizationService.getSummaryReport(),
        ])
        setTransactionReport(txReport as unknown as TransactionReport)
        setAuthorizationSummary(authSummary)
        setError(null)
      } catch (err) {
        setError('Failed to load reports')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    fetchReports()
  }, [])

  return (
    <MainLayout title="Reports">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Transaction Reports */}
        <Card>
          <CardHeader>
            <h2 className="text-lg font-semibold">Transaction Reports</h2>
          </CardHeader>
          <CardBody>
            {loading && <p className="text-neutral-600 dark:text-neutral-400">Loading reports...</p>}
            {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
            {!loading && transactionReport && (
              <div className="space-y-3">
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Total Transactions:</span>
                  <span>{transactionReport.totalTransactions || 0}</span>
                </div>
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Total Amount:</span>
                  <span>${(transactionReport.totalAmount as number || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                </div>
              </div>
            )}
          </CardBody>
        </Card>

        {/* Authorization Reports */}
        <Card>
          <CardHeader>
            <h2 className="text-lg font-semibold">Authorization Summary</h2>
          </CardHeader>
          <CardBody>
            {loading && <p className="text-neutral-600 dark:text-neutral-400">Loading reports...</p>}
            {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
            {!loading && authorizationSummary && (
              <div className="space-y-3">
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Total Count:</span>
                  <span className="font-semibold">{authorizationSummary.totalCount}</span>
                </div>
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Approved:</span>
                  <span className="text-green-600 dark:text-green-400 font-semibold">{authorizationSummary.approvedCount}</span>
                </div>
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Declined:</span>
                  <span className="text-red-600 dark:text-red-400 font-semibold">{authorizationSummary.declinedCount}</span>
                </div>
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Pending:</span>
                  <span className="text-orange-600 dark:text-orange-400 font-semibold">{authorizationSummary.pendingCount}</span>
                </div>
                <div className="flex justify-between border-b pb-2">
                  <span className="font-medium">Total Amount:</span>
                  <span>${authorizationSummary.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-medium">Average Amount:</span>
                  <span>${authorizationSummary.averageAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                </div>
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </MainLayout>
  )
}
