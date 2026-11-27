import { z } from 'zod'
import { apiResponseSchema } from '../../../shared/schemas/apiResponse.ts'

const premiumFrequencySchema = z.enum(['MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY'])
const paymentStatusSchema = z.enum(['PAID', 'PENDING', 'PARTIALLY_PAID', 'OVERDUE'])

const policyDetailsSchema = z.object({
  policyName: z.string().min(1),
  policyType: z.string().min(1),
  termMonths: z.number().int().positive(),
  premiumFrequency: premiumFrequencySchema,
  premiumAmount: z.number().positive(),
  sumInsured: z.number().positive(),
})

const subscribedPolicySchema = policyDetailsSchema.extend({
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

const dueInstallmentSchema = z.object({
  installmentId: z.number().int().positive(),
  policyNumber: z.string().min(1),
  installmentNumber: z.number().int().positive(),
  dueDate: z.iso.date(),
  gracePeriodEndDate: z.iso.date(),
  paymentStatus: paymentStatusSchema,
  overdue: z.boolean(),
  scheduledAmount: z.number().positive(),
  paidAmount: z.number().nonnegative(),
  penaltyAmount: z.number().nonnegative(),
  amountDue: z.number().nonnegative(),
})

const policyRegistrationPreviewSchema = availablePolicySchema.extend({
  customerName: z.string().min(1),
  dateOfBirth: z.iso.date(),
  email: z.email(),
  phone: z.string().min(1),
  startDate: z.iso.date(),
  maturityDate: z.iso.date(),
  installmentCount: z.number().int().positive(),
  totalPremiumPayable: z.number().positive(),
})

const policyRegistrationSchema = z.object({
  policyId: z.number().int().positive(),
  policyNumber: z.string().min(1),
  status: z.string().min(1),
  message: z.string().min(1),
  details: policyRegistrationPreviewSchema,
})

export const subscribedPoliciesResponseSchema = apiResponseSchema(z.array(subscribedPolicySchema))
export const browsePoliciesResponseSchema = apiResponseSchema(z.array(availablePolicySchema))
export const dueSumResponseSchema = apiResponseSchema(z.object({
  totalAmountDue: z.number().nonnegative(),
}))
export const upcomingInstallmentsResponseSchema = apiResponseSchema(z.array(dueInstallmentSchema))
export const policyRegistrationPreviewResponseSchema = apiResponseSchema(policyRegistrationPreviewSchema)
export const policyRegistrationResponseSchema = apiResponseSchema(policyRegistrationSchema, 201)

export type SubscribedPolicy = z.infer<typeof subscribedPolicySchema>
export type AvailablePolicy = z.infer<typeof availablePolicySchema>
export type PolicyRegistrationPreview = z.infer<typeof policyRegistrationPreviewSchema>
export type PolicyRegistration = z.infer<typeof policyRegistrationSchema>
