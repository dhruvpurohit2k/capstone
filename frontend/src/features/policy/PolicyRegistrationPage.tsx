import { useEffect, useState, type FormEvent } from 'react'
import type { ApiFailure } from '../../api/client.ts'
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
  onRegistered: (productId: number) => void
}

type RegistrationError = ApiFailure['errors'][number]

type Quote = {
  requestedStart: string | null
  attempt: number
  details?: PolicyRegistrationPreview
  error?: RegistrationError
  retryable?: boolean
}

export default function PolicyRegistrationPage({
  productId, recommendationItemId, token, onSignOut, onBack, onDashboard, onRegistered,
}: Props) {
  const [startDate, setStartDate] = useState<string | null>(null)
  const [quote, setQuote] = useState<Quote | null>(null)
  const [attempt, setAttempt] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<RegistrationError | null>(null)
  const [registered, setRegistered] = useState<PolicyRegistration | null>(null)

  useEffect(() => {
    if (startDate === '') return
    let active = true

    function showError(error: RegistrationError, retryable: boolean) {
      setQuote((previous) => ({ requestedStart: startDate, attempt, details: previous?.details, error, retryable }))
    }

    previewPolicyRegistration(token, { productId, startDate: startDate ?? undefined }).then((response) => {
      if (!active) return
      if (response.success) {
        setQuote({ requestedStart: startDate, attempt, details: response.data })
      } else if (response.status === 401) {
        onSignOut()
      } else {
        const error = response.errors[0] ?? { fieldName: null, message: 'Could not load the policy quote.' }
        showError(error, response.status >= 500)
      }
    }).catch(() => {
      if (active) showError({ fieldName: null, message: 'Could not load the policy quote. Please try again.' }, true)
    })

    return () => { active = false }
  }, [productId, token, startDate, attempt, onSignOut])

  const details = quote?.details
  const matchingQuote = quote?.requestedStart === startDate && quote.attempt === attempt ? quote : null
  const currentQuote = matchingQuote?.error ? undefined : matchingQuote?.details
  const loading = startDate !== '' && !matchingQuote
  const error = submitError ?? matchingQuote?.error
  const dateError = error?.fieldName === 'startDate' ? error.message : undefined

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!currentQuote || submitting) return
    setSubmitting(true)
    setSubmitError(null)

    try {
      const response = await registerPolicy(token, { productId, startDate: currentQuote.startDate, recommendationItemId })
      if (response.success) {
        onRegistered(productId)
        setRegistered(response.data)
      } else if (response.status === 401) {
        onSignOut()
      } else {
        setSubmitError(response.errors[0] ?? { fieldName: null, message: 'Could not register the policy.' })
      }
    } catch {
      setSubmitError({ fieldName: null, message: 'Could not register the policy. Please try again.' })
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
      <form className="registration-card" onSubmit={submit}>
        {details && (
          <>
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
                  <div><dt>Maturity date</dt><dd>{currentQuote ? formatDate(currentQuote.maturityDate) : '—'}</dd></div>
                  <div><dt>Payment frequency</dt><dd>{formatLabel(details.premiumFrequency)}</dd></div>
                  <div><dt>Premium per installment</dt><dd>{formatCurrency(details.premiumAmount)}</dd></div>
                  <div><dt>Sum insured</dt><dd>{formatCurrency(details.sumInsured)}</dd></div>
                  <div><dt>Installments</dt><dd>{currentQuote?.installmentCount ?? '—'}</dd></div>
                  <div><dt>Total premium payable</dt><dd>{currentQuote ? formatCurrency(currentQuote.totalPremiumPayable) : '—'}</dd></div>
                </dl>
              </section>
            </div>
          </>
        )}
        <label className="registration-date">Policy start date
          <input name="startDate" type="date" required disabled={submitting} value={startDate ?? details?.startDate ?? ''}
            aria-invalid={Boolean(dateError)} aria-describedby={dateError ? 'registration-date-error' : undefined}
            onChange={(event) => {
              setStartDate(event.target.value)
              setAttempt((value) => value + 1)
              setSubmitError(null)
            }} />
          {dateError && <span className="field-error" id="registration-date-error" role="alert">{dateError}</span>}
        </label>
        {loading && <p className="muted" role="status">{details ? 'Updating your quote…' : 'Loading policy details…'}</p>}
        {error && !dateError && <p className="error-list" role="alert">{error.message}</p>}
        {matchingQuote?.retryable && (
          <button className="registration-back" type="button" onClick={() => setAttempt((value) => value + 1)}>Retry preview</button>
        )}
        <button className="primary-button registration-action" type="submit" disabled={submitting || !currentQuote}>
          {submitting ? 'Registering…' : 'Confirm registration'}
        </button>
      </form>
    </>
  )
}
