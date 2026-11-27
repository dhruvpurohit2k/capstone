import { z } from 'zod'

export const apiErrorSchema = z.object({
  fieldName: z.string().nullable(),
  message: z.string(),
})

export const apiErrorResponseSchema = z.object({
  status: z.number().int().min(400).max(599),
  success: z.literal(false),
  errors: z.array(apiErrorSchema).min(1),
})

export const springErrorResponseSchema = z.object({
  timestamp: z.iso.datetime({ offset: true }),
  status: z.number().int().min(400).max(599),
  error: z.string(),
  path: z.string().optional(),
  message: z.string().optional(),
})

export function apiResponseSchema<T extends z.ZodType>(data: T, status: 200 | 201 = 200) {
  return z.discriminatedUnion('success', [
    z.object({ status: z.literal(status), success: z.literal(true), data }),
    apiErrorResponseSchema,
  ])
}
