export const PUBLIC_PATHS = ['/login', '/register']

export function canAccessRoute(path, isAuthenticated) {
  if (PUBLIC_PATHS.includes(path)) {
    return true
  }

  return isAuthenticated
}
