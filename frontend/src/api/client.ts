import axios, { type AxiosRequestConfig } from 'axios'
import type { z } from 'zod'
import { csrfResponseSchema, loginResponseSchema } from '../features/auth/schemas/authResponses.ts'
import { springErrorResponseSchema } from '../shared/schemas/apiResponse.ts'

export type ApiFailure = {
  success: false
  status: number
  errors: { fieldName: string | null; message: string }[]
}

const tokenKey = 'healthdekho-token'
const readMethods = ['GET', 'HEAD', 'OPTIONS', 'TRACE']
let sessionVersion = 0
let csrfRequest: Promise<string> | undefined
let refreshRequest: Promise<string | null> | undefined

const client = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || '/api',
  headers: { Accept: 'application/json' },
  withCredentials: true,
  timeout: 15_000,
  validateStatus: (status) => status >= 200 && status < 600,
})

export function getSessionToken() {
  return sessionStorage.getItem(tokenKey)
}

export function setSessionToken(token: string | null) {
  sessionVersion++
  refreshRequest = undefined
  if (token) sessionStorage.setItem(tokenKey, token)
  else sessionStorage.removeItem(tokenKey)
}

export function refreshAccessToken(): Promise<string | null> {
  if (refreshRequest) return refreshRequest

  const version = sessionVersion
  const pending = request({ method: 'POST', url: '/auth/refresh' }, loginResponseSchema)
    .then((response) => {
      if (!response.success || version !== sessionVersion) return null
      const token = response.data.token
      sessionStorage.setItem(tokenKey, token)
      return token
    })
    .finally(() => {
      if (refreshRequest === pending) refreshRequest = undefined
    })

  refreshRequest = pending
  return pending
}

export async function apiRequest<T extends z.ZodType<{ status: number }>>(
  config: AxiosRequestConfig,
  schema: T,
  token?: string,
): Promise<z.output<T> | ApiFailure> {
  const version = sessionVersion
  const accessToken = token === undefined ? undefined : (getSessionToken() ?? token)
  const response = await request(config, schema, accessToken)
  if (token === undefined || response.status !== 401 || version !== sessionVersion) return response

  const currentToken = getSessionToken()
  const refreshed = currentToken && currentToken !== accessToken ? currentToken : await refreshAccessToken()
  return refreshed && version === sessionVersion ? request(config, schema, refreshed) : response
}

function getCsrfToken() {
  if (!csrfRequest) {
    csrfRequest = request({ url: '/auth/csrf' }, csrfResponseSchema)
      .then((response) => {
        if (!response.success) throw new Error('Could not obtain a CSRF token')
        return response.data.token
      })
      .finally(() => { csrfRequest = undefined })
  }
  return csrfRequest
}

async function request<T extends z.ZodType<{ status: number }>>(
  config: AxiosRequestConfig,
  schema: T,
  token?: string,
): Promise<z.output<T> | ApiFailure> {
  const method = (config.method ?? 'GET').toUpperCase()
  const headers: Record<string, string> = {}
  if (token) headers.Authorization = `Bearer ${token}`
  if (!readMethods.includes(method)) headers['X-XSRF-TOKEN'] = await getCsrfToken()

  const response = await client.request<unknown>({ ...config, headers })

  const result = schema.safeParse(response.data)
  if (result.success && result.data.status === response.status) {
    return result.data
  }

  const fallback = springErrorResponseSchema.safeParse(response.data)
  if (fallback.success && fallback.data.status === response.status) {
    return {
      success: false,
      status: response.status,
      errors: [{ fieldName: null, message: fallback.data.message || fallback.data.error }],
    }
  }

  throw new Error(`Unexpected API response (HTTP ${response.status})`)
}
