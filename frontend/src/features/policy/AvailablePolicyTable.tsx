import { formatCurrency, formatLabel } from './formatPolicy.ts'
import type { AvailablePolicy } from './schemas/policyResponses.ts'

type Props<T extends AvailablePolicy> = {
  caption: string
  policies: T[]
  onRegister: (policy: T) => void
  recommendation?: (policy: T) => { reason: string; suitability: string }
}

export default function AvailablePolicyTable<T extends AvailablePolicy>({
  caption, policies, onRegister, recommendation,
}: Props<T>) {
  return (
    <div className="policy-table" tabIndex={0} role="region" aria-label={caption}>
      <table>
        <caption>{caption}</caption>
        <thead>
          <tr>
            <th scope="col">Product code</th>
            <th scope="col">Policy name</th>
            <th scope="col">Policy type</th>
            <th scope="col">Term</th>
            <th scope="col">Sum insured</th>
            <th scope="col">Premium frequency</th>
            <th scope="col">Premium per installment</th>
            {recommendation && (
              <>
                <th scope="col">Recommendation reason</th>
                <th scope="col">Suitability</th>
              </>
            )}
            <th scope="col">Action</th>
          </tr>
        </thead>
        <tbody>
          {policies.map((policy) => {
            const fit = recommendation?.(policy)
            return (
              <tr key={policy.productId}>
                <td>{policy.productCode}</td>
                <th scope="row">{policy.policyName}</th>
                <td>{formatLabel(policy.policyType)}</td>
                <td>{policy.termMonths} months</td>
                <td>{formatCurrency(policy.sumInsured)}</td>
                <td>{formatLabel(policy.premiumFrequency)}</td>
                <td>{formatCurrency(policy.premiumAmount)}</td>
                {fit && (
                  <>
                    <td className="policy-recommendation-reason">{fit.reason}</td>
                    <td>{formatLabel(fit.suitability)}</td>
                  </>
                )}
                <td>
                  <button className="primary-button policy-table-register" type="button"
                    aria-label={`Register for ${policy.policyName}`} onClick={() => onRegister(policy)}>Register</button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
