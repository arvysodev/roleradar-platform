import { AlertCircle, LoaderCircle, LogIn } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { ApiError } from '@/shared/api/http-client'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useLoginMutation } from '@/features/auth/model/use-login-mutation'

const initialForm = {
  email: '',
  password: '',
}

export function LoginForm() {
  const navigate = useNavigate()
  const loginMutation = useLoginMutation()

  const [form, setForm] = useState(initialForm)

  const errorMessage =
    loginMutation.error instanceof ApiError
      ? loginMutation.error.detail
      : loginMutation.error
        ? 'Sign in failed. Please try again.'
        : null

  function updateField(field: keyof typeof initialForm, value: string) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    await loginMutation.mutateAsync(form, {
      onSuccess: () => {
        navigate('/')
      },
    })
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div className="space-y-2">
        <label htmlFor="email" className="text-sm font-medium">
          Email
        </label>
        <Input
          id="email"
          type="email"
          value={form.email}
          onChange={(event) => updateField('email', event.target.value)}
          placeholder="artem@example.com"
          autoComplete="email"
          required
          className="h-11 rounded-xl"
        />
      </div>

      <div className="space-y-2">
        <label htmlFor="password" className="text-sm font-medium">
          Password
        </label>
        <Input
          id="password"
          type="password"
          value={form.password}
          onChange={(event) => updateField('password', event.target.value)}
          placeholder="Enter your password"
          autoComplete="current-password"
          required
          minLength={8}
          className="h-11 rounded-xl"
        />
      </div>

      {errorMessage ? (
        <Alert variant="destructive" className="rounded-2xl">
          <AlertCircle className="size-4" />
          <AlertTitle>Sign in failed</AlertTitle>
          <AlertDescription>{errorMessage}</AlertDescription>
        </Alert>
      ) : null}

      <Button type="submit" size="lg" className="h-11 w-full rounded-xl" disabled={loginMutation.isPending}>
        {loginMutation.isPending ? (
          <>
            <LoaderCircle className="size-4 animate-spin" />
            Signing in...
          </>
        ) : (
          <>
            <LogIn className="size-4" />
            Sign in
          </>
        )}
      </Button>
    </form>
  )
}
