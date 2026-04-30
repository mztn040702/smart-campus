<template>
  <div class="chat-container">
    <el-row :gutter="20" class="chat-layout">
      <el-col :span="7">
        <el-card class="contacts-card">
          <div class="contacts-header">
            <h3>好友列表</h3>
            <el-tag :type="connectionTagType" size="small">{{ connectionStatusLabel }}</el-tag>
          </div>

          <el-input
            v-model="searchKeyword"
            placeholder="搜索好友"
            style="margin-bottom: 10px;"
          />

          <div class="contacts-list">
            <div
              v-for="contact in filteredContacts"
              :key="contact.userId"
              class="contact-item"
              :class="{ active: selectedContact?.userId === contact.userId }"
              @click="selectContact(contact)"
            >
              <div class="contact-avatar avatar-shell">
                <img
                  v-if="contact.avatar"
                  :src="resolveAvatarUrl(contact.avatar)"
                  alt="contact avatar"
                  class="avatar-image"
                />
                <span v-else>{{ getAvatarText(contact) }}</span>
              </div>
              <div class="contact-info">
                <div class="contact-name">{{ getContactName(contact) }}</div>
                <div class="contact-last-msg">{{ contact.lastMessage?.content || '暂无消息' }}</div>
              </div>
              <el-badge
                v-if="contact.unreadCount > 0"
                :value="contact.unreadCount"
                class="unread-badge"
              />
            </div>
          </div>

          <div class="friend-panel">
            <h4>添加好友</h4>
            <div class="friend-search-row">
              <el-input
                v-model="friendSearchKeyword"
                placeholder="搜索用户"
                @keyup.enter="searchUsers"
              />
              <el-button type="primary" @click="searchUsers">搜索</el-button>
            </div>
            <div class="friend-search-results">
              <div
                v-for="user in friendSearchResults"
                :key="user.id"
                class="friend-row"
              >
                <div class="friend-user">
                  <div class="mini-avatar avatar-shell">
                    <img
                      v-if="user.avatar"
                      :src="resolveAvatarUrl(user.avatar)"
                      alt="user avatar"
                      class="avatar-image"
                    />
                    <span v-else>{{ getAvatarText(user) }}</span>
                  </div>
                  <span>{{ getContactName(user) }}</span>
                </div>
                <el-button size="small" type="primary" @click="sendFriendRequest(user.id)">添加</el-button>
              </div>
            </div>
          </div>

          <div class="friend-panel">
            <h4>收到的好友申请</h4>
            <div v-if="incomingRequests.length === 0" class="empty-tip">暂无待处理申请</div>
            <div
              v-for="request in incomingRequests"
              :key="request.id"
              class="friend-row"
            >
              <div class="friend-user">
                <div class="mini-avatar avatar-shell">
                  <img
                    v-if="request.avatar"
                    :src="resolveAvatarUrl(request.avatar)"
                    alt="request avatar"
                    class="avatar-image"
                  />
                  <span v-else>{{ getAvatarText(request) }}</span>
                </div>
                <span>{{ getContactName(request) }}</span>
              </div>
              <div class="friend-actions">
                <el-button size="small" type="success" @click="acceptFriendRequest(request.id)">接受</el-button>
                <el-button size="small" @click="rejectFriendRequest(request.id)">拒绝</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="17">
        <el-card v-if="selectedContact" class="chat-card">
          <div class="chat-header">
            <div class="chat-header-user">
              <div class="header-avatar avatar-shell">
                <img
                  v-if="selectedContact.avatar"
                  :src="resolveAvatarUrl(selectedContact.avatar)"
                  alt="selected contact avatar"
                  class="avatar-image"
                />
                <span v-else>{{ getAvatarText(selectedContact) }}</span>
              </div>
              <h3>{{ getContactName(selectedContact) }}</h3>
            </div>
          </div>
          <div ref="messagesContainer" class="messages-container">
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ 'message-right': message.senderId === currentUser.id }"
            >
              <div class="message-row">
                <div class="message-avatar avatar-shell">
                  <img
                    v-if="getMessageAvatar(message)"
                    :src="resolveAvatarUrl(getMessageAvatar(message))"
                    alt="message avatar"
                    class="avatar-image"
                  />
                  <span v-else>{{ getAvatarText(getMessageUser(message)) }}</span>
                </div>
                <div class="message-body">
                  <div class="message-content">{{ message.content }}</div>
                  <div class="message-time">{{ formatTime(message.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              placeholder="按 Ctrl+Enter 发送消息"
              @keyup.ctrl.enter="sendMessage"
            />
            <el-button type="primary" @click="sendMessage" style="margin-top: 10px;">发送</el-button>
          </div>
        </el-card>
        <el-empty v-else description="请选择一个好友开始聊天" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'
import {
  buildChatWebSocketUrl,
  shouldDisplayMessageInActiveConversation,
  getContactDisplayName,
  matchesContactSearch,
  mergeContactsWithMessageMeta,
  incrementUnreadMap,
  resetUnreadForContact,
  buildFriendContacts,
  mapIncomingFriendRequests,
  canOpenConversationWith
} from '../utils/chatSocket.mjs'

export default {
  name: 'Chat',
  setup() {
    const route = useRoute()
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const token = localStorage.getItem('token') || ''
    const contacts = ref([])
    const selectedContact = ref(null)
    const messages = ref([])
    const newMessage = ref('')
    const searchKeyword = ref('')
    const friendSearchKeyword = ref('')
    const friendSearchResults = ref([])
    const incomingRequests = ref([])
    const messagesContainer = ref(null)
    const socket = ref(null)
    const socketReady = ref(false)
    const connectionStatus = ref('connecting')
    const unreadMap = ref({})

    const filteredContacts = computed(() => {
      return contacts.value.filter((contact) => matchesContactSearch(contact, searchKeyword.value))
    })

    const connectionStatusLabel = computed(() => {
      const labels = {
        connecting: '连接中',
        connected: '已连接',
        disconnected: '已断开',
        error: '认证失败'
      }
      return labels[connectionStatus.value] || connectionStatus.value
    })

    const connectionTagType = computed(() => {
      const tags = {
        connecting: 'warning',
        connected: 'success',
        disconnected: 'info',
        error: 'danger'
      }
      return tags[connectionStatus.value] || 'info'
    })

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const resolveAvatarUrl = (url) => {
      if (!url) {
        return ''
      }
      return url.startsWith('http') ? url : url
    }

    const getAvatarText = (contact) => {
      return getContactDisplayName(contact).slice(0, 1).toUpperCase()
    }

    const getContactName = (contact) => {
      return getContactDisplayName(contact)
    }

    const getMessageUser = (message) => {
      if (message.senderId === currentUser.id) {
        return currentUser
      }
      return selectedContact.value || {}
    }

    const getMessageAvatar = (message) => {
      if (message.senderId === currentUser.id) {
        return currentUser.avatar || ''
      }
      return selectedContact.value?.avatar || ''
    }

    const applyUnreadState = () => {
      contacts.value = contacts.value.map((contact) => ({
        ...contact,
        unreadCount: unreadMap.value[contact.userId] ?? 0
      }))
    }

    const loadIncomingRequests = async () => {
      try {
        const res = await axios.get('/friends/requests/incoming')
        if (res.code === 0) {
          incomingRequests.value = mapIncomingFriendRequests(res.data)
        }
      } catch (error) {
        console.error('加载好友申请失败：', error)
      }
    }

    const mergeContactMeta = async () => {
      try {
        const res = await axios.get(`/message/contacts/${currentUser.id}`)
        if (res.code !== 0) {
          return
        }

        contacts.value = mergeContactsWithMessageMeta(contacts.value, res.data, unreadMap.value)
      } catch (error) {
        console.error('加载会话摘要失败：', error)
      }
    }

    const loadContacts = async () => {
      try {
        const res = await axios.get('/friends')
        if (res.code === 0) {
          contacts.value = buildFriendContacts(res.data)
          const contactMetaRes = await axios.get(`/message/contacts/${currentUser.id}`)
          if (contactMetaRes.code === 0) {
            unreadMap.value = contactMetaRes.data.reduce((accumulator, contact) => {
              accumulator[contact.userId] = contact.unreadCount || 0
              return accumulator
            }, {})
            contacts.value = mergeContactsWithMessageMeta(contacts.value, contactMetaRes.data, unreadMap.value)
          } else {
            applyUnreadState()
          }
        }
      } catch (error) {
        console.error('加载好友列表失败：', error)
      }
    }

    const loadConversation = async (contact) => {
      const res = await axios.get('/message/conversation', {
        params: {
          userId1: currentUser.id,
          userId2: contact.userId
        }
      })

      if (res.code === 0) {
        messages.value = res.data
        scrollToBottom()
      } else {
        throw new Error(res.msg || '加载聊天记录失败')
      }
    }

    const selectContact = async (contact) => {
      selectedContact.value = contact
      unreadMap.value = resetUnreadForContact(unreadMap.value, contact.userId)
      applyUnreadState()
      try {
        await loadConversation(contact)
      } catch (error) {
        ElMessage.error(error.message || '加载聊天记录失败')
      }
    }

    const ensureRouteContact = async () => {
      const userId = Number(route.query.userId)
      if (!userId || !canOpenConversationWith(contacts.value, userId)) {
        return
      }

      const existingContact = contacts.value.find((contact) => contact.userId === userId)
      if (existingContact) {
        await selectContact(existingContact)
      }
    }

    const handleIncomingMessage = async (message) => {
      if (
        shouldDisplayMessageInActiveConversation(
          message,
          currentUser.id,
          selectedContact.value?.userId
        )
      ) {
        messages.value = [...messages.value, message]
      }

      unreadMap.value = incrementUnreadMap(
        unreadMap.value,
        message,
        currentUser.id,
        selectedContact.value?.userId
      )
      await mergeContactMeta()
      applyUnreadState()
    }

    const connectWebSocket = () => {
      if (!token) {
        connectionStatus.value = 'error'
        ElMessage.error('缺少登录凭证，无法连接聊天服务')
        return
      }

      connectionStatus.value = 'connecting'
      const url = buildChatWebSocketUrl(window.location, token)
      const ws = new WebSocket(url)
      socket.value = ws

      ws.onopen = () => {
        socketReady.value = true
        connectionStatus.value = 'connected'
      }

      ws.onmessage = async (event) => {
        const payload = JSON.parse(event.data)
        if (payload.type === 'error') {
          ElMessage.error(payload.message || '聊天连接异常')
          return
        }

        await handleIncomingMessage(payload)
      }

      ws.onerror = () => {
        connectionStatus.value = 'error'
      }

      ws.onclose = () => {
        socketReady.value = false
        if (connectionStatus.value !== 'error') {
          connectionStatus.value = 'disconnected'
        }
      }
    }

    const sendMessage = () => {
      if (!selectedContact.value) {
        return
      }

      const content = newMessage.value.trim()
      if (!content) {
        return
      }

      if (!socket.value || !socketReady.value) {
        ElMessage.error('聊天连接未建立')
        return
      }

      socket.value.send(JSON.stringify({
        receiverId: selectedContact.value.userId,
        content,
        messageType: 'text'
      }))
      newMessage.value = ''
    }

    const searchUsers = async () => {
      try {
        const res = await axios.get('/friends/search', {
          params: { keyword: friendSearchKeyword.value }
        })
        if (res.code === 0) {
          friendSearchResults.value = res.data
        } else {
          ElMessage.error(res.msg || '搜索用户失败')
        }
      } catch (error) {
        ElMessage.error('搜索用户失败')
      }
    }

    const sendFriendRequest = async (receiverId) => {
      try {
        const res = await axios.post('/friends/request', { receiverId })
        if (res.code === 0) {
          ElMessage.success('好友申请已发送')
          friendSearchResults.value = friendSearchResults.value.filter((user) => user.id !== receiverId)
        } else {
          ElMessage.error(res.msg || '发送好友申请失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '发送好友申请失败')
      }
    }

    const acceptFriendRequest = async (requestId) => {
      try {
        const res = await axios.post(`/friends/requests/${requestId}/accept`)
        if (res.code === 0) {
          ElMessage.success('已接受好友申请')
          await Promise.all([loadIncomingRequests(), loadContacts()])
        } else {
          ElMessage.error(res.msg || '接受好友申请失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '接受好友申请失败')
      }
    }

    const rejectFriendRequest = async (requestId) => {
      try {
        const res = await axios.post(`/friends/requests/${requestId}/reject`)
        if (res.code === 0) {
          ElMessage.success('已拒绝好友申请')
          await loadIncomingRequests()
        } else {
          ElMessage.error(res.msg || '拒绝好友申请失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '拒绝好友申请失败')
      }
    }

    const formatTime = (timeStr) => {
      if (!timeStr) {
        return ''
      }

      return new Date(timeStr).toLocaleString('zh-CN')
    }

    watch(messages, () => {
      scrollToBottom()
    })

    onMounted(async () => {
      connectWebSocket()
      await Promise.all([loadContacts(), loadIncomingRequests()])
      await ensureRouteContact()
    })

    onBeforeUnmount(() => {
      if (socket.value) {
        socket.value.close()
      }
    })

    return {
      acceptFriendRequest,
      connectionStatusLabel,
      connectionTagType,
      currentUser,
      filteredContacts,
      formatTime,
      friendSearchKeyword,
      friendSearchResults,
      getAvatarText,
      getContactName,
      getMessageAvatar,
      getMessageUser,
      incomingRequests,
      messages,
      messagesContainer,
      newMessage,
      rejectFriendRequest,
      resolveAvatarUrl,
      searchKeyword,
      searchUsers,
      selectedContact,
      selectContact,
      sendFriendRequest,
      sendMessage
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 120px);
}

.chat-layout {
  height: 100%;
}

.contacts-card {
  height: 100%;
  overflow-y: auto;
}

.contacts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.contacts-list {
  max-height: 240px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 5px;
  margin-bottom: 5px;
  position: relative;
}

.contact-item:hover {
  background-color: #f5f5f5;
}

.contact-item.active {
  background-color: #e6f7ff;
}

.contact-avatar {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

.avatar-shell {
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.contact-info {
  flex: 1;
}

.contact-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.contact-last-msg {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  position: absolute;
  right: 10px;
}

.friend-panel {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
  margin-top: 12px;
}

.friend-panel h4 {
  margin-bottom: 10px;
}

.friend-search-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.friend-search-results,
.friend-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 0;
}

.friend-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.mini-avatar {
  width: 32px;
  height: 32px;
  font-size: 12px;
}

.empty-tip {
  color: #909399;
  font-size: 13px;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.chat-header-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 44px;
  height: 44px;
  font-size: 16px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f9f9f9;
  margin-bottom: 10px;
}

.message-item {
  margin-bottom: 15px;
}

.message-item.message-right {
  align-items: flex-end;
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.message-right .message-row {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  font-size: 14px;
}

.message-body {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-content {
  padding: 10px 15px;
  border-radius: 10px;
  background-color: white;
  word-wrap: break-word;
}

.message-right .message-content {
  background-color: #409eff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.chat-input {
  border-top: 1px solid #eee;
  padding-top: 10px;
}
</style>
