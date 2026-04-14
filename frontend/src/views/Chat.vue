<template>
  <div class="chat-container">
    <el-row :gutter="20" style="height: 100%;">
      <el-col :span="6">
        <el-card class="contacts-card">
          <h3>联系人</h3>
          <el-input v-model="searchKeyword" placeholder="搜索联系人" style="margin-bottom: 10px;"></el-input>
          <div class="contacts-list">
            <div
              v-for="contact in filteredContacts"
              :key="contact.userId"
              class="contact-item"
              :class="{ active: selectedContact?.userId === contact.userId }"
              @click="selectContact(contact)"
            >
              <div class="contact-avatar">{{ contact.realName?.[0] || contact.username?.[0] }}</div>
              <div class="contact-info">
                <div class="contact-name">{{ contact.realName || contact.username }}</div>
                <div class="contact-last-msg">{{ contact.lastMessage?.content || '暂无消息' }}</div>
              </div>
              <el-badge v-if="contact.unreadCount > 0" :value="contact.unreadCount" class="unread-badge"></el-badge>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card class="chat-card" v-if="selectedContact">
          <div class="chat-header">
            <h3>{{ selectedContact.realName || selectedContact.username }}</h3>
          </div>
          <div class="messages-container" ref="messagesContainer">
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ 'message-right': message.senderId === currentUser.id }"
            >
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.createTime) }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              @keyup.ctrl.enter="sendMessage"
            ></el-input>
            <el-button type="primary" @click="sendMessage" style="margin-top: 10px;">发送</el-button>
          </div>
        </el-card>
        <el-empty v-else description="请选择一个联系人开始聊天"></el-empty>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Chat',
  setup() {
    const route = useRoute()
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const contacts = ref([])
    const selectedContact = ref(null)
    const messages = ref([])
    const newMessage = ref('')
    const searchKeyword = ref('')
    const messagesContainer = ref(null)

    const filteredContacts = computed(() => {
      if (!searchKeyword.value) return contacts.value
      return contacts.value.filter(c =>
        (c.realName || c.username).toLowerCase().includes(searchKeyword.value.toLowerCase())
      )
    })

    const loadContacts = async () => {
      try {
        const res = await axios.get(`/message/contacts/${currentUser.id}`)
        if (res.code === 0) {
          contacts.value = res.data
        }
      } catch (error) {
        console.error('加载联系人失败:', error)
      }
    }

    const selectContact = async (contact) => {
      selectedContact.value = contact
      try {
        const res = await axios.get('/message/conversation', {
          params: {
            userId1: currentUser.id,
            userId2: contact.userId
          }
        })
        if (res.code === 0) {
          messages.value = res.data
          scrollToBottom()
        }
      } catch (error) {
        ElMessage.error('加载消息失败')
      }
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim() || !selectedContact.value) return
      try {
        const res = await axios.post('/message/send', {
          senderId: currentUser.id,
          receiverId: selectedContact.value.userId,
          content: newMessage.value,
          messageType: 'text'
        })
        if (res.code === 0) {
          messages.value.push(res.data)
          newMessage.value = ''
          scrollToBottom()
          loadContacts()
        }
      } catch (error) {
        ElMessage.error('发送失败')
      }
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const formatTime = (timeStr) => {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN')
    }

    watch(messages, () => {
      scrollToBottom()
    })

    onMounted(async () => {
      await loadContacts()
      // 如果URL中有userId参数，自动选择该联系人
      const userId = route.query.userId
      if (userId) {
        const contact = contacts.value.find(c => c.userId === Number(userId))
        if (contact) {
          selectContact(contact)
        } else {
          // 如果联系人列表中不存在，尝试获取用户信息并创建临时联系人
          try {
            const res = await axios.get(`/user/${userId}`)
            if (res.code === 0) {
              const user = res.data
              const tempContact = {
                userId: user.id,
                username: user.username,
                realName: user.realName,
                avatar: user.avatar,
                lastMessage: null,
                unreadCount: 0
              }
              contacts.value.push(tempContact)
              selectContact(tempContact)
            }
          } catch (error) {
            console.error('加载用户信息失败:', error)
          }
        }
      }
    })

    return {
      currentUser,
      contacts,
      selectedContact,
      messages,
      newMessage,
      searchKeyword,
      messagesContainer,
      filteredContacts,
      selectContact,
      sendMessage,
      formatTime
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 120px);
}

.contacts-card {
  height: 100%;
}

.contacts-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
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
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 10px;
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

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f9f9f9;
  margin-bottom: 10px;
}

.message-item {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.message-item.message-right {
  align-items: flex-end;
}

.message-content {
  padding: 10px 15px;
  border-radius: 10px;
  max-width: 70%;
  background-color: white;
  word-wrap: break-word;
}

.message-right .message-content {
  background-color: #409EFF;
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

