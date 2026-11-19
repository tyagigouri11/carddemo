import React, { useEffect } from 'react'
import { Button } from '@/components/common/Button'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { useAuthorizationStore } from '@/store/authorizationStore'

export const AuthorizationReports: React.FC = () => {
  const { summary, loading, loadSummaryReport } = useAuthorizationStore()

  useEffect(() => {
    // Load summary on mount
    loadSummaryReport()

    // Set up auto-refresh every 30 seconds
    const interval = setInterval(() => {
      loadSummaryReport()
    }, 30000)

    return () => {
      if (interval) clearInterval(interval)
    }
  }, [loadSummaryReport])

  const handleManualRefresh = () => {
    loadSummaryReport()
  }

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount)
  }

  const ReportCard: React.FC<{
    title: string
    value: string | number
    icon: string
    bgColor: string
    textColor: string
  }> = ({ title, value, icon, bgColor, textColor }) => (
    <Card className={bgColor}>
      <CardBody className="space-y-3">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm font-medium text-neutral-700 dark:text-neutral-300">{title}</p>
            <p className={`text-3xl font-bold mt-1 ${textColor}`}>{value}</p>
          </div>
          <span className="text-4xl">{icon}</span>
        </div>
      </CardBody>
    </Card>
  )

  if (loading && !summary) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Refresh Control */}
      <div className="flex justify-end">
        <Button
          onClick={handleManualRefresh}
          variant="secondary"
          loading={loading}
        >
          {loading ? 'Refreshing...' : 'Manual Refresh'}
        </Button>
      </div>

      {/* Summary Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <ReportCard
          title="Total Authorizations"
          value={summary?.totalCount || 0}
          icon="📊"
          bgColor="bg-blue-50 dark:bg-blue-900/20"
          textColor="text-blue-700 dark:text-blue-300"
        />

        <ReportCard
          title="Approved"
          value={summary?.approvedCount || 0}
          icon="✓"
          bgColor="bg-green-50 dark:bg-green-900/20"
          textColor="text-green-700 dark:text-green-300"
        />

        <ReportCard
          title="Declined"
          value={summary?.declinedCount || 0}
          icon="✗"
          bgColor="bg-red-50 dark:bg-red-900/20"
          textColor="text-red-700 dark:text-red-300"
        />

        <ReportCard
          title="Pending"
          value={summary?.pendingCount || 0}
          icon="⏳"
          bgColor="bg-yellow-50 dark:bg-yellow-900/20"
          textColor="text-yellow-700 dark:text-yellow-300"
        />

        <ReportCard
          title="Total Amount"
          value={formatAmount(summary?.totalAmount || 0)}
          icon="💰"
          bgColor="bg-purple-50 dark:bg-purple-900/20"
          textColor="text-purple-700 dark:text-purple-300"
        />

        <ReportCard
          title="Average Amount"
          value={formatAmount(summary?.averageAmount || 0)}
          icon="📈"
          bgColor="bg-indigo-50 dark:bg-indigo-900/20"
          textColor="text-indigo-700 dark:text-indigo-300"
        />
      </div>

      {/* Statistics Summary Card */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-semibold">Summary Statistics</h3>
        </CardHeader>
        <CardBody>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-3">
              <div className="flex justify-between items-center pb-2 border-b border-neutral-200 dark:border-neutral-700">
                <span className="text-neutral-600 dark:text-neutral-400">Approval Rate</span>
                <span className="font-semibold text-neutral-900 dark:text-neutral-100">
                  {summary && summary.totalCount > 0
                    ? `${((summary.approvedCount / summary.totalCount) * 100).toFixed(1)}%`
                    : '0%'}
                </span>
              </div>

              <div className="flex justify-between items-center pb-2 border-b border-neutral-200 dark:border-neutral-700">
                <span className="text-neutral-600 dark:text-neutral-400">Decline Rate</span>
                <span className="font-semibold text-neutral-900 dark:text-neutral-100">
                  {summary && summary.totalCount > 0
                    ? `${((summary.declinedCount / summary.totalCount) * 100).toFixed(1)}%`
                    : '0%'}
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-neutral-600 dark:text-neutral-400">Pending Rate</span>
                <span className="font-semibold text-neutral-900 dark:text-neutral-100">
                  {summary && summary.totalCount > 0
                    ? `${((summary.pendingCount / summary.totalCount) * 100).toFixed(1)}%`
                    : '0%'}
                </span>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between items-center pb-2 border-b border-neutral-200 dark:border-neutral-700">
                <span className="text-neutral-600 dark:text-neutral-400">Authorized Amount</span>
                <span className="font-semibold text-neutral-900 dark:text-neutral-100">
                  {formatAmount(summary?.totalAmount || 0)}
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-neutral-600 dark:text-neutral-400">Average per Authorization</span>
                <span className="font-semibold text-neutral-900 dark:text-neutral-100">
                  {formatAmount(summary?.averageAmount || 0)}
                </span>
              </div>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Auto-refresh indicator */}
      <p className="text-xs text-neutral-500 dark:text-neutral-400 text-center">
        Auto-refreshes every 30 seconds
      </p>
    </div>
  )
}
