import React, { useState } from 'react'
import { Button } from '@/components/common/Button'
import { Input } from '@/components/common/Input'
import { Card, CardBody, CardHeader, CardFooter } from '@/components/common/Card'
import { useAuthorizationStore } from '@/store/authorizationStore'
import { useUiStore } from '@/store/uiStore'
import { Authorization } from '@/types'

interface AuthorizationDetailProps {
  authorization: Authorization
  onClose: () => void
  onSuccess?: () => void
}

export const AuthorizationDetail: React.FC<AuthorizationDetailProps> = ({ authorization, onClose, onSuccess }) => {
  const [isApproving, setIsApproving] = useState(false)
  const [isDeclining, setIsDeclining] = useState(false)
  const [declineReason, setDeclineReason] = useState('')
  const [showDeclineConfirm, setShowDeclineConfirm] = useState(false)
  const [showApproveConfirm, setShowApproveConfirm] = useState(false)

  const approveAuthorization = useAuthorizationStore((s) => s.approveAuthorization)
  const declineAuthorization = useAuthorizationStore((s) => s.declineAuthorization)
  const loadAuthorizations = useAuthorizationStore((s) => s.loadAuthorizations)
  const addToast = useUiStore((s) => s.addToast)

  const handleApprove = async () => {
    setIsApproving(true)
    try {
      await approveAuthorization(authorization.id)
      addToast({
        message: 'Authorization approved successfully',
        type: 'success',
      })
      setShowApproveConfirm(false)
      onSuccess?.()
      await loadAuthorizations()
      onClose()
    } catch {
      addToast({
        message: 'Failed to approve authorization',
        type: 'error',
      })
    } finally {
      setIsApproving(false)
    }
  }

  const handleDecline = async () => {
    setIsDeclining(true)
    try {
      await declineAuthorization(authorization.id, declineReason)
      addToast({
        message: 'Authorization declined successfully',
        type: 'success',
      })
      setShowDeclineConfirm(false)
      setDeclineReason('')
      onSuccess?.()
      await loadAuthorizations()
      onClose()
    } catch {
      addToast({
        message: 'Failed to decline authorization',
        type: 'error',
      })
    } finally {
      setIsDeclining(false)
    }
  }

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return '-'
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount)
  }

  const getStatusColor = (status: string) => {
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

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <Card className="w-full max-w-2xl max-h-[90vh] overflow-auto">
        <CardHeader>
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold">Authorization Details</h2>
            <button
              onClick={onClose}
              className="text-neutral-400 hover:text-neutral-600 dark:hover:text-neutral-300 text-2xl"
            >
              ×
            </button>
          </div>
        </CardHeader>

        <CardBody className="space-y-6">
          {/* Status */}
          <div>
            <label htmlFor="status-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
              Status
            </label>
            <div
              id="status-display"
              className={`inline-block px-4 py-2 rounded-lg font-semibold ${getStatusColor(authorization.status)}`}
            >
              {authorization.status}
            </div>
          </div>

          {/* Basic Info Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="id-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                ID
              </label>
              <p
                id="id-display"
                className="font-mono text-sm text-neutral-900 dark:text-neutral-100 bg-neutral-50 dark:bg-neutral-700 p-2 rounded"
              >
                {authorization.id}
              </p>
            </div>

            <div>
              <label htmlFor="card-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Card Number
              </label>
              <p
                id="card-display"
                className="font-mono text-sm text-neutral-900 dark:text-neutral-100 bg-neutral-50 dark:bg-neutral-700 p-2 rounded"
              >
                {authorization.cardNumber}
              </p>
            </div>

            <div>
              <label htmlFor="amount-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Amount
              </label>
              <p id="amount-display" className="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                {formatAmount(authorization.amount)}
              </p>
            </div>

            {authorization.approvedAmount && (
              <div>
                <label htmlFor="approved-amount-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                  Approved Amount
                </label>
                <p id="approved-amount-display" className="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                  {formatAmount(authorization.approvedAmount)}
                </p>
              </div>
            )}

            <div>
              <label htmlFor="merchant-id-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Merchant ID
              </label>
              <p id="merchant-id-display" className="font-mono text-sm text-neutral-900 dark:text-neutral-100">
                {authorization.merchantId}
              </p>
            </div>

            <div>
              <label htmlFor="merchant-name-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Merchant Name
              </label>
              <p id="merchant-name-display" className="text-sm text-neutral-900 dark:text-neutral-100">
                {authorization.merchantName}
              </p>
            </div>
          </div>

          {/* Dates */}
          <div className="border-t border-neutral-200 dark:border-neutral-700 pt-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="request-date-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                  Request Date
                </label>
                <p id="request-date-display" className="text-sm text-neutral-900 dark:text-neutral-100">
                  {formatDate(authorization.requestDate)}
                </p>
              </div>

              {authorization.responseDate && (
                <div>
                  <label htmlFor="response-date-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                    Response Date
                  </label>
                  <p id="response-date-display" className="text-sm text-neutral-900 dark:text-neutral-100">
                    {formatDate(authorization.responseDate)}
                  </p>
                </div>
              )}

              <div>
                <label htmlFor="expiry-date-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                  Expiry Date
                </label>
                <p id="expiry-date-display" className="text-sm text-neutral-900 dark:text-neutral-100">
                  {formatDate(authorization.expiryDate)}
                </p>
              </div>
            </div>
          </div>

          {/* Decline Reason */}
          {authorization.status === 'DECLINED' && authorization.declineReasonCode && (
            <div className="border-t border-neutral-200 dark:border-neutral-700 pt-4">
              <label htmlFor="decline-reason-display" className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Decline Reason
              </label>
              <p id="decline-reason-display" className="text-sm text-red-700 dark:text-red-300 bg-red-50 dark:bg-red-900/20 p-3 rounded">
                {authorization.declineReasonCode}
              </p>
            </div>
          )}

          {/* Action Confirmation Dialogs */}
          {showApproveConfirm && (
            <div className="border-t border-neutral-200 dark:border-neutral-700 pt-4 bg-blue-50 dark:bg-blue-900/20 p-4 rounded">
              <p className="text-sm text-neutral-700 dark:text-neutral-300 mb-3">
                Are you sure you want to approve this authorization for {formatAmount(authorization.amount)}?
              </p>
              <div className="flex gap-2">
                <Button size="sm" onClick={handleApprove} loading={isApproving}>
                  Confirm Approval
                </Button>
                <Button size="sm" variant="secondary" onClick={() => setShowApproveConfirm(false)}>
                  Cancel
                </Button>
              </div>
            </div>
          )}

          {showDeclineConfirm && (
            <div className="border-t border-neutral-200 dark:border-neutral-700 pt-4 bg-red-50 dark:bg-red-900/20 p-4 rounded space-y-3">
              <p className="text-sm text-neutral-700 dark:text-neutral-300">
                Are you sure you want to decline this authorization?
              </p>
              <Input
                label="Decline Reason"
                placeholder="Enter reason for declining..."
                value={declineReason}
                onChange={(e) => setDeclineReason(e.target.value)}
              />
              <div className="flex gap-2">
                <Button
                  size="sm"
                  onClick={handleDecline}
                  loading={isDeclining}
                  variant="danger"
                >
                  Confirm Decline
                </Button>
                <Button size="sm" variant="secondary" onClick={() => setShowDeclineConfirm(false)}>
                  Cancel
                </Button>
              </div>
            </div>
          )}
        </CardBody>

        <CardFooter>
          <div className="flex gap-2">
            {authorization.status === 'DECLINED' && (
              <Button onClick={() => setShowApproveConfirm(true)} variant="success">
                Approve
              </Button>
            )}

            {authorization.status === 'APPROVED' && (
              <Button onClick={() => setShowDeclineConfirm(true)} variant="danger">
                Decline
              </Button>
            )}

            {authorization.status === 'PENDING' && (
              <>
                <Button onClick={() => setShowApproveConfirm(true)} variant="success">
                  Approve
                </Button>
                <Button onClick={() => setShowDeclineConfirm(true)} variant="danger">
                  Decline
                </Button>
              </>
            )}

            <Button onClick={onClose} variant="secondary">
              Close
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  )
}
