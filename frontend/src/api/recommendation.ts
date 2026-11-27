import { recommendationResponseSchema } from '../features/recommendation/schemas/recommendationResponses.ts'
import { apiRequest } from './client.ts'

type RecommendationRequest = {
  dateOfBirth?: string
  insuranceGoals?: string
  preferredTermMonths?: number
  preferredPolicyType?: string
}

export function getRecommendations(token: string, data: RecommendationRequest) {
  return apiRequest({ method: 'POST', url: '/recommendations', data, timeout: 90_000 }, recommendationResponseSchema, token)
}
