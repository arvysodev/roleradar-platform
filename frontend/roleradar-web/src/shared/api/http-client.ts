import { env } from '@/shared/config/env'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

type RequestOptions = {
  method?: HttpMethod
  body?: unknown
  headers?: HeadersInit
  signal?: AbortSignal
}

export class ApiError extends Error {
  readonly status: number
  readonly detail: string
  readonly type?: string

  constructor(status: number, detail: string, type?: string) {
    super(detail)
    this.name = 'ApiError'
    this.status = status
    this.detail = detail
    this.type = type
  }
}

function buildUrl(path: string) {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  return `${env.apiBaseUrl}${path}`
}

export async function httpRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, headers, signal } = options

  const response = await fetch(buildUrl(path), {
    method,
    credentials: 'include',
    signal,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    let detail = `Request failed with status ${response.status}.`
    let type: string | undefined

    try {
      const problem = (await response.json()) as {
        detail?: string
        type?: string
      }

      detail = problem.detail || detail
      type = problem.type
    } catch {
      // Fall back to the generic message when the response is not JSON.
    }

    throw new ApiError(response.status, detail, type)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}
