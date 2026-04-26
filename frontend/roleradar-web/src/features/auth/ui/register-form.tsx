import { AlertCircle, LoaderCircle, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useRegisterMutation } from '@/features/auth/model/use-register-mutation'
import { ApiError } from '@/shared/api/http-client'

const initialForm = {
  email: '',
  username: '',
  password: '',
}

export function RegisterForm() {
  const registerMutation = useRegisterMutation()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)

  const errorMessage =
    registerMutation.error instanceof ApiError
      ? registerMutation.error.detail
      : registerMutation.error
        ? 'Registration failed. Please try again.'
        : null

  function updateField(field: keyof typeof initialForm, value: string) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const registeredUser = await registerMutation.mutateAsync(form)
    const searchParams = new URLSearchParams({
      email: registeredUser.email,
    })

    navigate(`/verify-email/pending?${searchParams.toString()}`, {
      replace: true,
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
          placeholder="test@example.com"
          autoComplete="email"
          required
          className="h-11 rounded-xl"
        />
      </div>

      <div className="space-y-2">
        <label htmlFor="username" className="text-sm font-medium">
          Username
        </label>
        <Input
          id="username"
          type="text"
          value={form.username}
          onChange={(event) => updateField('username', event.target.value)}
          placeholder="test"
          autoComplete="username"
          required
          minLength={3}
          className="h-11 rounded-xl"
        />
        <p className="text-xs leading-5 text-muted-foreground">
          Letters, digits, dots, underscores, and hyphens are supported.
        </p>
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
          placeholder="Create a strong password"
          autoComplete="new-password"
          required
          minLength={8}
          className="h-11 rounded-xl"
        />
      </div>

      {errorMessage ? (
        <Alert variant="destructive" className="rounded-2xl">
          <AlertCircle className="size-4" />
          <AlertTitle>Registration failed</AlertTitle>
          <AlertDescription>{errorMessage}</AlertDescription>
        </Alert>
      ) : null}

      <Button type="submit" size="lg" className="h-11 w-full rounded-xl" disabled={registerMutation.isPending}>
        {registerMutation.isPending ? (
          <>
            <LoaderCircle className="size-4 animate-spin" />
            Creating account...
          </>
        ) : (
          <>
            <UserPlus className="size-4" />
            Create account
          </>
        )}
      </Button>
    </form>
  )
}
