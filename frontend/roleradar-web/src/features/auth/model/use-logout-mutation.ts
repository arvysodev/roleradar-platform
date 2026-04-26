import { useMutation } from '@tanstack/react-query'

import { logout } from '@/features/auth/api/auth-api'

export function useLogoutMutation() {
  return useMutation({
    mutationFn: logout,
  })
}
