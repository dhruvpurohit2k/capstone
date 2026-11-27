import { z } from 'zod'
import { apiResponseSchema } from '../../../shared/schemas/apiResponse.ts'

const customerSchema = z.object({
  id: z.number().int().positive(),
  email: z.email(),
  fullName: z.string().min(1),
  dateOfBirth: z.iso.date(),
  phone: z.string().min(1),
  status: z.string().min(1),
})

export const loginResponseSchema = apiResponseSchema(z.object({
  token: z.string().min(1),
  tokenType: z.literal('Bearer'),
}))

export const signupResponseSchema = apiResponseSchema(customerSchema, 201)

export const csrfResponseSchema = apiResponseSchema(z.object({
  token: z.string().min(1),
  headerName: z.literal('X-XSRF-TOKEN'),
}))

export const logoutResponseSchema = apiResponseSchema(z.object({
  message: z.string().min(1),
}))
