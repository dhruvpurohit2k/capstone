import { z } from 'zod'
import { apiResponseSchema } from '../../../shared/schemas/apiResponse.ts'
import { customerSchema } from '../../auth/schemas/authResponses.ts'
import {
  availablePolicySchema,
  paymentStatusSchema,
  premiumFrequencySchema,
} from '../../policy/schemas/policyResponses.ts'

const id = z.number().int().positive()
const timestamp = z.iso.datetime({ offset: true })

const customerViewSchema = customerSchema.extend({
  createdAt: timestamp,
  updatedAt: timestamp,
})

const insuranceProductViewSchema = availablePolicySchema.omit({ productId: true }).extend({
  id,
  minEntryAge: z.number().int().nonnegative(),
  maxEntryAge: z.number().int().nonnegative(),
  active: z.boolean(),
})

const customerPolicyViewSchema = z.object({
  id,
  policyNumber: z.string(),
  customerId: id,
  productId: id,
  startDate: z.iso.date(),
  maturityDate: z.iso.date(),
  premiumFrequency: premiumFrequencySchema,
  premiumAmount: z.number().positive(),
  sumInsured: z.number().positive(),
  totalPremiumPayable: z.number().positive(),
  status: z.string(),
  createdAt: timestamp,
})

const premiumInstallmentViewSchema = z.object({
  id,
  policyId: id,
  installmentNumber: z.number().int().positive(),
  dueDate: z.iso.date(),
  gracePeriodEndDate: z.iso.date(),
  scheduledAmount: z.number().positive(),
  assessedPenaltyAmount: z.number().nonnegative(),
  paidAmount: z.number().nonnegative(),
  paymentStatus: paymentStatusSchema,
})

const premiumPaymentViewSchema = z.object({
  id,
  installmentId: id,
  transactionReference: z.string(),
  paymentDate: z.iso.date(),
  amountPaid: z.number().positive(),
  penaltyAmount: z.number().nonnegative(),
  paymentMethod: z.string(),
  createdAt: timestamp,
})

const recommendationRequestViewSchema = z.object({
  id,
  customerId: id,
  insuranceGoals: z.string().nullable(),
  preferredTerm: z.number().int().positive().nullable(),
  preferredPolicyType: z.string().nullable(),
  calculatedAge: z.number().int().nonnegative(),
  modelName: z.string(),
  modelVersion: z.string(),
  promptVersion: z.string(),
  requestedAt: timestamp,
})

const recommendationItemViewSchema = z.object({
  id,
  recommendationRequestId: id,
  productId: id,
  rankOrder: z.number().int().positive(),
  recommendationReason: z.string(),
  suitability: z.string(),
  selectedForRegistration: z.boolean(),
})

function devResponseSchemas<T extends z.ZodType>(item: T) {
  return { list: apiResponseSchema(z.array(item)), byId: apiResponseSchema(item) }
}

export const devResponses = {
  customers: devResponseSchemas(customerViewSchema),
  'insurance-products': devResponseSchemas(insuranceProductViewSchema),
  'customer-policies': devResponseSchemas(customerPolicyViewSchema),
  'premium-installments': devResponseSchemas(premiumInstallmentViewSchema),
  'premium-payments': devResponseSchemas(premiumPaymentViewSchema),
  'ai-recommendation-requests': devResponseSchemas(recommendationRequestViewSchema),
  'ai-recommendation-items': devResponseSchemas(recommendationItemViewSchema),
}
