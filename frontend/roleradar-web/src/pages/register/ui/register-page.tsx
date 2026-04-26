import { ChevronLeft, MailCheck, Radar } from 'lucide-react'
import { Link } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { RegisterForm } from '@/features/auth/ui/register-form'

export function RegisterPage() {
  return (
    <main className="relative min-h-svh overflow-hidden bg-background text-foreground">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.10),_transparent_24%),radial-gradient(circle_at_bottom,_rgba(14,165,233,0.10),_transparent_28%)]" />

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
              Create account
            </p>
            <h1 className="text-4xl leading-tight font-semibold tracking-tight text-balance sm:text-5xl">
              Create your account and start shaping a smarter vacancy feed.
            </h1>
            <p className="text-base leading-7 text-muted-foreground sm:text-lg">
              Registration stays aligned with the backend flow: create the account through the Gateway,
              then verify email before the first sign-in.
            </p>

            <div>
              <Button variant="outline" size="lg" className="rounded-full" render={<Link to="/login" />}>
                Back to login
              </Button>
            </div>

            <div className="rounded-[1.75rem] border border-border/70 bg-white/75 p-5 shadow-[0_18px_72px_-60px_rgba(15,23,42,0.45)] backdrop-blur">
              <div className="mb-3 inline-flex rounded-2xl bg-emerald-500/10 p-2.5 text-emerald-700">
                <MailCheck className="size-5" />
              </div>
              <h2 className="text-lg font-semibold">Email verification is part of the flow</h2>
              <p className="mt-2 text-sm leading-6 text-muted-foreground">
                After registration, the backend publishes a verification event and the notification service
                sends the email. In local development, you can open the link from Mailpit.
              </p>
            </div>
          </section>

          <Card className="rounded-[2rem] border border-border/70 bg-white/85 shadow-[0_24px_90px_-60px_rgba(15,23,42,0.48)] backdrop-blur">
            <CardHeader className="space-y-2">
              <CardTitle className="text-2xl">Create account</CardTitle>
              <CardDescription>Set up your RoleRadar account to continue.</CardDescription>
            </CardHeader>
            <CardContent>
              <RegisterForm />
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  )
}
