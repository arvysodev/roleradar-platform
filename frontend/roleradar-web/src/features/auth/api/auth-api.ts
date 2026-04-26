import { clearCsrfTokenCache, ensureCsrfToken } from '@/shared/api/csrf'
import { httpRequest } from '@/shared/api/http-client'

export type LoginPayload = {
  email: string
  password: string
}

export type RegisterPayload = {
  email: string
  username: string
  password: string
}

export type LoginResponse = {
  tokenType: string
  expiresIn: number
}

export type RegisteredUser = {
  id: string
  email: string
  username: string
  role: string
  status: string
  createdAt: string
  updatedAt: string
  emailVerified: boolean
}

export type CurrentUser = {
  id: string
  email: string
  username: string
  roles: string[]
}

function csrfHeaders(token: string) {
  return {
    'X-XSRF-TOKEN': token,
  }
}

export async function login(payload: LoginPayload) {
  const csrfToken = await ensureCsrfToken()

  return httpRequest<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: payload,
    headers: csrfHeaders(csrfToken),
  })
}

export async function register(payload: RegisterPayload) {
  const csrfToken = await ensureCsrfToken()

  return httpRequest<RegisteredUser>('/api/v1/auth/register', {
    method: 'POST',
    body: payload,
    headers: csrfHeaders(csrfToken),
  })
}

export async function logout() {
  const csrfToken = await ensureCsrfToken()

  await httpRequest<void>('/api/v1/auth/logout', {
    method: 'POST',
    headers: csrfHeaders(csrfToken),
  })

  clearCsrfTokenCache()
}

export async function getCurrentUser() {
  return httpRequest<CurrentUser>('/api/v1/auth/me')
}
