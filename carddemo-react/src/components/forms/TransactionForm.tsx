import React from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '@/components/common/Button'
import { Input } from '@/components/common/Input'
import { transactionService } from '@/services/transactionService'
import { useTransactionStore } from '@/store/transactionStore'
import { useUiStore } from '@/store/uiStore'

interface TransactionFormData {
  accountId: string
  cardNumber: string
  amount: number
  type: 'PURCHASE' | 'CASH_ADVANCE' | 'PAYMENT' | 'FEE'
  merchantId: string
  merchantName: string
}

export const TransactionForm: React.FC = () => {
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<TransactionFormData>()
  const postTransaction = useTransactionStore((s) => s.postTransaction)
  const addToast = useUiStore((s) => s.addToast)

  const onSubmit = async (data: TransactionFormData) => {
    try {
      await postTransaction(data)
      addToast({ message: 'Transaction posted successfully', type: 'success' })
      reset()
    } catch {
      addToast({ message: 'Failed to post transaction', type: 'error' })
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-md">
      <Input label="Account ID" {...register('accountId', { required: 'Account ID is required' })} error={errors.accountId?.message} />
      <Input label="Card Number" {...register('cardNumber', { required: 'Card number is required' })} error={errors.cardNumber?.message} />
      <Input
        label="Amount"
        type="number"
        step="0.01"
        {...register('amount', { required: 'Amount is required', min: { value: 0.01, message: 'Must be positive' } })}
        error={errors.amount?.message}
      />
      <div>
        <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1.5">Type</label>
        <select
          {...register('type', { required: 'Type is required' })}
          className="w-full px-3 py-2 border border-neutral-300 dark:border-neutral-600 rounded-md dark:bg-neutral-800 dark:text-white"
        >
          <option value="PURCHASE">Purchase</option>
          <option value="CASH_ADVANCE">Cash Advance</option>
          <option value="PAYMENT">Payment</option>
          <option value="FEE">Fee</option>
        </select>
      </div>
      <Input label="Merchant ID" {...register('merchantId', { required: 'Merchant ID is required' })} error={errors.merchantId?.message} />
      <Input label="Merchant Name" {...register('merchantName', { required: 'Merchant name is required' })} error={errors.merchantName?.message} />
      <Button type="submit" loading={isSubmitting}>
        Post Transaction
      </Button>
    </form>
  )
}
