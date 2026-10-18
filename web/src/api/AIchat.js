import request from '@/utils/request.js'

const API_CHAT_BASE = (import.meta.env.VITE_API_CHAT_BASE ?? '/api').replace(/\/$/, '')
const withChatBase = (path) => `${API_CHAT_BASE}${path}`

// ========== 会话流式聊天 ==========

export const chatStreamApi = (chatId, payload) => {
  return fetch(withChatBase(`/chat/${chatId}/stream`), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream'
    },
    body: JSON.stringify(payload)
  })
}

// ========== 会话管理 ==========

export const createSessionApi = (chatId, { sessionName, userId }) => {
  return request.post(withChatBase(`/chat/${chatId}/sessions`), null, {
    params: {
      sessionName,
      userId
    }
  })
}

export const listSessionsApi = (chatId, { page = 1, pageSize = 30 } = {}) => {
  return request.get(withChatBase(`/chat/${chatId}/sessions`), {
    params: {
      page,
      pageSize
    }
  })
}

export const deleteSessionApi = (chatId, sessionIds) => {
  return request.delete(withChatBase(`/chat/${chatId}/sessions`), {
    data: sessionIds ? { sessionIds } : undefined
  })
}
