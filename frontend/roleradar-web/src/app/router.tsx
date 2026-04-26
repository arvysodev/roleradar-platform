import { createBrowserRouter } from 'react-router-dom'

import { HomePage } from '@/pages/home/ui/home-page'
import { LoginPage } from '@/pages/login/ui/login-page'

export const appRouter = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
])
