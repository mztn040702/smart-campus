import test from 'node:test'
import assert from 'node:assert/strict'
import {
  createMemoryStorage,
  getStoredSession,
  hasValidSession,
  setStoredSession,
  clearStoredSession
} from '../src/utils/auth.mjs'
import { canAccessRoute, PUBLIC_PATHS } from '../src/router/access.mjs'

test('stored session becomes valid after saving token and user', () => {
  const storage = createMemoryStorage()

  assert.equal(hasValidSession(storage), false)

  setStoredSession(storage, { id: 7, username: 'alice' }, 'jwt-token')

  assert.deepEqual(getStoredSession(storage), {
    user: { id: 7, username: 'alice' },
    token: 'jwt-token'
  })
  assert.equal(hasValidSession(storage), true)
})

test('clearing storage removes auth state', () => {
  const storage = createMemoryStorage()
  setStoredSession(storage, { id: 7, username: 'alice' }, 'jwt-token')

  clearStoredSession(storage)

  assert.equal(hasValidSession(storage), false)
  assert.deepEqual(getStoredSession(storage), {
    user: {},
    token: ''
  })
})

test('route access allows public pages and blocks protected pages without auth', () => {
  assert.equal(PUBLIC_PATHS.includes('/login'), true)
  assert.equal(canAccessRoute('/login', false), true)
  assert.equal(canAccessRoute('/register', false), true)
  assert.equal(canAccessRoute('/home', false), false)
  assert.equal(canAccessRoute('/product', false), false)
})

test('route access allows business pages with auth', () => {
  assert.equal(canAccessRoute('/home', true), true)
  assert.equal(canAccessRoute('/product', true), true)
  assert.equal(canAccessRoute('/job', true), true)
  assert.equal(canAccessRoute('/help', true), true)
  assert.equal(canAccessRoute('/chat', true), true)
  assert.equal(canAccessRoute('/profile', true), true)
})
