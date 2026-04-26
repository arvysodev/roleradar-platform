import { ChevronLeft, Radar } from 'lucide-react'
import { Link } from 'react-router-dom'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { LoginForm } from '@/features/auth/ui/login-form'

export function LoginPage() {
  return (
    <main className="relative min-h-svh overflow-hidden bg-background text-foreground">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(14,165,233,0.10),_transparent_26%),radial-gradient(circle_at_bottom,_rgba(245,158,11,0.08),_transparent_28%)]" />

      <div className="relative mx-auto flex min-h-svh w-full max-w-6xl flex-col px-5 py-6 sm:px-8 lg:px-10">
        <div className="mb-8 flex items-center justify-between">
          <Link
            to="/"
            className="inline-flex items-center gap-2 rounded-full border border-border/70 bg-background/80 px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            <ChevronLeft className="size-4" />
            Back to vacancies
          </Link>

          <div className="inline-flex items-center gap-2 rounded-full border border-border/70 bg-background/80 px-4 py-2 text-sm">
            <Radar className="size-4" />
            RoleRadar
          </div>
        </div>

        <div className="grid flex-1 items-center gap-8 lg:grid-cols-[1.05fr_0.95fr]">
          <section className="max-w-xl space-y-5">
            <p className="text-[0.72rem] font-semibold tracking-[0.22em] text-primary uppercase">
              Authentication
            </p>
            <h1 className="text-4xl leading-tight font-semibold tracking-tight text-balance sm:text-5xl">
              Sign in to save roles, track alerts, and personalize the vacancy feed.
            </h1>
            <p className="text-base leading-7 text-muted-foreground sm:text-lg">
              This is the first real auth screen. It talks to the Gateway, uses cookie-based auth, and
              follows the backend CSRF flow instead of inventing a separate frontend auth model.
            </p>
          </section>

          <Card className="rounded-[2rem] border border-border/70 bg-white/85 shadow-[0_24px_90px_-60px_rgba(15,23,42,0.48)] backdrop-blur">
            <CardHeader className="space-y-2">
              <CardTitle className="text-2xl">Welcome back</CardTitle>
              <CardDescription>Use your RoleRadar account credentials to continue.</CardDescription>
            </CardHeader>
            <CardContent>
              <LoginForm />
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  )
}
