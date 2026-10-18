import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

//创建axios实例对象
const request = axios.create({
  baseURL: API_BASE_URL || undefined,
  timeout: 600000
})

// //请求拦截器
request.interceptors.request.use(
  (config) => { //成功回调
    const loginUser=JSON.parse(localStorage.getItem('loginUser'))
    if(loginUser&&loginUser.token){
      //请求头添加token
      config.headers.token = loginUser.token
    }
    return config

  },
  (error) => { //失败回调
    return Promise.reject(error)
  }
)
request.interceptors.response.use(
  (response) => { 
    return response.data
  },
  (error)=>
  {
    if(error.response && error.response.status == 401) {
      ElMessage.error("请先登录")
      router.push('/login')
    } else if(error.response && error.response.status == 404) {
      ElMessage.error(`请求的资源不存在: ${error.config.url}`)
      console.error('404错误详情:', error.config)
    } else {
      ElMessage.error(error.message || "请求失败")
    }
    return Promise.reject(error)
  }
)
export default request