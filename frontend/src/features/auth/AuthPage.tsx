import { useState, type FormEvent } from 'react'
import { login, signup } from '../../api/auth.ts'
import type { ApiFailure } from '../../api/client.ts'

type Props = {
  onLogin: (token: string) => void
}

export default function AuthPage({ onLogin }: Props) {
  const [isSignup, setIsSignup] = useState(false)
  const [email, setEmail] = useState('')
  const [busy, setBusy] = useState(false)
  const [errors, setErrors] = useState<ApiFailure['errors']>([])
  const [notice, setNotice] = useState('')

  function switchMode(signupMode: boolean) {
    setIsSignup(signupMode)
    setErrors([])
    setNotice('')
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const password = String(form.get('password'))
    setBusy(true)
    setErrors([])
    setNotice('')

    try {
      if (isSignup) {
        const result = await signup({
          email,
          password,
          fullName: String(form.get('fullName')),
          dateOfBirth: String(form.get('dateOfBirth')),
          phone: String(form.get('phone')),
        })
        if (result.success) {
          setEmail(result.data.email)
          setIsSignup(false)
          setNotice('Account created. Sign in to continue.')
        } else {
          setErrors(result.errors)
        }
      } else {
        const result = await login({ email, password })
        if (result.success) {
          onLogin(result.data.token)
        } else {
          setErrors(result.errors)
        }
      }
    } catch {
      setErrors([{ fieldName: null, message: 'Could not reach HealthDekho. Please try again.' }])
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-intro" aria-label="HealthDekho">
        <div className="brand"><span className="brand-mark" aria-hidden="true">+</span> HealthDekho</div>
        <div className="intro-content">
          <h1>Your cover,<br /> in one place.</h1>
          <p>Manage your insurance policies and find cover that fits your needs.</p>
        </div>
      </section>

      <section className="auth-panel" aria-labelledby="auth-title">
        <div className="auth-card">
          <div className="auth-switch" aria-label="Account options">
            <button type="button" onClick={() => switchMode(false)} aria-pressed={!isSignup} disabled={busy}>Sign in</button>
            <button type="button" onClick={() => switchMode(true)} aria-pressed={isSignup} disabled={busy}>Sign up</button>
          </div>

          <h2 id="auth-title">{isSignup ? 'Create your account' : 'Welcome back'}</h2>
          <p className="muted">{isSignup ? 'Enter your details to get started.' : 'Sign in to manage your policies.'}</p>

          {notice && <p className="notice" role="status">{notice}</p>}
          {errors.length > 0 && (
            <ul className="error-list" role="alert">
              {errors.map((error, index) => <li key={index}>{error.message}</li>)}
            </ul>
          )}

          <form key={isSignup ? 'signup' : 'login'} onSubmit={submit} className="auth-form">
            {isSignup && (
              <label>Full name
                <input name="fullName" autoComplete="name" maxLength={255} required />
              </label>
            )}
            <label>Email address
              <input name="email" type="email" autoComplete="email" maxLength={254} value={email}
                onChange={(event) => setEmail(event.target.value)} required />
            </label>
            {isSignup && (
              <div className="form-row">
                <label>Date of birth
                  <input name="dateOfBirth" type="date" autoComplete="bday" required />
                </label>
                <label>Phone number
                  <input name="phone" type="tel" autoComplete="tel" pattern={'\\+?[0-9]{7,15}'}
                    title="Use 7 to 15 digits with an optional leading +" required />
                </label>
              </div>
            )}
            <label>Password
              <input name="password" type="password" autoComplete={isSignup ? 'new-password' : 'current-password'}
                minLength={isSignup ? 8 : undefined} maxLength={72} required />
              {isSignup && <span className="field-hint">Use at least 8 characters.</span>}
            </label>
            <button className="primary-button" type="submit" disabled={busy}>
              {busy ? 'Please wait…' : isSignup ? 'Create account' : 'Sign in'}
            </button>
          </form>
        </div>
      </section>
    </main>
  )
}
