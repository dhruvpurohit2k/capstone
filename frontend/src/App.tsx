import { useCallback, useEffect, useState } from 'react'
import { getSessionToken, refreshAccessToken, setSessionToken } from './api/client.ts'
import AuthPage from './features/auth/AuthPage.tsx'
import PolicyWorkspace from './features/policy/PolicyWorkspace.tsx'
import './App.css'

export default function App() {
  const [token, setToken] = useState(getSessionToken)
  const [checkingSession, setCheckingSession] = useState(() => !getSessionToken())

  useEffect(() => {
    if (!checkingSession) return
    let active = true

    refreshAccessToken().then((value) => {
      if (active) setToken(value)
    }).catch(() => {
      // The sign-in form is still available when the session check fails.
    }).finally(() => {
      if (active) setCheckingSession(false)
    })

    return () => { active = false }
  }, [checkingSession])

  function signIn(value: string) {
    setSessionToken(value)
    setToken(value)
  }

  const signOut = useCallback(() => {
    setSessionToken(null)
    setToken(null)
  }, [])

  if (checkingSession) return <main className="auth-panel"><p role="status">Checking your session…</p></main>
  return token ? <PolicyWorkspace token={token} onSignOut={signOut} /> : <AuthPage onLogin={signIn} />
}
