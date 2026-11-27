import {
  browsePoliciesResponseSchema,
  dueInstallmentsResponseSchema,
  dueSumResponseSchema,
  policyPaymentsResponseSchema,
  policyRegistrationPreviewResponseSchema,
  policyRegistrationResponseSchema,
  policyStatusResponseSchema,
  subscribedPoliciesResponseSchema,
  upcomingInstallmentsResponseSchema,
} from '../features/policy/schemas/policyResponses.ts'
import { apiRequest } from './client.ts'

export type PolicyRegistrationRequest = {
  productId: number
  startDate?: string
}

export function getSubscribedPolicies(token: string) {
  return apiRequest({ url: '/policy/subscribed' }, subscribedPoliciesResponseSchema, token)
}

export function browsePolicies(token: string) {
  return apiRequest({ url: '/policy/browse' }, browsePoliciesResponseSchema, token)
}

export function getPolicyStatus(token: string, policyId: number) {
  return apiRequest({ url: `/policy/${policyId}/status` }, policyStatusResponseSchema, token)
}

export function getDueInstallments(token: string) {
  return apiRequest({ url: '/policy/due' }, dueInstallmentsResponseSchema, token)
}

export function getDueSum(token: string) {
  return apiRequest({ url: '/policy/due/sum' }, dueSumResponseSchema, token)
}

export function getUpcomingInstallments(token: string) {
  return apiRequest({ url: '/policy/due/upcoming' }, upcomingInstallmentsResponseSchema, token)
}

export function getPolicyPayments(token: string) {
  return apiRequest({ url: '/policy/payments' }, policyPaymentsResponseSchema, token)
}

export function previewPolicyRegistration(token: string, params: PolicyRegistrationRequest) {
  return apiRequest({ url: '/policy/register/preview', params }, policyRegistrationPreviewResponseSchema, token)
}

export function registerPolicy(token: string, data: PolicyRegistrationRequest & { recommendationItemId?: number }) {
  return apiRequest({ method: 'POST', url: '/policy/register', data }, policyRegistrationResponseSchema, token)
}
