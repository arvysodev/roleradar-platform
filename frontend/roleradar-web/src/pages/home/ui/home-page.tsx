import { AppHeader } from '@/widgets/app-header/ui/app-header'
import { VacancyFeed } from '@/widgets/vacancy-feed/ui/vacancy-feed'
import { VacancySearchPanel } from '@/widgets/vacancy-search-panel/ui/vacancy-search-panel'

export function HomePage() {
  return (
    <main className="relative min-h-svh overflow-hidden bg-background text-foreground">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(23,37,84,0.09),_transparent_28%),radial-gradient(circle_at_top_right,_rgba(14,165,233,0.10),_transparent_22%),radial-gradient(circle_at_bottom,_rgba(245,158,11,0.08),_transparent_28%)]" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(to_right,rgba(15,23,42,0.04)_1px,transparent_1px),linear-gradient(to_bottom,rgba(15,23,42,0.04)_1px,transparent_1px)] bg-[size:68px_68px] [mask-image:radial-gradient(circle_at_center,black,transparent_84%)]" />

      <div className="relative mx-auto flex min-h-svh w-full max-w-7xl flex-col px-5 pb-12 sm:px-8 lg:px-10">
        <AppHeader />

        <div className="flex flex-1 flex-col gap-8 pb-8 pt-6 lg:gap-10">
          <VacancySearchPanel />
          <VacancyFeed />
        </div>
      </div>
    </main>
  )
}
