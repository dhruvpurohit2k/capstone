import {
  browsePoliciesResponseSchema,
  dueSumResponseSchema,
  policyRegistrationPreviewResponseSchema,
  policyRegistrationResponseSchema,
  subscribedPoliciesResponseSchema,
  upcomingInstallmentsResponseSchema,
} from '../features/policy/schemas/policyResponses.ts'
import { apiRequest } from './client.ts'

type PolicyRegistrationRequest = {
  productId: number
  startDate?: string
}

export function getSubscribedPolicies(token: string) {
  return apiRequest({ url: '/policy/subscribed' }, subscribedPoliciesResponseSchema, token)
}

export function browsePolicies(token: string) {
  return apiRequest({ url: '/policy/browse' }, browsePoliciesResponseSchema, token)
}

export function getDueSum(token: string) {
  return apiRequest({ url: '/policy/due/sum' }, dueSumResponseSchema, token)
}

export function getUpcomingInstallments(token: string) {
  return apiRequest({ url: '/policy/due/upcoming' }, upcomingInstallmentsResponseSchema, token)
}

export function previewPolicyRegistration(token: string, params: PolicyRegistrationRequest) {
  return apiRequest({ url: '/policy/register/preview', params }, policyRegistrationPreviewResponseSchema, token)
}

export function registerPolicy(token: string, data: PolicyRegistrationRequest & { recommendationItemId?: number }) {
  return apiRequest({ method: 'POST', url: '/policy/register', data }, policyRegistrationResponseSchema, token)
}
