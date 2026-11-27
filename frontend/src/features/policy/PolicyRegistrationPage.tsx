import { useEffect, useState, type FormEvent } from 'react'
import { previewPolicyRegistration, registerPolicy } from '../../api/policy.ts'
import { formatCurrency, formatDate, formatLabel } from './formatPolicy.ts'
import type { PolicyRegistration, PolicyRegistrationPreview } from './schemas/policyResponses.ts'

type Props = {
  productId: number
  recommendationItemId?: number
  token: string
  onSignOut: () => void
  onBack: () => void
  onDashboard: () => void
}

type Quote = {
  requestedStart: string | null
  details?: PolicyRegistrationPreview
  error?: string
}

export default function PolicyRegistrationPage({ productId, recommendationItemId, token, onSignOut, onBack, onDashboard }: Props) {
  const [startDate, setStartDate] = useState<string | null>(null)
  const [quote, setQuote] = useState<Quote | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [registered, setRegistered] = useState<PolicyRegistration | null>(null)

  useEffect(() => {
    if (startDate === '') return
    let active = true

    function showError(message: string) {
      setQuote((previous) => ({ requestedStart: startDate, details: previous?.details, error: message }))
    }

    previewPolicyRegistration(token, { productId, startDate: startDate ?? undefined }).then((response) => {
      if (!active) return
      if (response.success) {
        setQuote({ requestedStart: startDate, details: response.data })
      } else if (response.status === 401) {
        onSignOut()
      } else {
        showError(response.errors[0]?.message || 'Could not load the policy quote.')
      }
    }).catch(() => {
      if (active) showError('Could not load the policy quote. Please try again.')
    })

    return () => { active = false }
  }, [productId, token, startDate, onSignOut])

  const details = quote?.details
  const currentQuote = quote?.requestedStart === startDate && !quote?.error
  const loading = startDate !== '' && quote?.requestedStart !== startDate

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!details || !currentQuote || submitting || startDate === '') return
    setSubmitting(true)
    setSubmitError('')

    try {
      const response = await registerPolicy(token, { productId, startDate: details.startDate, recommendationItemId })
      if (response.success) {
        setRegistered(response.data)
      } else if (response.status === 401) {
        onSignOut()
      } else {
        setSubmitError(response.errors[0]?.message || 'Could not register the policy.')
      }
    } catch {
      setSubmitError('Could not register the policy. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  if (registered) {
    return (
      <>
        <h1>Policy registered</h1>
        <section className="registration-card" aria-label="Registration confirmation">
          <p role="status">{registered.message}</p>
          <dl className="registration-details">
            <div><dt>Policy</dt><dd>{registered.details.policyName}</dd></div>
            <div><dt>Policy number</dt><dd>{registered.policyNumber}</dd></div>
            <div><dt>Start date</dt><dd>{formatDate(registered.details.startDate)}</dd></div>
            <div><dt>Total premium</dt><dd>{formatCurrency(registered.details.totalPremiumPayable)}</dd></div>
          </dl>
          <button className="primary-button registration-action" type="button" onClick={onDashboard}>View dashboard</button>
        </section>
      </>
    )
  }

  return (
    <>
      <button className="registration-back" type="button" onClick={onBack} disabled={submitting}>{recommendationItemId ? 'Back to AI Recommendations' : 'Back to Browse Policy'}</button>
      <h1>Register for a policy</h1>
      {loading && <p className="muted" role="status">{details ? 'Updating your quote…' : 'Loading policy details…'}</p>}
      {quote?.error && !loading && <p className="error-list" role="alert">{quote.error}</p>}
      {details && (
        <form className="registration-card" onSubmit={submit}>
          <h2>{details.policyName}</h2>
          <p className="muted">{details.productCode} · {formatLabel(details.policyType)}</p>
          <div className="registration-sections">
            <section aria-labelledby="registration-customer">
              <h3 id="registration-customer">Customer details</h3>
              <dl className="registration-details">
                <div><dt>Full name</dt><dd>{details.customerName}</dd></div>
                <div><dt>Date of birth</dt><dd>{formatDate(details.dateOfBirth)}</dd></div>
                <div><dt>Email address</dt><dd>{details.email}</dd></div>
                <div><dt>Phone number</dt><dd>{details.phone}</dd></div>
              </dl>
            </section>
            <section aria-labelledby="registration-policy">
              <h3 id="registration-policy">Coverage and premium</h3>
              <dl className="registration-details">
                <div><dt>Policy term</dt><dd>{details.termMonths} months</dd></div>
                <div><dt>Maturity date</dt><dd>{currentQuote ? formatDate(details.maturityDate) : '—'}</dd></div>
                <div><dt>Payment frequency</dt><dd>{formatLabel(details.premiumFrequency)}</dd></div>
                <div><dt>Premium per installment</dt><dd>{formatCurrency(details.premiumAmount)}</dd></div>
                <div><dt>Sum insured</dt><dd>{formatCurrency(details.sumInsured)}</dd></div>
                <div><dt>Total premium · {details.installmentCount} installments</dt><dd>{formatCurrency(details.totalPremiumPayable)}</dd></div>
              </dl>
            </section>
          </div>
          <label className="registration-date">Policy start date
            <input name="startDate" type="date" required disabled={submitting} value={startDate ?? details.startDate}
              onChange={(event) => {
                setStartDate(event.target.value)
                setSubmitError('')
              }} />
          </label>
          {submitError && <p className="error-list" role="alert">{submitError}</p>}
          <button className="primary-button registration-action" type="submit" disabled={submitting || !currentQuote}>
            {submitting ? 'Registering…' : 'Confirm registration'}
          </button>
        </form>
      )}
    </>
  )
}
