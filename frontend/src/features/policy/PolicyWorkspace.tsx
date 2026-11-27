import { useEffect, useState } from 'react'
import { logout } from '../../api/auth.ts'
import { browsePolicies, getDueSum, getSubscribedPolicies, getUpcomingInstallments } from '../../api/policy.ts'
import RecommendationPage from '../recommendation/RecommendationPage.tsx'
import { formatCurrency, formatDate, formatLabel } from './formatPolicy.ts'
import PolicyCard from './PolicyCard.tsx'
import PolicyRegistrationPage from './PolicyRegistrationPage.tsx'
import type { AvailablePolicy, SubscribedPolicy } from './schemas/policyResponses.ts'

const sections = ['Dashboard', 'Browse Policy', 'AI Recommendations'] as const
type Section = typeof sections[number]

type Props = {
  token: string
  onSignOut: () => void
}

export default function PolicyWorkspace({ token, onSignOut }: Props) {
  const [section, setSection] = useState<Section>('Dashboard')
  const [selection, setSelection] = useState<{ productId: number; recommendationItemId?: number } | null>(null)
  const [signingOut, setSigningOut] = useState(false)
  const [signOutError, setSignOutError] = useState('')

  async function signOut() {
    setSigningOut(true)
    setSignOutError('')
    try {
      const response = await logout()
      if (response.success) onSignOut()
      else setSignOutError(response.errors[0]?.message || 'Could not sign out. Please try again.')
    } catch {
      setSignOutError('Could not sign out. Please try again.')
    } finally {
      setSigningOut(false)
    }
  }

  function selectSection(item: Section) {
    setSelection(null)
    setSection(item)
  }

  return (
    <div className="workspace">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark" aria-hidden="true">+</span> HealthDekho</div>
        <nav aria-label="Main navigation">
          {sections.map((item) => (
            <button key={item} type="button" aria-current={item === section ? 'page' : undefined}
              onClick={() => selectSection(item)}>{item}</button>
          ))}
        </nav>
        <button className="signout" type="button" onClick={signOut} disabled={signingOut}>
          {signingOut ? 'Signing out…' : 'Sign out'}
        </button>
      </aside>

      <main className="workspace-content">
        {signOutError && <p className="error-list" role="alert">{signOutError}</p>}
        {selection ? (
          <PolicyRegistrationPage key={selection.productId} {...selection} token={token} onSignOut={onSignOut}
            onBack={() => setSelection(null)} onDashboard={() => selectSection('Dashboard')} />
        ) : (
          <>
            <h1>{section}</h1>
            {section === 'Dashboard' && <PaymentSummary token={token} onSignOut={onSignOut} />}
            {section !== 'AI Recommendations' && (
              <PolicyList key={section} token={token} onSignOut={onSignOut} browse={section === 'Browse Policy'}
                onRegister={(productId) => setSelection({ productId })} />
            )}
          </>
        )}
        {section === 'AI Recommendations' && (
          <div hidden={selection !== null}>
            <RecommendationPage token={token} onSignOut={onSignOut}
              onRegister={(productId, recommendationItemId) => setSelection({ productId, recommendationItemId })} />
          </div>
        )}
      </main>
    </div>
  )
}

type PaymentCardContent = { value: string; detail: string } | { error: string } | null

function PaymentSummary({ token, onSignOut }: Props) {
  const [total, setTotal] = useState<PaymentCardContent>(null)
  const [upcoming, setUpcoming] = useState<PaymentCardContent>(null)

  useEffect(() => {
    let active = true

    getDueSum(token).then((response) => {
      if (!active) return
      if (response.success) {
        setTotal({ value: formatCurrency(response.data.totalAmountDue), detail: 'Unpaid amounts due today or earlier, including penalties' })
      } else if (response.status === 401) {
        onSignOut()
      } else {
        setTotal({ error: 'Could not load the total due.' })
      }
    }).catch(() => {
      if (active) setTotal({ error: 'Could not load the total due.' })
    })

    getUpcomingInstallments(token).then((response) => {
      if (!active) return
      if (!response.success) {
        if (response.status === 401) onSignOut()
        else setUpcoming({ error: 'Could not load upcoming payments.' })
        return
      }

      const [next] = response.data
      if (!next) {
        setUpcoming({ value: 'None scheduled', detail: 'You have no upcoming installments.' })
        return
      }

      const count = response.data.length
      const nextAmount = response.data.reduce((sum, installment) => sum + installment.amountDue, 0)
      const dueDate = formatDate(next.dueDate)
      setUpcoming({
        value: formatCurrency(nextAmount),
        detail: `Due ${dueDate} · ${count} ${count === 1 ? 'installment' : 'installments'}`,
      })
    }).catch(() => {
      if (active) setUpcoming({ error: 'Could not load upcoming payments.' })
    })

    return () => { active = false }
  }, [token, onSignOut])

  return (
    <section className="payment-summary" aria-label="Payment summary">
      <PaymentCard title="Total due" content={total} featured />
      <PaymentCard title="Upcoming payment" content={upcoming} />
    </section>
  )
}

