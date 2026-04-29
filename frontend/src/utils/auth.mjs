import { reactive } from 'vue'

const USER_KEY = 'user'
const TOKEN_KEY = 'token'

function safeParseUser(rawUser) {
  if (!rawUser) {
    return {}
  }

  try {
    return JSON.parse(rawUser) || {}
  } catch {
    return {}
  }
}

function createFallbackStorage() {
  const values = new Map()

  return {
    getItem(key) {
      return values.has(key) ? values.get(key) : null
    },
    setItem(key, value) {
      values.set(key, String(value))
    },
    removeItem(key) {
      values.delete(key)
    }
  }
}

function getDefaultStorage() {
  if (typeof localStorage !== 'undefined') {
    return localStorage
  }

  return createFallbackStorage()
}

export function getStoredSession(storage = getDefaultStorage()) {
  return {
    user: safeParseUser(storage.getItem(USER_KEY)),
    token: storage.getItem(TOKEN_KEY) || ''
  }
}

export function hasValidSession(storage = getDefaultStorage()) {
  const session = getStoredSession(storage)
  return Boolean(session.user?.id && session.token)
}

export function setStoredSession(storage, user, token) {
  storage.setItem(USER_KEY, JSON.stringify(user))
  storage.setItem(TOKEN_KEY, token)
}

export function clearStoredSession(storage = getDefaultStorage()) {
  storage.removeItem(USER_KEY)
  storage.removeItem(TOKEN_KEY)
}

export function createMemoryStorage() {
  return createFallbackStorage()
}

const sessionState = reactive({
  user: {},
  token: ''
})

export function useSessionState() {
  return sessionState
}

export function syncSessionState(storage = getDefaultStorage()) {
  const next = getStoredSession(storage)
  sessionState.user = next.user
  sessionState.token = next.token
}

export function saveSession(user, token, storage = getDefaultStorage()) {
  setStoredSession(storage, user, token)
  syncSessionState(storage)
}

export function clearSession(storage = getDefaultStorage()) {
  clearStoredSession(storage)
  syncSessionState(storage)
}

if (typeof window !== 'undefined') {
  syncSessionState()
}
