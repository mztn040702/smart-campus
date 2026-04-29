import test from 'node:test'
import assert from 'node:assert/strict'
import { canAccessRoute, PUBLIC_PATHS, isAdminUser } from '../src/router/access.mjs'

test('admin helper detects admin users only', () => {
  assert.equal(isAdminUser({ role: 'ADMIN' }), true)
  assert.equal(isAdminUser({ role: 'admin' }), true)
  assert.equal(isAdminUser({ role: 'USER' }), false)
  assert.equal(isAdminUser({}), false)
})

test('public pages stay open without token', () => {
  assert.equal(PUBLIC_PATHS.includes('/login'), true)
  assert.equal(canAccessRoute('/login', { user: {}, token: '' }), true)
  assert.equal(canAccessRoute('/register', { user: {}, token: '' }), true)
})

test('authenticated students can access business pages but not admin page', () => {
  const session = {
    user: { id: 1, username: 'student1', role: 'USER' },
    token: 'jwt-token'
  }

  assert.equal(canAccessRoute('/home', session), true)
  assert.equal(canAccessRoute('/product', session), true)
  assert.equal(canAccessRoute('/chat', session), true)
  assert.equal(canAccessRoute('/admin', session), false)
})

test('authenticated admin can access admin page', () => {
  const session = {
    user: { id: 99, username: 'admin', role: 'ADMIN' },
    token: 'jwt-token'
  }

  assert.equal(canAccessRoute('/admin', session), true)
})
