import { useMutation } from '@tanstack/react-query'

import { login, type LoginPayload } from '@/features/auth/api/auth-api'

export function useLoginMutation() {
  return useMutation({
    mutationFn: (payload: LoginPayload) => login(payload),
  })
}
