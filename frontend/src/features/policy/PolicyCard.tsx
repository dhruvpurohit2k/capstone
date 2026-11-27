import { formatCurrency, formatLabel } from './formatPolicy.ts'
import type { AvailablePolicy } from './schemas/policyResponses.ts'

type Props = {
  policy: AvailablePolicy
  recommendation?: { reason: string; suitability: string }
  onRegister?: () => void
}

export default function PolicyCard({ policy, recommendation, onRegister }: Props) {
  return (
    <article className="policy-card">
      <header className="policy-card-header">
        <p className="policy-card-code">{policy.productCode}</p>
        <h2>{policy.policyName}</h2>
      </header>
      <div className="policy-card-body">
        <dl className="policy-card-details">
          <div><dt>Policy type</dt><dd>{formatLabel(policy.policyType)}</dd></div>
          <div><dt>Term</dt><dd>{policy.termMonths} months</dd></div>
          <div><dt>Premium per installment</dt><dd>{formatCurrency(policy.premiumAmount)}</dd></div>
          <div><dt>Payment frequency</dt><dd>{formatLabel(policy.premiumFrequency)}</dd></div>
          <div><dt>Sum insured</dt><dd>{formatCurrency(policy.sumInsured)}</dd></div>
        </dl>
        {recommendation && (
          <div className="policy-card-reason">
            <h3>Why it was recommended</h3>
            <p>{recommendation.reason}</p>
            <p className="policy-card-suitability">Suitability: {formatLabel(recommendation.suitability)}</p>
          </div>
        )}
        {onRegister && (
          <button className="primary-button policy-card-register" type="button"
            aria-label={`Register for ${policy.policyName}`} onClick={onRegister}>Register</button>
        )}
      </div>
    </article>
  )
}
