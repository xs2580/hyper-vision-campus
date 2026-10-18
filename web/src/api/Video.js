import request from '@/utils/request.js'

const FILE_BASE_URL = (import.meta.env.VITE_FILE_BASE_URL ?? '').replace(/\/$/, '')
const withFileBase = (path) => `${FILE_BASE_URL}${path}`

// ========== 图片API ==========

// 获取所有图片列表（返回元数据）
export const getAllImageApi = () => {
  return request.get('/image')
}

// 根据ID获取图片信息（返回数组，取第一个元素）
export const getImageurlApi = (id) => {
  return request.get(`/image/${id}`)
}

// 获取图片文件URL（直接指向Java后端）
export const getImageFileUrl = (id) => {
  return withFileBase(`/image/${id}`)
}

// 获取所有图片列表（反向顺序）
export const getAllImageReverseApi = () => {
  return request.get('/image/reverse')
}

// 获取反向图片文件URL（直接指向Java后端）
export const getImageFileUrlReverse = (id) => {
  return withFileBase(`/image/reverse/${id}`)
}

// ========== 视频API ==========

// 获取所有视频列表（返回元数据）
export const getAllVideoApi = () => {
  return request.get('/video')
}

// 根据ID获取视频文件（返回字节流）
// 注意：您的后端 /video/{id} 直接返回文件字节流，不是元数据
// 如果需要元数据，应该先调用 getAllVideoApi() 然后筛选
export const getVideourlApi = (id) => {
  return request.get(`/video/${id}`)
}

// 获取视频文件URL（直接指向Java后端）
export const getVideoFileUrl = (id) => {
  return withFileBase(`/video/${id}`)
}

// ========== 图片API（反向路线）==========

// 根据ID获取反向图片文件（返回字节流）
export const getImageReverseApi = (id) => {
  return request.get(`/image/reverse/${id}`)
}

// ========== 视频API（反向路线）==========

// 获取所有反向视频列表（返回元数据）
export const getAllVideoReverseApi = () => {
  return request.get('/video/reverse')
}

// 根据ID获取反向视频文件（返回字节流）
export const getVideoReverseApi = (id) => {
  return request.get(`/video/reverse/${id}`)
}

// 获取反向视频文件URL
export const getVideoReverseFileUrl = (id) => {
  return withFileBase(`/video/reverse/${id}`)
}

// ========== 场景边API（邻接表）==========

// 获取所有场景边数据（用于构建邻接表）
export const getAllSceneEdgeApi = () => {
  return request.get('/edge')
}