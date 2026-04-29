export const PUBLIC_PATHS = ['/login', '/register']
export const ADMIN_PATHS = ['/admin']

export function isAdminUser(user = {}) {
  return String(user?.role || '').toUpperCase() === 'ADMIN'
}

function normalizeSession(input) {
  if (typeof input === 'boolean') {
    return {
      user: input ? { id: 1 } : {},
      token: input ? 'token' : ''
    }
  }

  return {
    user: input?.user || {},
    token: input?.token || ''
  }
}

function isAuthenticated(session) {
  return Boolean(session.user?.id && session.token)
}

export function canAccessRoute(path, sessionInput) {
  if (PUBLIC_PATHS.includes(path)) {
    return true
  }

  const session = normalizeSession(sessionInput)
  if (!isAuthenticated(session)) {
    return false
  }

  if (ADMIN_PATHS.includes(path)) {
    return isAdminUser(session.user)
  }

  return true
}
