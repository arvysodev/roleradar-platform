import { BellDot, Radar, UserRound } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'

export function AppHeader() {
  return (
    <header className="flex flex-col gap-4 border-b border-border/70 py-6 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-center gap-3">
        <div className="flex size-11 items-center justify-center rounded-[1.3rem] bg-slate-950 text-slate-50 shadow-[0_16px_44px_-24px_rgba(15,23,42,0.7)]">
          <Radar className="size-5" />
        </div>

        <div>
          <p className="text-[0.72rem] font-semibold tracking-[0.22em] text-muted-foreground uppercase">
            RoleRadar
          </p>
          <p className="text-lg font-semibold tracking-tight">Find signal in the remote job noise.</p>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <Badge variant="outline" className="rounded-full px-3 py-1">
          Backend via Gateway
        </Badge>
        <Button variant="outline" size="sm" className="rounded-full">
          <BellDot className="size-4" />
          Alerts
        </Button>
        <Button size="sm" className="rounded-full">
          <UserRound className="size-4" />
          Sign in
        </Button>
      </div>
    </header>
  )
}
