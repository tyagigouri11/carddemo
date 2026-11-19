import React, { useState, useEffect } from 'react'
import { Button } from '@/components/common/Button'
import { Input } from '@/components/common/Input'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { useAuthorizationStore } from '@/store/authorizationStore'
import { Authorization, AuthorizationStatus } from '@/types'

interface AuthorizationListProps {
  onSelectAuthorization: (auth: Authorization) => void
}

export const AuthorizationList: React.FC<AuthorizationListProps> = ({ onSelectAuthorization }) => {
  const [statusFilter, setStatusFilter] = useState<AuthorizationStatus | 'ALL'>('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const itemsPerPage = 10

  const { authorizations, loading, loadAuthorizations } = useAuthorizationStore()

  useEffect(() => {
    loadAuthorizations()
  }, [loadAuthorizations])

  const filteredAuthorizations = authorizations.filter((auth) => {
    const statusMatch = statusFilter === 'ALL' || auth.status === statusFilter
    const searchMatch =
      auth.cardNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      auth.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      auth.merchantName.toLowerCase().includes(searchTerm.toLowerCase())
    return statusMatch && searchMatch
  })

  const totalPages = Math.ceil(filteredAuthorizations.length / itemsPerPage)
  const startIdx = (currentPage - 1) * itemsPerPage
  const endIdx = startIdx + itemsPerPage
  const paginatedAuthorizations = filteredAuthorizations.slice(startIdx, endIdx)

  const getStatusBadgeColor = (status: AuthorizationStatus) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
      case 'DECLINED':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
    }
  }

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return '-'
    return new Date(dateString).toLocaleDateString()
  }

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount)
  }

  return (
    <div className="space-y-4">
      {/* Controls */}
      <Card>
        <CardBody className="space-y-4">
          <div className="space-y-3">
            <label htmlFor="status-filter" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Filter by Status
            </label>
            <div className="flex flex-wrap gap-2">
              {(['ALL', 'APPROVED', 'DECLINED', 'PENDING'] as const).map((status) => (
                <Button
                  key={status}
                  variant={statusFilter === status ? 'primary' : 'secondary'}
                  size="sm"
                  onClick={() => {
                    setStatusFilter(status)
                    setCurrentPage(1)
                  }}
                >
                  {status}
                </Button>
              ))}
            </div>
          </div>

          <div className="space-y-3">
            <label htmlFor="search-input" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Search by Card/ID/Merchant
            </label>
            <Input
              id="search-input"
              placeholder="Search..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value)
                setCurrentPage(1)
              }}
            />
          </div>

          <Button onClick={() => loadAuthorizations()} variant="secondary">
            Refresh
          </Button>
        </CardBody>
      </Card>

      {/* Table */}
      <Card>
        <CardHeader>
          <h3 className="text-lg font-semibold">Authorizations</h3>
          <p className="text-sm text-neutral-600 dark:text-neutral-400 mt-1">
            Showing {paginatedAuthorizations.length} of {filteredAuthorizations.length} results
          </p>
        </CardHeader>
        <CardBody>
          {loading && (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500"></div>
            </div>
          )}

          {!loading && paginatedAuthorizations.length === 0 && (
            <p className="text-center text-neutral-600 dark:text-neutral-400 py-8">No authorizations found</p>
          )}

          {!loading && paginatedAuthorizations.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-neutral-200 dark:border-neutral-700">
                    <th className="text-left py-3 px-4 font-semibold text-neutral-700 dark:text-neutral-300">ID</th>
                    <th className="text-left py-3 px-4 font-semibold text-neutral-700 dark:text-neutral-300">Card Number</th>
                    <th className="text-right py-3 px-4 font-semibold text-neutral-700 dark:text-neutral-300">Amount</th>
                    <th className="text-left py-3 px-4 font-semibold text-neutral-700 dark:text-neutral-300">Merchant</th>
                    <th className="text-center py-3 px-4 font-semibold text-neutral-700 dark:text-neutral-300">Status</th>
                    <th className="text-left py-3 px-4 font-semibold text-neutral-700 dark:text-neutral-300">Date</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedAuthorizations.map((auth) => (
                    <tr
                      key={auth.id}
                      className="border-b border-neutral-200 dark:border-neutral-700 hover:bg-neutral-50 dark:hover:bg-neutral-700 cursor-pointer transition-colors"
                      onClick={() => onSelectAuthorization(auth)}
                    >
                      <td className="py-3 px-4 font-mono text-sm">{auth.id.substring(0, 8)}</td>
                      <td className="py-3 px-4 font-mono">{auth.cardNumber}</td>
                      <td className="py-3 px-4 text-right">{formatAmount(auth.amount)}</td>
                      <td className="py-3 px-4 truncate">{auth.merchantName}</td>
                      <td className="py-3 px-4 text-center">
                        <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStatusBadgeColor(auth.status)}`}>
                          {auth.status}
                        </span>
                      </td>
                      <td className="py-3 px-4">{formatDate(auth.requestDate)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          {!loading && totalPages > 1 && (
            <div className="flex items-center justify-between mt-6 pt-4 border-t border-neutral-200 dark:border-neutral-700">
              <p className="text-sm text-neutral-600 dark:text-neutral-400">
                Page {currentPage} of {totalPages}
              </p>
              <div className="flex gap-2">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                  disabled={currentPage === 1}
                >
                  Previous
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                  disabled={currentPage === totalPages}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  )
}
