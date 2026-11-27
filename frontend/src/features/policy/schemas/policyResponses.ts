import { z } from 'zod'
import { apiResponseSchema } from '../../../shared/schemas/apiResponse.ts'

export const premiumFrequencySchema = z.enum(['MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY'])
export const paymentStatusSchema = z.enum(['PAID', 'PENDING', 'PARTIALLY_PAID', 'OVERDUE'])

const policyDetailsSchema = z.object({
  policyName: z.string().min(1),
  policyType: z.string().min(1),
  termMonths: z.number().int().positive(),
  premiumFrequency: premiumFrequencySchema,
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

const policyStatusSchema = subscribedPolicySchema.extend({
  productId: z.number().int().positive(),
  productCode: z.string().min(1),
  subscribedAt: z.iso.datetime({ offset: true }),
  daysUntilMaturity: z.number().int().nonnegative(),
  totalPremiumPayable: z.number().positive(),
  totalPaid: z.number().nonnegative(),
  remainingPremium: z.number().nonnegative(),
  outstandingBalance: z.number().nonnegative(),
  amountDue: z.number().nonnegative(),
  overdueAmount: z.number().nonnegative(),
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

const paymentHistoryItemSchema = z.object({
  paymentId: z.number().int().positive(),
  installmentId: z.number().int().positive(),
  policyNumber: z.string().min(1),
  installmentNumber: z.number().int().positive(),
  transactionReference: z.string().min(1),
  paymentDate: z.iso.date(),
  amountPaid: z.number().positive(),
  penaltyAmount: z.number().nonnegative(),
  paymentMethod: z.string().min(1),
})

const paymentActivityItemSchema = z.discriminatedUnion('type', [
  z.object({ type: z.literal('PAYMENT'), payment: paymentHistoryItemSchema }),
  z.object({ type: z.literal('OPEN_INSTALLMENT'), installment: dueInstallmentSchema }),
])

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
export const policyStatusResponseSchema = apiResponseSchema(policyStatusSchema)
export const dueInstallmentsResponseSchema = apiResponseSchema(z.array(dueInstallmentSchema))
export const dueSumResponseSchema = apiResponseSchema(z.object({
  totalAmountDue: z.number().nonnegative(),
}))
export const upcomingInstallmentsResponseSchema = dueInstallmentsResponseSchema
export const policyPaymentsResponseSchema = apiResponseSchema(z.array(paymentActivityItemSchema))
export const policyRegistrationPreviewResponseSchema = apiResponseSchema(policyRegistrationPreviewSchema)
export const policyRegistrationResponseSchema = apiResponseSchema(policyRegistrationSchema, 201)

export type SubscribedPolicy = z.infer<typeof subscribedPolicySchema>
export type AvailablePolicy = z.infer<typeof availablePolicySchema>
export type PolicyStatus = z.infer<typeof policyStatusSchema>
export type DueInstallment = z.infer<typeof dueInstallmentSchema>
export type PaymentActivityItem = z.infer<typeof paymentActivityItemSchema>
export type PolicyRegistrationPreview = z.infer<typeof policyRegistrationPreviewSchema>
export type PolicyRegistration = z.infer<typeof policyRegistrationSchema>
