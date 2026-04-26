import { useMutation } from '@tanstack/react-query'

import { login, type LoginPayload } from '@/features/auth/api/auth-api'
import { authQueryKeys } from '@/features/auth/model/auth-query-keys'
import { queryClient } from '@/shared/lib/query-client'

export function useLoginMutation() {
  return useMutation({
    mutationFn: (payload: LoginPayload) => login(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: authQueryKeys.currentUser,
      })
    },
  })
}
