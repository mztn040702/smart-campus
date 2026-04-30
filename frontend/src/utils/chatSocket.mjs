export function buildChatWebSocketUrl(locationLike, token) {
  const protocol = locationLike.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${locationLike.host}/ws/chat?token=${encodeURIComponent(token)}`
}

export function shouldDisplayMessageInActiveConversation(message, currentUserId, activeContactId) {
  if (!activeContactId) {
    return false
  }

  const outbound = message.senderId === currentUserId && message.receiverId === activeContactId
  const inbound = message.senderId === activeContactId && message.receiverId === currentUserId
  return outbound || inbound
}

export function getContactDisplayName(contact) {
  return String(
    contact.nickname ||
    contact.realName ||
    contact.username ||
    contact.email ||
    contact.id ||
    contact.userId ||
    'Unknown'
  )
}

export function matchesContactSearch(contact, keyword) {
  const query = (keyword || '').trim().toLowerCase()
  if (!query) {
    return true
  }

  return [contact.username, contact.nickname, contact.realName, contact.email]
    .filter(Boolean)
    .some((value) => String(value).toLowerCase().includes(query))
}

export function buildContactsFromUsers(users, currentUser) {
  return users
    .filter((user) => user.id !== currentUser.id)
    .map((user) => ({
      ...user,
      userId: user.id,
      lastMessage: null,
      unreadCount: 0
    }))
}

export function buildFriendContacts(friends) {
  return friends.map((friend) => ({
    ...friend,
    userId: friend.id,
    lastMessage: null,
    unreadCount: 0
  }))
}

export function mergeContactsWithMessageMeta(contacts, metadata, unreadMap = {}) {
  const metaByUserId = new Map(metadata.map((contact) => [contact.userId, contact]))

  return contacts.map((contact) => {
    const meta = metaByUserId.get(contact.userId)
    return {
      ...contact,
      ...(meta ? { lastMessage: meta.lastMessage ?? contact.lastMessage } : {}),
      unreadCount: unreadMap[contact.userId] ?? contact.unreadCount ?? meta?.unreadCount ?? 0
    }
  })
}

export function incrementUnreadMap(unreadMap, message, currentUserId, activeContactId) {
  const nextUnreadMap = { ...unreadMap }
  const isInbound = message.receiverId === currentUserId

  if (!isInbound) {
    return nextUnreadMap
  }

  const senderId = message.senderId
  if (senderId === activeContactId) {
    nextUnreadMap[senderId] = 0
    return nextUnreadMap
  }

  nextUnreadMap[senderId] = (nextUnreadMap[senderId] || 0) + 1
  return nextUnreadMap
}

export function resetUnreadForContact(unreadMap, contactId) {
  return {
    ...unreadMap,
    [contactId]: 0
  }
}

export function mapIncomingFriendRequests(requests) {
  return requests.map((request) => ({
    id: request.id,
    requesterId: request.requesterId,
    username: request.username,
    realName: request.realName,
    avatar: request.avatar,
    status: request.status,
    createTime: request.createTime
  }))
}

export function canOpenConversationWith(contacts, userId) {
  return contacts.some((contact) => contact.userId === userId)
}
