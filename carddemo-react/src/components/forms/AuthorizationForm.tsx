import React from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '@/components/common/Button'
import { Input } from '@/components/common/Input'
import { useAuthorizationStore } from '@/store/authorizationStore'
import { useUiStore } from '@/store/uiStore'

interface AuthorizationFormData {
  cardNumber: string
  merchantId: string
  merchantName: string
  amount: number
  accountId?: string
}

interface AuthorizationFormProps {
  onSuccess?: () => void
}

export const AuthorizationForm: React.FC<AuthorizationFormProps> = ({ onSuccess }) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<AuthorizationFormData>({
    mode: 'onBlur',
  })
  const createAuthorization = useAuthorizationStore((s) => s.createAuthorization)
  const loadAuthorizations = useAuthorizationStore((s) => s.loadAuthorizations)
  const addToast = useUiStore((s) => s.addToast)

  const onSubmit = async (data: AuthorizationFormData) => {
    try {
      await createAuthorization({
        cardNumber: data.cardNumber,
        merchantId: data.merchantId,
        merchantName: data.merchantName,
        amount: data.amount,
        accountId: data.accountId || '1', // Use default if not provided
      })
      addToast({
        message: 'Authorization created successfully',
        type: 'success',
      })
      reset()
      onSuccess?.()
      // Refresh list
      await loadAuthorizations()
    } catch {
      addToast({
        message: 'Failed to create authorization',
        type: 'error',
      })
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-md">
      <Input
        label="Card Number"
        placeholder="1234 5678 9012 3456"
        {...register('cardNumber', {
          required: 'Card number is required',
          pattern: {
            value: /^\d{4}\s?\d{4}\s?\d{4}\s?\d{4}$/,
            message: 'Invalid card number format',
          },
        })}
        error={errors.cardNumber?.message}
      />

      <Input
        label="Merchant ID"
        placeholder="MER123456"
        {...register('merchantId', {
          required: 'Merchant ID is required',
        })}
        error={errors.merchantId?.message}
      />

      <Input
        label="Merchant Name"
        placeholder="ACME Corp"
        {...register('merchantName', {
          required: 'Merchant name is required',
        })}
        error={errors.merchantName?.message}
      />

      <Input
        label="Amount"
        type="number"
        step="0.01"
        placeholder="100.00"
        {...register('amount', {
          required: 'Amount is required',
          min: {
            value: 0.01,
            message: 'Amount must be greater than 0',
          },
          validate: (value) => {
            if (isNaN(value)) return 'Amount must be a valid number'
            return true
          },
        })}
        error={errors.amount?.message}
      />

      <Input
        label="Account ID (Optional)"
        placeholder="1"
        {...register('accountId')}
      />

      <Button type="submit" loading={isSubmitting} className="w-full">
        Create Authorization
      </Button>
    </form>
  )
}
