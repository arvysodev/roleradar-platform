import { ChevronLeft, Inbox, Radar } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function VerifyEmailPendingPage() {
  const [searchParams] = useSearchParams()
  const email = searchParams.get('email')

  return (
    <main className="relative min-h-svh overflow-hidden bg-background text-foreground">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(16,185,129,0.10),_transparent_24%),radial-gradient(circle_at_bottom_right,_rgba(14,165,233,0.10),_transparent_28%)]" />

      <div className="relative mx-auto flex min-h-svh w-full max-w-6xl flex-col px-5 py-6 sm:px-8 lg:px-10">
        <div className="mb-8 flex items-center justify-between">
          <Link
            to="/login"
            className="inline-flex items-center gap-2 rounded-full border border-border/70 bg-background/80 px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            <ChevronLeft className="size-4" />
            Back to login
          </Link>

          <div className="inline-flex items-center gap-2 rounded-full border border-border/70 bg-background/80 px-4 py-2 text-sm">
            <Radar className="size-4" />
            RoleRadar
          </div>
        </div>

        <div className="grid flex-1 items-center gap-8 lg:grid-cols-[1.05fr_0.95fr]">
          <section className="max-w-xl space-y-5">
            <p className="text-[0.72rem] font-semibold tracking-[0.22em] text-primary uppercase">
              Verification needed
            </p>
            <h1 className="text-4xl leading-tight font-semibold tracking-tight text-balance sm:text-5xl">
              Check your inbox to finish setting up the account.
            </h1>
            <p className="text-base leading-7 text-muted-foreground sm:text-lg">
              Registration succeeded, but sign-in stays locked until the email link is opened and confirmed.
            </p>
          </section>

          <Card className="rounded-[2rem] border border-border/70 bg-white/85 shadow-[0_24px_90px_-60px_rgba(15,23,42,0.48)] backdrop-blur">
            <CardHeader className="space-y-2">
              <CardTitle className="text-2xl">Email verification needed</CardTitle>
              <CardDescription>
                Open the verification email and click the link to activate the account.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-5">
              <div className="flex min-h-44 flex-col items-center justify-center gap-4 rounded-[1.75rem] border border-dashed border-border/70 bg-muted/35 px-6 py-10 text-center">
                <div className="rounded-full bg-emerald-500/10 p-3 text-emerald-700">
                  <Inbox className="size-6" />
                </div>
                <div className="space-y-2">
                  <p className="text-base font-medium">We sent a verification link.</p>
                  <p className="text-sm leading-6 text-muted-foreground">
                    {email ? (
                      <>
                        Delivery target:
                        {' '}
                        <span className="font-medium text-foreground">{email}</span>
                      </>
                    ) : (
                      'Open your mailbox and look for the RoleRadar verification email.'
                    )}
                  </p>
                </div>
              </div>

              <div className="rounded-2xl bg-muted/55 p-4 text-sm leading-6 text-muted-foreground">
                In local development, the email should appear in Mailpit. Open the verification link there to
                continue.
              </div>

              <div className="flex flex-col gap-3 sm:flex-row">
                <Button size="lg" className="h-11 flex-1 rounded-xl" render={<Link to="/login" />}>
                  Back to login
                </Button>
                <Button
                  variant="outline"
                  size="lg"
                  className="h-11 flex-1 rounded-xl"
                  render={<a href="http://localhost:8025" target="_blank" rel="noreferrer" />}
                >
                  Open Mailpit
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  )
}
