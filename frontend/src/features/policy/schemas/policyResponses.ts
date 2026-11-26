import { z } from 'zod'

const policyDetailsSchema = z.object({
  policyName: z.string().min(1),
  policyType: z.string().min(1),
  termMonths: z.number().int().positive(),
  premiumFrequency: z.enum(['MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY']),
  premiumAmount: z.number().positive(),
  sumInsured: z.number().positive(),
})

export const subscribedPolicySchema = policyDetailsSchema.extend({
  policyId: z.number().int().positive(),
  policyNumber: z.string().min(1),
  startDate: z.iso.date(),
  maturityDate: z.iso.date(),
  status: z.string().min(1),
})

export const availablePolicySchema = policyDetailsSchema.extend({
  productId: z.number().int().positive(),
  productCode: z.string().min(1),
})

const errorResponseSchema = z.object({
  status: z.number().int().min(400).max(599),
  success: z.literal(false),
  errors: z.array(z.object({
    fieldName: z.string().nullable(),
    message: z.string(),
  })).min(1),
})

export const subscribedPoliciesResponseSchema = z.discriminatedUnion('success', [
  z.object({
    status: z.literal(200),
    success: z.literal(true),
    data: z.array(subscribedPolicySchema),
  }),
  errorResponseSchema,
])

export const browsePoliciesResponseSchema = z.discriminatedUnion('success', [
  z.object({
    status: z.literal(200),
    success: z.literal(true),
    data: z.array(availablePolicySchema),
  }),
  errorResponseSchema,
])

export type SubscribedPolicy = z.infer<typeof subscribedPolicySchema>
export type AvailablePolicy = z.infer<typeof availablePolicySchema>
