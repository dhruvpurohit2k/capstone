import { loginResponseSchema, logoutResponseSchema, signupResponseSchema } from '../features/auth/schemas/authResponses.ts'
import { apiRequest } from './client.ts'

type LoginRequest = {
  email: string
  password: string
}

type SignupRequest = LoginRequest & {
  fullName: string
  dateOfBirth: string
  phone: string
}

export function login(data: LoginRequest) {
  return apiRequest({ method: 'POST', url: '/auth/login', data }, loginResponseSchema)
}

export function signup(data: SignupRequest) {
  return apiRequest({ method: 'POST', url: '/auth/signup', data }, signupResponseSchema)
}


export function logout() {
  return apiRequest({ method: 'POST', url: '/auth/logout' }, logoutResponseSchema)
}
