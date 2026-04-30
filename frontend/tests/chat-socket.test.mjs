import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildChatWebSocketUrl,
  shouldDisplayMessageInActiveConversation,
  buildContactsFromUsers,
  getContactDisplayName,
  matchesContactSearch,
  mergeContactsWithMessageMeta,
  incrementUnreadMap,
  resetUnreadForContact,
  buildFriendContacts,
  mapIncomingFriendRequests,
  canOpenConversationWith
} from '../src/utils/chatSocket.mjs'

test('buildChatWebSocketUrl uses ws for http origins and appends encoded token', () => {
  const url = buildChatWebSocketUrl(
    { protocol: 'http:', host: 'localhost:3000' },
    'jwt token'
  )

  assert.equal(url, 'ws://localhost:3000/ws/chat?token=jwt%20token')
})

test('buildChatWebSocketUrl uses wss for https origins', () => {
  const url = buildChatWebSocketUrl(
    { protocol: 'https:', host: 'campus.example.com' },
    'abc123'
  )

  assert.equal(url, 'wss://campus.example.com/ws/chat?token=abc123')
})

test('shouldDisplayMessageInActiveConversation matches both inbound and outbound messages', () => {
  assert.equal(
    shouldDisplayMessageInActiveConversation(
      { senderId: 1, receiverId: 2 },
      1,
      2
    ),
    true
  )

  assert.equal(
    shouldDisplayMessageInActiveConversation(
      { senderId: 2, receiverId: 1 },
      1,
      2
    ),
    true
  )

  assert.equal(
    shouldDisplayMessageInActiveConversation(
      { senderId: 3, receiverId: 1 },
      1,
      2
    ),
    false
  )
})

test('buildContactsFromUsers filters out the current user and preserves other users', () => {
  const contacts = buildContactsFromUsers(
    [
      { id: 1, username: 'student1', realName: 'Student One' },
      { id: 2, username: 'student2', realName: 'Student Two' },
      { id: 3, username: 'admin', realName: 'Administrator' }
    ],
    { id: 1 }
  )

  assert.deepEqual(
    contacts.map((contact) => contact.userId),
    [2, 3]
  )
  assert.equal(contacts[0].lastMessage, null)
  assert.equal(contacts[0].unreadCount, 0)
})

test('getContactDisplayName uses nickname then username then email then id', () => {
  assert.equal(
    getContactDisplayName({ nickname: 'Nick', username: 'student2', email: 's2@example.com', id: 2 }),
    'Nick'
  )
  assert.equal(
    getContactDisplayName({ username: 'student2', email: 's2@example.com', id: 2 }),
    'student2'
  )
  assert.equal(
    getContactDisplayName({ email: 's2@example.com', id: 2 }),
    's2@example.com'
  )
  assert.equal(
    getContactDisplayName({ id: 2 }),
    '2'
  )
})

test('matchesContactSearch checks username nickname and email', () => {
  const contact = {
    username: 'student2',
    nickname: 'monitor',
    email: 'student2@example.com'
  }

  assert.equal(matchesContactSearch(contact, 'stud'), true)
  assert.equal(matchesContactSearch(contact, 'moni'), true)
  assert.equal(matchesContactSearch(contact, 'example'), true)
  assert.equal(matchesContactSearch(contact, 'admin'), false)
})

test('mergeContactsWithMessageMeta keeps local unread counts while updating last message', () => {
  const merged = mergeContactsWithMessageMeta(
    [
      { userId: 2, username: 'student2', unreadCount: 0, lastMessage: null },
      { userId: 3, username: 'student3', unreadCount: 0, lastMessage: null }
    ],
    [
      { userId: 2, unreadCount: 5, lastMessage: { content: 'old' } }
    ],
    { 2: 0, 3: 2 }
  )

  assert.equal(merged[0].lastMessage.content, 'old')
  assert.equal(merged[0].unreadCount, 0)
  assert.equal(merged[1].unreadCount, 2)
})

test('incrementUnreadMap increases unread only for inactive inbound messages', () => {
  assert.deepEqual(
    incrementUnreadMap({}, { senderId: 2, receiverId: 1 }, 1, null),
    { 2: 1 }
  )

  assert.deepEqual(
    incrementUnreadMap({ 2: 1 }, { senderId: 2, receiverId: 1 }, 1, 2),
    { 2: 0 }
  )

  assert.deepEqual(
    incrementUnreadMap({ 2: 1 }, { senderId: 1, receiverId: 2 }, 1, 2),
    { 2: 1 }
  )
})

test('resetUnreadForContact clears the selected contact unread count', () => {
  assert.deepEqual(
    resetUnreadForContact({ 2: 3, 3: 1 }, 2),
    { 2: 0, 3: 1 }
  )
})

test('buildFriendContacts maps accepted friends into chat contacts', () => {
  const contacts = buildFriendContacts([
    { id: 2, username: 'student2', realName: 'Student Two' },
    { id: 3, username: 'student3', realName: 'Student Three' }
  ])

  assert.deepEqual(
    contacts.map((contact) => contact.userId),
    [2, 3]
  )
  assert.equal(contacts[0].unreadCount, 0)
  assert.equal(contacts[1].lastMessage, null)
})

test('mapIncomingFriendRequests keeps only request fields needed by Chat view', () => {
  const requests = mapIncomingFriendRequests([
    { id: 10, requesterId: 2, username: 'student2', realName: 'Student Two', status: 'PENDING' }
  ])

  assert.equal(requests[0].id, 10)
  assert.equal(requests[0].requesterId, 2)
  assert.equal(requests[0].username, 'student2')
})

test('canOpenConversationWith allows only known friend contacts', () => {
  assert.equal(canOpenConversationWith([{ userId: 2 }, { userId: 3 }], 2), true)
  assert.equal(canOpenConversationWith([{ userId: 2 }, { userId: 3 }], 4), false)
})
