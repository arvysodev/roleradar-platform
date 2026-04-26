import { httpRequest } from '@/shared/api/http-client'

type CsrfResponse = {
  token: string
}

let csrfTokenCache: string | null = null

export async function ensureCsrfToken() {
  if (csrfTokenCache) {
    return csrfTokenCache
  }

  const response = await httpRequest<CsrfResponse>('/csrf')
  csrfTokenCache = response.token

  return csrfTokenCache
}

export function clearCsrfTokenCache() {
  csrfTokenCache = null
}
