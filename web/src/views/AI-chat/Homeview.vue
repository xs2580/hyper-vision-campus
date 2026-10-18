<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  chatStreamApi,
  createSessionApi,
  deleteSessionApi,
  listSessionsApi
} from '@/api/AIchat.js'

// 优先读 .env.local 中的 VITE_CHAT_ID，留空时回落到演示用的助手 ID
const DEFAULT_ASSISTANT_ID = import.meta.env.VITE_CHAT_ID || 'dfb23554ba5011f0aca5729aa2412eec'

// 响应式数据
const messages = ref([])
const inputText = ref('')
const isSending = ref(false)
const assistantChatId = ref(localStorage.getItem('ragflowChatId') || DEFAULT_ASSISTANT_ID)
const currentSessionId = ref('')
const currentUserId = ref('1')
const chatList = ref([])
const currentChatTitle = ref('我的会话')
const showScrollbar = ref(false)
const sessionCache = ref({})

// 移动端侧边栏控制
const showSidebar = ref(false)
const isMobile = ref(false)

const getAssistantId = () => assistantChatId.value || DEFAULT_ASSISTANT_ID

// 检测是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    showSidebar.value = false
  }
}

// 切换侧边栏
const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
}

// 关闭侧边栏
const closeSidebar = () => {
  showSidebar.value = false
}

// 点击外部区域关闭侧边栏
const handleClickOutside = (event) => {
  if (showSidebar.value && !event.target.closest('.sidebar') && !event.target.closest('.sidebar-btn')) {
    showSidebar.value = false
  }
}

// 滚动到底部（带节流）
let scrollTimer = null
const scrollToBottom = () => {
  if (scrollTimer) return
  scrollTimer = setTimeout(() => {
  nextTick(() => {
  const messagesContainer = document.querySelector('.messages-container')
  if (messagesContainer) {
    messagesContainer.scrollTop = messagesContainer.scrollHeight
  }
      scrollTimer = null
    })
  }, 50)
}

