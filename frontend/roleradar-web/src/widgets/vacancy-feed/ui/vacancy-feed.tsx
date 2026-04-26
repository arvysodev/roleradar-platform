import { ArrowUpRight, Clock3, MapPinned, Sparkles } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

const vacancies = [
  {
    id: '1',
    title: 'Senior Backend Engineer',
    company: 'Northstar Cloud',
    location: 'Remote',
    source: 'Remotive',
    summary:
      'Spring Boot, Kafka, PostgreSQL, and event-driven systems for a growing distributed platform.',
    tags: ['Spring', 'Kafka', 'PostgreSQL'],
    posted: '2h ago',
  },
  {
    id: '2',
    title: 'Platform Java Engineer',
    company: 'Signal Stack',
    location: 'Remote / EMEA',
    source: 'Arbeitnow',
    summary:
      'Focus on resilient APIs, async processing, and integration-heavy services with strong ownership.',
    tags: ['Java', 'APIs', 'Resilience'],
    posted: '5h ago',
  },
  {
    id: '3',
    title: 'Backend Engineer, Integrations',
    company: 'Relay Forge',
    location: 'Remote / EU',
    source: 'Adzuna',
    summary:
      'Work on provider ingestion pipelines, data normalization, and search-friendly vacancy flows.',
    tags: ['Integrations', 'Data Pipelines', 'Search'],
    posted: 'Today',
  },
]

export function VacancyFeed() {
  return (
    <section className="grid gap-6 xl:grid-cols-[minmax(0,1.15fr)_360px]">
      <div className="space-y-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-medium text-muted-foreground">Latest matches</p>
            <h2 className="text-2xl font-semibold tracking-tight">Vacancy list shell</h2>
          </div>
          <p className="text-sm text-muted-foreground">
            Mock data for now. Next step is replacing this with Gateway-backed queries.
          </p>
        </div>

        <div className="grid gap-4">
          {vacancies.map((vacancy) => (
            <Card
              key={vacancy.id}
              className="rounded-[1.75rem] border border-border/70 bg-white/82 shadow-[0_18px_72px_-60px_rgba(15,23,42,0.48)] backdrop-blur"
            >
              <CardHeader className="gap-4">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div className="space-y-2">
                    <div className="flex flex-wrap items-center gap-2">
                      <Badge variant="outline" className="rounded-full">
                        {vacancy.source}
                      </Badge>
                      <Badge className="rounded-full bg-emerald-500/12 text-emerald-700 hover:bg-emerald-500/18">
                        <Sparkles className="size-3.5" />
                        Strong match
                      </Badge>
                    </div>
                    <div>
                      <CardTitle className="text-xl">{vacancy.title}</CardTitle>
                      <CardDescription className="mt-1 text-sm">
                        {vacancy.company}
                      </CardDescription>
                    </div>
                  </div>

                  <Button variant="outline" size="sm" className="rounded-full self-start">
                    View details
                    <ArrowUpRight className="size-4" />
                  </Button>
                </div>
              </CardHeader>

              <CardContent className="space-y-4">
                <p className="text-sm leading-6 text-muted-foreground">{vacancy.summary}</p>

                <div className="flex flex-wrap gap-2">
                  {vacancy.tags.map((tag) => (
                    <Badge key={tag} variant="secondary" className="rounded-full">
                      {tag}
                    </Badge>
                  ))}
                </div>
              </CardContent>

              <CardFooter className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
                  <div className="inline-flex items-center gap-2">
                    <MapPinned className="size-4" />
                    {vacancy.location}
                  </div>
                  <div className="inline-flex items-center gap-2">
                    <Clock3 className="size-4" />
                    {vacancy.posted}
                  </div>
                </div>
                <Button className="rounded-full">Save role</Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      </div>

      <aside className="space-y-4">
        <Card className="rounded-[1.75rem] border border-slate-900/10 bg-slate-950 text-slate-50 shadow-[0_24px_90px_-58px_rgba(2,6,23,0.75)]">
          <CardHeader>
            <CardTitle className="text-xl">What comes next</CardTitle>
            <CardDescription className="text-slate-300">
              This is the first honest frontend commit: a vacancy product shell, not a project-marketing
              landing page.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 text-sm text-slate-300">
            <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
              Replace hardcoded vacancy cards with a TanStack Query list request to the Gateway.
            </div>
            <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
              Turn quick filters into actual query params and sync them with the URL.
            </div>
            <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
              Add vacancy detail route and auth screens once the list flow is stable.
            </div>
          </CardContent>
        </Card>
      </aside>
    </section>
  )
}
