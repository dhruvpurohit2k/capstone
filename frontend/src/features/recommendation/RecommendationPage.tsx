import { useState, type FormEvent } from 'react'
import { getRecommendations } from '../../api/recommendation.ts'
import { formatLabel } from '../policy/formatPolicy.ts'
import PolicyCard from '../policy/PolicyCard.tsx'
import { premiumFrequencySchema } from '../policy/schemas/policyResponses.ts'
import type { RecommendedPolicy } from './schemas/recommendationResponses.ts'

type Props = {
  token: string
  onSignOut: () => void
  onRegister: (productId: number, recommendationItemId: number) => void
}

export default function RecommendationPage({ token, onSignOut, onRegister }: Props) {
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState<RecommendedPolicy[] | null>(null)
  const [error, setError] = useState('')

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const dateOfBirth = String(form.get('dateOfBirth'))
    const insuranceGoals = String(form.get('insuranceGoals')).trim()
    const preferredFrequency = String(form.get('preferredFrequency'))
    const preferredPolicyType = String(form.get('preferredPolicyType')).trim()
    setResults(null)
    setError('')
    if (!dateOfBirth && !insuranceGoals && !preferredFrequency && !preferredPolicyType) {
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
        preferredPolicyType: preferredPolicyType || undefined,
      })
      if (response.success) setResults(response.data.recommendations)
      else if (response.status === 401) onSignOut()
      else setError(response.errors[0]?.message || 'Could not load recommendations.')
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
        <label>Date of birth <span className="field-hint">Optional. Your account date is used by default.</span>
          <input name="dateOfBirth" type="date" />
        </label>
        <button type="submit" className="primary-button registration-action" disabled={loading}>
          {loading ? 'Finding recommendations…' : 'Get recommendations'}
        </button>
        {error && <p className="error-list" role="alert">{error}</p>}
      </form>

      {results && (results.length ? (
        <ul className="policy-grid" aria-label="Recommended policies">
          {results.map((policy) => (
            <li key={policy.recommendationItemId}>
              <PolicyCard policy={policy} recommendation={policy}
                onRegister={() => onRegister(policy.productId, policy.recommendationItemId)} />
            </li>
          ))}
        </ul>
      ) : <p className="empty-state" role="status">No matching recommendations were found.</p>)}
    </>
  )
}
