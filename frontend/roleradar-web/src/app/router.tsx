import { createBrowserRouter } from 'react-router-dom'

import { HomePage } from '@/pages/home/ui/home-page'

export const appRouter = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
])
