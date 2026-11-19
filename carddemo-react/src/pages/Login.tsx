import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/common/Button'
import { Input } from '@/components/common/Input'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { useAuth } from '@/hooks/useAuth'
import { CreditCard } from 'lucide-react'

export const Login: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      await login(email, password)
      navigate('/', { replace: true })
    } catch (err) {
      setError('Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-600 to-primary-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex justify-center mb-8">
          <div className="bg-white rounded-full p-4">
            <CreditCard className="text-primary-600" size={32} />
          </div>
        </div>

        {/* Login Card */}
        <Card className="shadow-xl">
          <CardHeader>
            <h1 className="text-2xl font-bold text-center text-neutral-900 dark:text-white">CardDemo</h1>
            <p className="text-sm text-center text-neutral-600 dark:text-neutral-400 mt-2">Sign in to your account</p>
          </CardHeader>
          <CardBody>
            {error && <div className="mb-4 p-3 bg-danger-50 dark:bg-danger-900 border border-danger-200 dark:border-danger-700 text-danger-700 dark:text-danger-200 rounded-md text-sm">{error}</div>}

            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                label="Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="demo@example.com"
              />
              <Input
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="••••••••"
              />
              <Button type="submit" loading={loading} className="w-full">
                Sign In
              </Button>
            </form>

            <p className="text-xs text-center text-neutral-600 dark:text-neutral-400 mt-4">
              Demo account: Use any email and password (mock authentication)
            </p>
          </CardBody>
        </Card>
      </div>
    </div>
  )
}
