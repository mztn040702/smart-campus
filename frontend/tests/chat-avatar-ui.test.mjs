import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const chatSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Chat.vue'),
  'utf8'
)

test('chat view renders avatars for contacts, selected contact, messages, requests, and search results', () => {
  assert.match(chatSource, /:src="resolveAvatarUrl\(contact\.avatar\)"/)
  assert.match(chatSource, /:src="resolveAvatarUrl\(user\.avatar\)"/)
  assert.match(chatSource, /:src="resolveAvatarUrl\(request\.avatar\)"/)
  assert.match(chatSource, /:src="resolveAvatarUrl\(selectedContact\.avatar\)"/)
  assert.match(chatSource, /:src="resolveAvatarUrl\(getMessageAvatar\(message\)\)"/)
})

test('chat view exposes avatar helpers and current user avatar fallback', () => {
  assert.match(chatSource, /const resolveAvatarUrl = \(url\) =>/)
  assert.match(chatSource, /const getAvatarText = \(contact\) =>/)
  assert.match(chatSource, /const getMessageAvatar = \(message\) =>/)
  assert.match(chatSource, /return currentUser\.avatar \|\| ''/)
})
