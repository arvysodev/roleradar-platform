import { useQuery } from '@tanstack/react-query'

import { type CurrentUser, getCurrentUser } from '@/features/auth/api/auth-api'
import { authQueryKeys } from '@/features/auth/model/auth-query-keys'
import { ApiError } from '@/shared/api/http-client'

async function loadCurrentUser(): Promise<CurrentUser | null> {
  try {
    return await getCurrentUser()
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
      return null
    }

    throw error
  }
}

export function useCurrentUserQuery() {
  return useQuery({
    queryKey: authQueryKeys.currentUser,
    queryFn: loadCurrentUser,
    retry: false,
  })
}
