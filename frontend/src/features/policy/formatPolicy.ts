const currency = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' })
const dateFormat = new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })

export function formatCurrency(amount: number) {
  return currency.format(amount)
}

export function formatDate(date: string) {
  return dateFormat.format(new Date(date + 'T00:00:00'))
}

export function formatLabel(value: string) {
  const text = value.toLowerCase().replaceAll('_', ' ')
  return text.charAt(0).toUpperCase() + text.slice(1)
}