function PaymentCard({ title, content, featured = false }: {
  title: string
  content: PaymentCardContent
  featured?: boolean
}) {
  return (
    <article className={featured ? 'payment-card payment-card-featured' : 'payment-card'}>
      <h2>{title}</h2>
      <div aria-live="polite">
        {!content ? <p className="payment-card-detail">Loading…</p> : 'error' in content ? (
          <p className="payment-card-detail">{content.error}</p>
        ) : (
          <>
            <p className="payment-card-value">{content.value}</p>
            <p className="payment-card-detail">{content.detail}</p>
          </>
        )}
      </div>
    </article>
  )
}

function PolicyList({ token, onSignOut, browse, onRegister }: Props & {
  browse: boolean
  onRegister: (productId: number) => void
}) {
  const [rows, setRows] = useState<(AvailablePolicy | SubscribedPolicy)[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    const request = browse ? browsePolicies(token) : getSubscribedPolicies(token)

    request.then((response) => {
      if (!active) return
      if (response.success) {
        setRows(response.data)
      } else if (response.status === 401) {
        onSignOut()
      } else {
        setError(response.errors[0]?.message || 'Could not load policies.')
      }
    }).catch(() => {
      if (active) setError('Could not load policies. Please try again.')
    })

    return () => { active = false }
  }, [token, browse, onSignOut])

  if (error) return <p className="error-list" role="alert">{error}</p>
  if (!rows) return <p role="status" className="empty-state">Loading policies…</p>
  if (rows.length === 0) {
    return <p className="empty-state">{browse ? 'No policies are currently available.' : 'You haven’t registered for a policy yet.'}</p>
  }

  if (!browse) return <SubscribedPolicyTable policies={rows.filter((policy) => 'policyId' in policy)} />

  return (
    <ul className="policy-grid" aria-label="Available policies">
      {rows.filter((policy) => 'productId' in policy).map((policy) => (
        <li key={policy.productId}>
          <PolicyCard policy={policy} onRegister={() => onRegister(policy.productId)} />
        </li>
      ))}
    </ul>
  )
}

function SubscribedPolicyTable({ policies }: { policies: SubscribedPolicy[] }) {
  return (
    <div className="subscribed-table">
      <table>
        <caption>Your policies</caption>
        <thead>
          <tr>
            <th scope="col">Policy number</th>
            <th scope="col">Policy</th>
            <th scope="col">Type</th>
            <th scope="col">Term</th>
            <th scope="col">Start date</th>
            <th scope="col">Maturity date</th>
            <th scope="col">Frequency</th>
            <th scope="col">Premium per installment</th>
            <th scope="col">Sum insured</th>
            <th scope="col">Status</th>
          </tr>
        </thead>
        <tbody>
          {policies.map((policy) => (
            <tr key={policy.policyId}>
              <td>{policy.policyNumber}</td>
              <th scope="row">{policy.policyName}</th>
              <td>{formatLabel(policy.policyType)}</td>
              <td>{policy.termMonths} months</td>
              <td>{formatDate(policy.startDate)}</td>
              <td>{formatDate(policy.maturityDate)}</td>
              <td>{formatLabel(policy.premiumFrequency)}</td>
              <td>{formatCurrency(policy.premiumAmount)}</td>
              <td>{formatCurrency(policy.sumInsured)}</td>
              <td><span className="policy-status">{formatLabel(policy.status)}</span></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
