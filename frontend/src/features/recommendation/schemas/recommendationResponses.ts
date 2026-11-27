import { z } from 'zod'
import { availablePolicySchema } from '../../policy/schemas/policyResponses.ts'
import { apiResponseSchema } from '../../../shared/schemas/apiResponse.ts'

const recommendedPolicySchema = availablePolicySchema.extend({
  recommendationItemId: z.number().int().positive(),
  rank: z.number().int().positive(),
  reason: z.string().min(1),
  suitability: z.enum(['HIGH', 'MEDIUM', 'LOW']),
})

export const recommendationResponseSchema = apiResponseSchema(z.object({
  recommendationRequestId: z.number().int().positive(),
  calculatedAge: z.number().int().nonnegative(),
  recommendations: z.array(recommendedPolicySchema),
}))

export type RecommendedPolicy = z.infer<typeof recommendedPolicySchema>
