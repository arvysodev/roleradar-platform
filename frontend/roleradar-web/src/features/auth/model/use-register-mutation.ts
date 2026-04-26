import { useMutation } from '@tanstack/react-query'

import { register, type RegisterPayload } from '@/features/auth/api/auth-api'

export function useRegisterMutation() {
  return useMutation({
    mutationFn: (payload: RegisterPayload) => register(payload),
  })
}
