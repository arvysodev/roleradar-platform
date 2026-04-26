import { ListFilter, MapPin, Search, SlidersHorizontal } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

const quickFilters = ['Remote only', 'Backend', 'Spring', 'Kafka', 'New today']

export function VacancySearchPanel() {
  return (
    <section className="rounded-[2rem] border border-border/70 bg-white/82 p-5 shadow-[0_20px_80px_-52px_rgba(15,23,42,0.38)] backdrop-blur sm:p-6">
      <div className="flex flex-col gap-6">
        <div className="space-y-3">
          <Badge
            variant="outline"
            className="border-primary/20 bg-primary/5 px-3 py-1 text-[0.72rem] font-semibold tracking-[0.18em] text-primary uppercase"
          >
            Vacancies
          </Badge>
          <div className="space-y-2">
            <h1 className="max-w-3xl text-4xl leading-tight font-semibold tracking-tight text-balance sm:text-5xl">
              Track strong backend roles without losing time in noisy job boards.
            </h1>
            <p className="max-w-2xl text-base leading-7 text-muted-foreground">
              The first frontend pass should help you search, filter, and scan roles quickly. We are
              starting with a simple shell that will later connect to the Gateway-backed vacancy API.
            </p>
          </div>
        </div>

        <div className="grid gap-3 lg:grid-cols-[minmax(0,1.35fr)_minmax(0,0.8fr)_auto]">
          <div className="relative">
            <Search className="pointer-events-none absolute top-1/2 left-4 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value="backend engineer spring kafka"
              readOnly
              className="h-12 rounded-2xl border-transparent bg-muted/70 pl-11 shadow-none"
              aria-label="Vacancy keyword search preview"
            />
          </div>

          <div className="relative">
            <MapPin className="pointer-events-none absolute top-1/2 left-4 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value="Remote / Europe-friendly"
              readOnly
              className="h-12 rounded-2xl border-transparent bg-muted/70 pl-11 shadow-none"
              aria-label="Location filter preview"
            />
          </div>

          <Button size="lg" className="h-12 rounded-2xl px-5">
            <ListFilter className="size-4" />
            Search
          </Button>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <div className="mr-1 inline-flex items-center gap-2 text-sm text-muted-foreground">
            <SlidersHorizontal className="size-4" />
            Quick filters
          </div>
          {quickFilters.map((filter) => (
            <Button key={filter} variant="outline" size="sm" className="rounded-full">
              {filter}
            </Button>
          ))}
        </div>
      </div>
    </section>
  )
}
