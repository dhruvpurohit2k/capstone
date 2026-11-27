import { useState, type FormEvent } from 'react'
import { getRecommendations } from '../../api/recommendation.ts'
import AvailablePolicyTable from '../policy/AvailablePolicyTable.tsx'
import type { RecommendedPolicy } from './schemas/recommendationResponses.ts'

const unavailableMessage = 'AI recommendations are temporarily unavailable. Please try again later.'

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
    const preferredTerm = String(form.get('preferredTermMonths'))
    const preferredPolicyType = String(form.get('preferredPolicyType')).trim()
    setResults(null)
    setError('')
    setBirthDateError('')
    if (!dateOfBirth && !insuranceGoals && !preferredTerm && !preferredPolicyType) {
      setError('Fill in at least one recommendation field.')
      return
    }
    setLoading(true)

    try {
      const response = await getRecommendations(token, {
        dateOfBirth: dateOfBirth || undefined,
        insuranceGoals: insuranceGoals || undefined,
        preferredTermMonths: preferredTerm ? Number(preferredTerm) : undefined,
        preferredPolicyType: preferredPolicyType || undefined,
      })
      if (response.success) setResults(response.data.recommendations)
      else if (response.status === 401) onSignOut()
      else if (response.status >= 500) setError(unavailableMessage)
      else {
        const dateError = response.errors.find((item) => item.fieldName === 'dateOfBirth')
        setBirthDateError(dateError?.message ?? '')
        setError(response.errors.find((item) => item.fieldName !== 'dateOfBirth')?.message
          || (dateError ? '' : 'Could not load recommendations.'))
      }
    } catch {
      setError(unavailableMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <form className="registration-card recommendation-form" onSubmit={submit}>
        <h2>What are you looking for?</h2>
        <p className="muted">Fill in at least one field.</p>
        <label>Date of birth (DOB)
          <span className="field-hint" id="recommendation-dob-hint">Optional. Any date entered must match your account date of birth.</span>
          <input name="dateOfBirth" type="date" max={localToday} aria-invalid={Boolean(birthDateError)}
            aria-describedby={birthDateError ? 'recommendation-dob-hint recommendation-dob-error' : 'recommendation-dob-hint'}
            onChange={() => setBirthDateError('')} />
          {birthDateError && <span className="field-error" id="recommendation-dob-error" role="alert">{birthDateError}</span>}
        </label>
        <label>Insurance goals and preferences
          <textarea name="insuranceGoals" rows={3} maxLength={1000} placeholder="For example, cover my family’s medical expenses" />
        </label>
        <div className="form-row">
          <label>Preferred policy term (months)
            <input name="preferredTermMonths" type="number" min={1} max={2_147_483_647} step={1} />
          </label>
          <label>Insurance type
            <input name="preferredPolicyType" maxLength={32} placeholder="For example, Health" />
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
