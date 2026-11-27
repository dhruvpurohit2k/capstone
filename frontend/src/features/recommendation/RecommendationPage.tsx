import { useState, type FormEvent } from 'react'
import { getRecommendations } from '../../api/recommendation.ts'
import { formatLabel } from '../policy/formatPolicy.ts'
import AvailablePolicyTable from '../policy/AvailablePolicyTable.tsx'
import { premiumFrequencySchema } from '../policy/schemas/policyResponses.ts'
import type { RecommendedPolicy } from './schemas/recommendationResponses.ts'

type Props = {
  token: string
  registeredProducts: number[]
  onSignOut: () => void
  onRegister: (productId: number, recommendationItemId: number) => void
}

export default function RecommendationPage({ token, registeredProducts, onSignOut, onRegister }: Props) {
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState<RecommendedPolicy[] | null>(null)
  const [error, setError] = useState('')
  const [birthDateError, setBirthDateError] = useState('')
  const visibleResults = results?.filter((policy) => !registeredProducts.includes(policy.productId))
  const today = new Date()
  const localToday = new Date(today.getTime() - today.getTimezoneOffset() * 60_000).toISOString().slice(0, 10)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const dateOfBirth = String(form.get('dateOfBirth'))
    const insuranceGoals = String(form.get('insuranceGoals')).trim()
    const preferredFrequency = String(form.get('preferredFrequency'))
    const preferredTerm = String(form.get('preferredTermMonths'))
    const preferredPolicyType = String(form.get('preferredPolicyType')).trim()
    setResults(null)
    setError('')
    setBirthDateError('')
    if (!dateOfBirth && !insuranceGoals && !preferredFrequency && !preferredTerm && !preferredPolicyType) {
      setError('Fill in at least one recommendation field.')
      return
    }
    setLoading(true)

    try {
      const preferences = [insuranceGoals]
      if (preferredFrequency) preferences.push(`Preferred premium frequency: ${formatLabel(preferredFrequency)}.`)
      const response = await getRecommendations(token, {
        dateOfBirth: dateOfBirth || undefined,
        insuranceGoals: preferences.filter(Boolean).join('\n') || undefined,
        preferredTermMonths: preferredTerm ? Number(preferredTerm) : undefined,
        preferredPolicyType: preferredPolicyType || undefined,
      })
      if (response.success) setResults(response.data.recommendations)
      else if (response.status === 401) onSignOut()
      else {
        const dateError = response.errors.find((item) => item.fieldName === 'dateOfBirth')
        setBirthDateError(dateError?.message ?? '')
        setError(response.errors.find((item) => item.fieldName !== 'dateOfBirth')?.message
          || (dateError ? '' : 'Could not load recommendations.'))
      }
    } catch {
      setError('Could not load recommendations. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <form className="registration-card recommendation-form" onSubmit={submit}>
        <h2>What are you looking for?</h2>
        <p className="muted">Fill in at least one field.</p>
        <label>Insurance goals
          <textarea name="insuranceGoals" rows={3} maxLength={950} placeholder="For example, cover my family’s medical expenses" />
        </label>
        <div className="form-row">
          <label>Preferred payment frequency
            <select name="preferredFrequency">
              <option value="">Any frequency</option>
              {premiumFrequencySchema.options.map((frequency) => (
                <option key={frequency} value={frequency}>{formatLabel(frequency)}</option>
              ))}
            </select>
          </label>
          <label>Preferred policy type
            <input name="preferredPolicyType" maxLength={32} placeholder="For example, Health" />
          </label>
        </div>
        <div className="form-row">
          <label>Preferred policy term (months)
            <input name="preferredTermMonths" type="number" min={1} max={2_147_483_647} step={1} />
          </label>
          <label>Date of birth
            <span className="field-hint" id="recommendation-dob-hint">Optional. Any date entered must match your account date of birth.</span>
            <input name="dateOfBirth" type="date" max={localToday} aria-invalid={Boolean(birthDateError)}
              aria-describedby={birthDateError ? 'recommendation-dob-hint recommendation-dob-error' : 'recommendation-dob-hint'}
              onChange={() => setBirthDateError('')} />
            {birthDateError && <span className="field-error" id="recommendation-dob-error" role="alert">{birthDateError}</span>}
          </label>
        </div>
        <button type="submit" className="primary-button registration-action" disabled={loading}>
          {loading ? 'Finding recommendations…' : 'Get recommendations'}
        </button>
        {error && <p className="error-list" role="alert">{error}</p>}
      </form>

      {visibleResults && (visibleResults.length ? (
        <AvailablePolicyTable caption="Recommended policies" policies={visibleResults} recommendation={(policy) => policy}
          onRegister={(policy) => onRegister(policy.productId, policy.recommendationItemId)} />
      ) : <p className="empty-state" role="status">No matching recommendations were found.</p>)}
    </>
  )
}