// 生成唯一ID
const generateMessageId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`

// 规范化服务端消息
const normalizeSessionMessages = (sessionMessages = []) => {
  return sessionMessages.map((msg, index) => {
    const rawId = msg.id || generateMessageId()
    const role = msg.role || msg.messageType || 'assistant'
    const createdAt = Number(msg.created_at ?? msg.create_time ?? msg.createTime ?? Date.now())
    const timestamp = Number.isFinite(createdAt)
      ? (createdAt > 1e12 ? createdAt : Math.round(createdAt * 1000))
      : Date.now()
    return {
      id: `${rawId}-${index}`,
      type: role.toLowerCase() === 'user' ? 'user' : 'ai',
      content: msg.content || msg.text || '',
      createTime: timestamp
    }
  })
}

const getSessionTitle = (messageList = [], fallback = '新对话') => {
  const lastUserMessage = messageList.filter(msg => msg.type === 'user').pop()
  if (!lastUserMessage) return fallback
  const text = lastUserMessage.content || ''
  return text.length > 20 ? `${text.substring(0, 20)}...` : text
}

const cacheSession = (session) => {
  if (!session?.id) return []
  const normalizedMessages = normalizeSessionMessages(session.messages || [])
  sessionCache.value[session.id] = {
    id: session.id,
    name: session.name || '新对话',
    chat_id: session.chat_id || getAssistantId(),
    update_time: session.update_time || Date.now(),
    messages: normalizedMessages
  }
  return normalizedMessages
}

const buildChatItem = (session) => {
  if (!session?.id) return null
  const normalizedMessages = cacheSession(session)
  return {
    sessionId: session.id,
    title: getSessionTitle(normalizedMessages, session.name || '新对话'),
    updateTime: session.update_time || Date.now()
  }
}

const makeSessionPreview = (session) => buildChatItem(session)

const updateSessionPreview = (sessionId) => {
  const cached = sessionCache.value[sessionId]
  if (!cached) return
  const title = getSessionTitle(cached.messages, cached.name || '新对话')
  const updateTime = cached.update_time || Date.now()
  cached.update_time = updateTime
  const existingIndex = chatList.value.findIndex(item => item.sessionId === sessionId)
  if (existingIndex === -1) {
    chatList.value.unshift({
      sessionId,
      title,
      updateTime
    })
  } else {
    chatList.value[existingIndex] = {
      sessionId,
      title,
      updateTime
    }
    if (existingIndex > 0) {
      const [item] = chatList.value.splice(existingIndex, 1)
      chatList.value.unshift(item)
    }
  }
  currentChatTitle.value = title
}

const showSession = (sessionId) => {
  if (!sessionId) {
    currentSessionId.value = ''
    messages.value = []
    currentChatTitle.value = '新对话'
    return
  }

  currentSessionId.value = sessionId
  const cached = sessionCache.value[sessionId]
  if (cached) {
    messages.value = cached.messages.map(m => ({ ...m }))
    currentChatTitle.value = getSessionTitle(messages.value, cached.name || '新对话')
  } else {
    messages.value = []
    currentChatTitle.value = '新对话'
  }
  nextTick(() => {
    scrollToBottom()
  })
}

const syncSessionMessages = (sessionId, moveToTop = true) => {
  if (!sessionId) return
  const cached = sessionCache.value[sessionId] || {}
  sessionCache.value[sessionId] = {
    ...cached,
    id: sessionId,
    chat_id: cached.chat_id || getAssistantId(),
    name: cached.name || currentChatTitle.value || '新对话',
    messages: messages.value.map(m => ({ ...m })),
    update_time: Date.now()
  }
  if (moveToTop) {
    updateSessionPreview(sessionId)
  }
}

const ensureSession = async () => {
  if (currentSessionId.value) return
  await createNewChat()
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

// 处理消息内容，将换行符转换为HTML
const formatMessageContent = (content) => {
  if (!content) return ''
  return content
    // 去掉知识库引用标记（例如 ##0$$）
    .replace(/##\d+\$\$/g, '')
    // 去掉 markdown 标记（# 和 *）
    .replace(/#+/g, '')
    .replace(/\*+/g, '')
    // 去掉截断提示语
    .replace(/由于长度的原因，回答被截断了，要继续吗？/g, '')
    .replace(/\n/g, '<br>')
    .replace(/\r\n/g, '<br>')
    .replace(/\r/g, '<br>')
}

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || isSending.value) return
  
  const userMessage = inputText.value.trim()
  inputText.value = ''
  isSending.value = true
  
  await ensureSession()

  if (!currentSessionId.value) {
    const errorMsg = '未找到有效会话，请稍后再试。'
    ElMessage.error(errorMsg)
    inputText.value = userMessage
    isSending.value = false
    return
  }

  const timestamp = Date.now()
  const userMsg = {
    id: generateMessageId(),
    type: 'user',
    content: userMessage,
    createTime: timestamp
  }
  messages.value.push(userMsg)
  scrollToBottom()
  syncSessionMessages(currentSessionId.value)

  const payload = {
    question: userMessage,
    sessionId: currentSessionId.value
  }

  let response
  try {
    response = await chatStreamApi(getAssistantId(), payload)
  } catch (error) {
    console.error('请求聊天接口失败:', error)
  }

  if (!response) {
    const errorMsg = '无法连接聊天服务，请稍后重试。'
    messages.value.push({
      id: generateMessageId(),
      type: 'ai',
      content: errorMsg,
      createTime: Date.now()
    })
    isSending.value = false
    syncSessionMessages(currentSessionId.value)
    ElMessage.error(errorMsg)
    return
  }

  if (!response.ok || !response.body) {
    const errorMsg = `请求出错: ${response.status} ${response.statusText}`
    messages.value.push({
      id: generateMessageId(),
      type: 'ai',
      content: errorMsg,
      createTime: Date.now()
    })
    isSending.value = false
    syncSessionMessages(currentSessionId.value)
    ElMessage.error(errorMsg)
    return
  }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let aiResponse = ''
  let buffer = ''
  let frameId = null
  let streamError = null
  let lastAnswer = ''
  let lastReference = null
   
    const aiMsg = {
    id: generateMessageId(),
      type: 'ai',
      content: '',
      createTime: Date.now()
    }
    messages.value.push(aiMsg)

  const updateAiMessage = () => {
    const msgIndex = messages.value.findIndex(m => m.id === aiMsg.id)
    if (msgIndex !== -1) {
      messages.value[msgIndex].content = aiResponse
      if (lastReference) {
        messages.value[msgIndex].reference = lastReference
      }
    }
  }

  const processBuffer = () => {
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    let updated = false
    lines.forEach(line => {
      const trimmed = line.trim()
      if (!trimmed) return
      const payloadLine = trimmed.startsWith('data:') ? trimmed.slice(5).trim() : trimmed
      if (!payloadLine || payloadLine === '[DONE]') return

      try {
        const parsed = JSON.parse(payloadLine)
        if (typeof parsed === 'string') {
          lastAnswer = parsed
          updated = true
          return
        }

        if (parsed && typeof parsed === 'object') {
          const answerText = parsed.data?.answer ?? parsed.answer
          if (typeof answerText === 'string') {
            lastAnswer = answerText
            updated = true
          }

          const referenceData = parsed.data?.reference ?? parsed.reference
          if (referenceData) {
            lastReference = referenceData
          }
          return
        }

        if (typeof parsed === 'boolean') {
          return
        }
      } catch (error) {
        lastAnswer += payloadLine
        updated = true
      }
    })

    if (updated) {
      aiResponse = lastAnswer
      if (lastReference) {
        aiMsg.reference = lastReference
      }
    }

    return updated
  }

  let done = false
  while (!done) {
    const result = await reader.read().catch(error => {
      streamError = error
      return { done: true, value: undefined }
    })
    if (!result) break
    done = result.done
    if (result.value) {
      buffer += decoder.decode(result.value, { stream: true })
      const updated = processBuffer()
      if (updated) {
        updateAiMessage()
        if (!frameId) {
          frameId = requestAnimationFrame(() => {
            scrollToBottom()
            frameId = null
          })
        }
      }
    }
  }

  if (buffer) {
    buffer += '\n'
    const updated = processBuffer()
    if (updated) {
      updateAiMessage()
    }
  }

  if (streamError) {
    console.error('流式读取失败:', streamError)
    const msgIndex = messages.value.findIndex(m => m.id === aiMsg.id)
    if (msgIndex !== -1) {
      messages.value[msgIndex].content = '抱歉，AI回复处理出现问题，请稍后再试。'
    }
    ElMessage.error('AI回复处理出现问题，请稍后再试。')
  }

  isSending.value = false
  syncSessionMessages(currentSessionId.value)
}

// 创建新对话
const createNewChat = async () => {
  const sessionName = '我的会话'
  const result = await createSessionApi(getAssistantId(), {
    sessionName,
    userId: currentUserId.value
  }).catch((error) => {
    console.error('创建会话失败:', error)
    ElMessage.error('创建新对话失败，请稍后再试。')
    return null
  })

  if (!result) return

  if (result.code !== 1 || !result.data) {
    ElMessage.error(result.msg || '创建新对话失败，请稍后再试。')
    return
  }

  const assistantIdFromSession = result.data.chat_id
  if (assistantIdFromSession) {
    assistantChatId.value = assistantIdFromSession
    localStorage.setItem('ragflowChatId', assistantIdFromSession)
  }
  const newChatItem = buildChatItem(result.data)
  if (newChatItem) {
    chatList.value.unshift(newChatItem)
    showSession(newChatItem.sessionId)
    syncSessionMessages(newChatItem.sessionId, false)
    updateSessionPreview(newChatItem.sessionId)
  }
}

// 加载对话历史
const switchChat = (chat) => {
  if (!chat?.sessionId) return
  showSession(chat.sessionId)
}

// 删除对话
const deleteChat = async (sessionId) => {
  if (!sessionId) return

  const result = await deleteSessionApi(getAssistantId(), [sessionId]).catch((error) => {
    console.error('删除对话失败:', error)
    ElMessage.error('删除对话失败，请稍后再试。')
    return null
  })

  if (!result) return

  if (result.code !== 1) {
    ElMessage.error(result.msg || '删除对话失败，请稍后再试。')
    return
  }

  const index = chatList.value.findIndex(chat => chat.sessionId === sessionId)
  if (index !== -1) {
    chatList.value.splice(index, 1)
  }

  delete sessionCache.value[sessionId]

  if (currentSessionId.value === sessionId) {
    if (chatList.value.length > 0) {
      showSession(chatList.value[0].sessionId)
    } else {
      currentSessionId.value = ''
      messages.value = []
      currentChatTitle.value = '新对话'
    }
  }

  ElMessage.success('对话已删除')
}

// 加载用户所有对话列表
const loadUserChatList = async () => {
  const result = await listSessionsApi(getAssistantId()).catch((error) => {
    console.error('获取会话列表失败:', error)
    ElMessage.error('加载会话列表失败，请稍后再试。')
    return null
  })

  if (!result) return

  if (result.code !== 1 || !Array.isArray(result.data)) {
    ElMessage.error(result.msg || '加载会话列表失败，请稍后再试。')
    return
  }

  sessionCache.value = {}
  const sessions = result.data || []
  chatList.value = sessions
    .map(makeSessionPreview)
    .filter(Boolean)

  const firstAssistantId = sessions[0]?.chat_id
  if (firstAssistantId) {
    assistantChatId.value = firstAssistantId
    localStorage.setItem('ragflowChatId', firstAssistantId)
  }

  if (chatList.value.length > 0) {
    showSession(chatList.value[0].sessionId)
  } else {
    currentSessionId.value = ''
    messages.value = []
    currentChatTitle.value = '新对话'
  }
}

// 处理滚动事件
const handleScroll = () => {
  showScrollbar.value = true
  setTimeout(() => {
    showScrollbar.value = false
  }, 3000)
}

// 初始化
onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  document.addEventListener('click', handleClickOutside)
  
  const loginUser = localStorage.getItem('loginuser')
  if (loginUser) {
    try {
    const userInfo = JSON.parse(loginUser)
      if (userInfo?.id) {
    currentUserId.value = userInfo.id.toString()
      }
    } catch (error) {
      console.warn('解析用户信息失败', error)
    }
  }
  
  await loadUserChatList()
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="chat-app">
    <!-- 移动端侧边栏按钮 -->
    <div v-if="isMobile" class="mobile-header">
      <button class="sidebar-btn" @click="toggleSidebar">☰</button>
      <div class="mobile-logo">
        <div class="mobile-logo-icon">
          <img src="/logo.jpg" alt="logo"></img>
        </div>
        <h1 class="mobile-title">咸师小匠</h1>
      </div>
    </div>
    
    <!-- 侧边栏遮罩层 -->
    <div v-if="showSidebar && isMobile" class="sidebar-overlay" @click="closeSidebar"></div>
    
    <div class="app-container" :class="{ 'mobile-app-container': isMobile }">
      <!-- 左侧边栏 -->
      <div class="sidebar" :class="{ 'sidebar-mobile': isMobile, 'sidebar-open': showSidebar }">
        <!-- 侧边栏头部 -->
        <div class="sidebar-header">
          <div class="logo">
 
            <div class="logo-icon">
              <img src="/logo.jpg" alt="logo"></img>
              </div>
            <h1>咸师小匠</h1>
          </div>
      
        </div>

        <!-- 新对话按钮 -->
        <div class="new-chat-section">
          <button @click="createNewChat" class="new-chat-btn">
            <span class="btn-icon">+</span>
            <span>新对话</span>
          </button>
        </div>


        <!-- 历史对话 -->
        <div class="history-section">
          <h3 class="history-title">历史对话</h3>
          <div class="chat-list">
            <div 
              v-for="(chat, index) in chatList" 
              :key="chat.sessionId || index"
              :class="['chat-item', chat.sessionId === currentSessionId ? 'active' : '']"
              @click="switchChat(chat)"
            >
              <span class="chat-text">{{ chat.title || ('对话' + (index + 1)) }}</span>
              <span class="chat-menu" @click.stop="deleteChat(chat.sessionId)">⋯</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="main-area">
        <!-- 主区域头部 -->
        <div class="main-header">
          <div class="header-left">
            <h2 class="chat-title">{{ currentChatTitle }}</h2>
            <p class="ai-notice">内容由AI生成</p>
          </div>
          <div class="header-right">
           <div class="user-info">
              <div class="user-avatar">U</div>
              <span class="user-name">用户</span>
            </div>
          </div>
        </div>

        <!-- 消息显示区域 -->
        <div class="messages-container" @scroll="handleScroll" :class="{ 'show-scrollbar': showScrollbar }">
          <div class="messages">
            <div 
              v-for="msg in messages" 
              :key="msg.id"
              :class="['message', msg.type]"
            >
          
              <div class="message-content">
                <div class="message-bubble" v-html="formatMessageContent(msg.content)">
                </div>
                <div class="message-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
            
      
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-container">
            <div class="input-wrapper">
              <textarea 
                v-model="inputText"
                placeholder="发送消息..."
                @keyup.enter="sendMessage"
       
               
                class="message-input"
                ref="messageInput"
              ></textarea>
              <div class="input-actions">
                <button 
                  @click="sendMessage" 
                  class="send-btn"
                  :disabled="!inputText.trim() || isSending"
                >
                  <span v-if="isSending" class="loading-spinner">...</span>
                  <span v-else>→</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全局样式 */
.chat-app {
  height: 100vh;
  width: 100%;
  background: white;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.app-container {
  display: flex;
  height: 100%;
  background: white;
  border-radius: 10px;
  overflow: hidden;
}

/* 移动端应用容器 */
.mobile-app-container {
  margin-top: 60px;

  border-radius: 0;
}

/* 移动端头部 */
.mobile-header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: #f5f5f5;
  color: #333;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1001;
  height: 60px;
  border-bottom: 1px solid #e5e5e5;
}

.mobile-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  justify-content: center;
}

.mobile-logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-logo-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

.sidebar-btn {
  background: transparent;
  color: #333;
  border: none;
  font-size: 35px;

  padding: 8px;
  border-radius: 4px;
  margin-right: 15px;
}

.mobile-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

/* 侧边栏遮罩层 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(55, 55, 55, 0.5);
  z-index: 999;
}

/* 左侧边栏样式 */
.sidebar {
  width: 200px;
  background: #f5f3f3;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
}

/* 移动端侧边栏样式 */
.sidebar-mobile {
  position: fixed;
  top: 0;
  left: -90vw;
  height: 100vh;
  width: 50vw;
  z-index: 1000;
  background: white;
  transition: left 0.4s ease;
  overflow-y: auto;
  border-radius: 2px;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-mobile.sidebar-open {
  left: 0;
}


.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;

  border-bottom: 1px solid #e5e5e5;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon img {
  width: 100%;
  height: 100%;

  border-radius: 4px;
}

.logo h1 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-icons {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 16px;
}

.icon-btn:hover {
  background: #f5f5f5;
  color: #333;
}






/* 新对话按钮 */
.new-chat-section {
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.new-chat-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #1150d8 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.new-chat-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.btn-icon {
  font-size: 20px;
  font-weight: 600;
}






/* 历史对话 */
.history-section {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.history-title {
  font-size: 19px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  
}

.chat-item:hover {
  background: #f8f9fa;
}

.chat-item.active {
  background: #e3f2fd;
  color: #1976d2;
}



.chat-item.active .chat-icon {
  background: #667eea;
  color: white;
}

.chat-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-item.active .chat-text {
  color: #1976d2;
}

.chat-menu {
  font-size: 16px;
  color: #999;
  opacity: 0;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.chat-item:hover .chat-menu {
  opacity: 1;
}

.chat-menu:hover {
  background: #f0f0f0;
  color: #666;
}



/* 主聊天区域 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.main-header {
  padding: 20px 15px;

  display: flex;
  justify-content: space-between;
  align-items: center;
}



.chat-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.ai-notice {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-info:hover {
  background: #e9ecef;
}

.user-avatar {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.header-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
}

/* 消息区域 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  background: white;
  /* 滚动条样式 - 默认隐藏 */
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  transition: scrollbar-color 0.3s ease;
}

/* Webkit浏览器滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
  transition: all 0.3s ease;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 3px;
  transition: background 0.3s ease;
}

/* 显示滚动条时的样式 */
.messages-container.show-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
}

.messages-container.show-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.messages-container.show-scrollbar {
  scrollbar-color: #c1c1c1 transparent;
}

/* 鼠标悬停时也显示滚动条 */
.messages-container:hover::-webkit-scrollbar-thumb {
  background: #c1c1c1;
}

.messages-container:hover::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.messages {
  padding: 20px 20px 40px 20px;
  max-width: 70vw;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.message {
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
  animation: fadeInUp 0.3s ease;
}

.message.user {
  flex-direction: row-reverse;
}

.message.ai {
  flex-direction: row;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}




.message-content {
  max-width: 100%;
  width: auto;
}

.message.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message.ai .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-bubble {
  padding: 12px 12px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.5;
  word-wrap: break-word;
 
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #efeded 0%, #ffffff 100%);
  color: rgb(15, 15, 15);
  border-bottom-right-radius: 4px;
}

.message.ai .message-bubble {
  background: #f8f9fa;
  color: #333;
  border: 1px solid #e5e5e5;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  padding: 0 4px;
}








/* 输入区域 */
.input-area {
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #ffffff;
}

.input-container {
  max-width: 60vw;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1.5px solid #e5e5e5;
  border-radius: 20px;
  background: white;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: 84px;
  width: 100%;
 
}

.input-wrapper:focus-within {
  border-color: #173eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}





.message-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  min-height: 100px;
  max-height: 200px;
  font-family: inherit;
  overflow-y: auto;
  transition: height 0.2s ease;
  height: auto;
  /* 自动调整高度 */
  field-sizing: content;
}




.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #343436 0%, #d8d6da 100%);
  color: white;
 
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.2);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}



.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.input-buttons {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    margin: 10px;
    border-radius: 8px;
  }
  
  .sidebar {
    width: 240px;
  }
  
  .messages {
    padding: 20px;
  }
  
  .input-area {
    padding: 15px 20px;
  }
}
</style>