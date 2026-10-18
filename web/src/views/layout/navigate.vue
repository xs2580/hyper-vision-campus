<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { getAllImageApi,  getImageFileUrl, getVideoFileUrl, getAllSceneEdgeApi } from '@/api/Video.js'

const container = ref(null)
const startId = ref(null)
const endId = ref(null)

let scene, camera, renderer, controls
let sphere, material, videoTexture, vid

// 起点列表（固定）
const startPointList = ref([
  { id: 1, name: '东门' },
  { id: 2, name: '西门' }
])

// 终点列表（从后端加载）
const endPointList = ref([])
// 邻接表：key为起点ID，value为可到达的场景信息数组
const AllNeighbourList = ref(new Map())
// 所有场景数据
const allScenes = ref([])
// 场景边数据
const edgeList = ref([])

// 初始化数据，获取所有场景和边数据
const initData = async () => {
  try {
   
    
    // 获取所有场景图片数据
    const response = await getAllImageApi()
    
    if (response.code !== 1) {
      ElMessage.error('获取场景数据失败:', response)
      return
    }
    
    // 保存所有场景数据
    for (let i = 0; i < response.data.length; i++) {
      const image = response.data[i]
      allScenes.value.push({
        id: Number(image.id),
        url: getImageFileUrl(image.id),
        description: image.description
      })
    }
    
    // 获取所有边数据
    const edgeResponse = await getAllSceneEdgeApi()
    
    if (edgeResponse.code !== 1) {
      ElMessage.error('获取场景边数据失败:', edgeResponse)
      return
    }
    
    edgeList.value = edgeResponse.data
    
    // 构建邻接表
    buildNeighbourList()
    
    // 初始化时清空终点列表，等待用户选择起点
    endPointList.value = []
    
  } catch (error) {
    ElMessage.error('数据加载失败: ' + error.message)
  }
}

// 构建邻接表
const buildNeighbourList = () => {
  const neighbourList = new Map()
  
  for (let i = 0; i < edgeList.value.length; i++) {
    const edge = edgeList.value[i]
    
    // 初始化起点场景的边数组
    if (!neighbourList.has(edge.startId)) {
      neighbourList.set(edge.startId, [])
    }
    
    // 查找终点场景并添加边信息
    const endScene = allScenes.value.find(scene => scene.id == edge.endId)
    if (endScene) {
      neighbourList.get(edge.startId).push({
        sceneId: edge.endId,
        videoId: edge.videoId,
        description: endScene.description,
        angle: edge.angle
      })
    }
  }
  
  // 保存邻接表
  AllNeighbourList.value = neighbourList
}

onMounted(() => {
  initData()
  initThree()
})

onBeforeUnmount(() => {
  // 清理资源
  if (vid) {
    vid.pause()
    vid.src = ''
    vid.load()
    vid = null
  }
  if (videoTexture) {
    videoTexture.dispose()
    videoTexture = null
  }
  if (renderer) {
    renderer.dispose()
  }
  if (controls) {
    controls.dispose()
  }
})

// 初始化Three.js场景
const initThree = () => {
  scene = new THREE.Scene()
  
  // 创建相机
  const containerWidth = container.value.clientWidth
  const containerHeight = container.value.clientHeight
  camera = new THREE.PerspectiveCamera(75, containerWidth / containerHeight, 0.1, 1000)
  camera.position.set(1, -0.1, 0)
  
  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ 
    antialias: true,
    preserveDrawingBuffer: true,
    alpha: false
  })
  renderer.setSize(containerWidth, containerHeight)
  renderer.domElement.crossOrigin = 'anonymous'
  container.value.appendChild(renderer.domElement)
  
  // 创建球体
  const geometry = new THREE.SphereGeometry(600, 60, 40)
  geometry.scale(-1, 1, 1)
  
  // 创建材质（默认白色）
  material = new THREE.MeshBasicMaterial({ color: 0xffffff })
  
  // 创建网格
  sphere = new THREE.Mesh(geometry, material)
  scene.add(sphere)
  
  // 创建控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  
  // 渲染循环
  const animate = () => {
    controls.update()
    renderer.render(scene, camera)
    requestAnimationFrame(animate)
  }
  animate()
}

// 选择起点，加载对应的背景图并更新终点列表
const handleSelectStart = () => {
  if (startId.value) {
    loadBackgroundImage(startId.value)
    updateEndPointList()
  }
}

// 根据选择的起点更新终点列表
const updateEndPointList = () => {
  if (!startId.value) {
    // 如果没有选择起点，清空终点列表
    endPointList.value = []
    return
  }
  
  // 从邻接表获取当前起点可到达的场景
  const availableScenes = AllNeighbourList.value.get(startId.value)
  
  if (!availableScenes || availableScenes.length === 0) {
    endPointList.value = []
    ElMessage.warning('当前起点没有可到达的终点')
    return
  }
  
  // 更新终点列表为当前起点可到达的场景
  endPointList.value = availableScenes.map(scene => ({
    id: scene.sceneId,
    name: scene.description
  }))
  
  // 清空已选择的终点（如果当前选择的终点不在新的列表中）
  if (endId.value && !endPointList.value.find(p => p.id === endId.value)) {
    endId.value = null
  }
}



// 加载背景图
const loadBackgroundImage = (imageId) => {
  const imageUrl = getImageFileUrl(imageId)
  const startPoint = startPointList.value.find(p => p.id === imageId)
  
  ElMessage.success(`加载起点：${startPoint.name}`)

  
  const loader = new THREE.TextureLoader()

  
  loader.load(
    imageUrl,
    (texture) => {
      console.log('背景图加载成功')
      
      // 清理视频
      if (videoTexture) {
        videoTexture.dispose()
        videoTexture = null
      }
      if (vid) {
        vid.pause()
        vid.src = ''
        vid.load()
        vid = null
      }
      
      material.map = texture
   
      material.needsUpdate = true
    }
  )
}

// 规划路线，播放视频
const buildRoute = () => {
  if (!startId.value || !endId.value) {
    ElMessage.warning('请选择起点和终点')
    return
  }
  
  const startPoint = startPointList.value.find(p => p.id == startId.value)
  const endPoint = endPointList.value.find(p => p.id == endId.value)
  
  ElMessage.info(`导航路线：${startPoint.name} → ${endPoint.name}`)
  
  // 播放视频（根据终点id）
  playVideo(endId.value)
}

// 播放视频
const playVideo = (videoId) => {
  const videoUrl = getVideoFileUrl(videoId)
  
  // 创建video元素
  vid = document.createElement('video')
  vid.crossOrigin = 'anonymous'
  vid.muted = false
  vid.loop = false
  vid.playsInline = true
  vid.preload = 'auto'
  
  // 视频可以播放时
  vid.addEventListener('canplay', () => {
    videoTexture = new THREE.VideoTexture(vid)
    videoTexture.minFilter = THREE.LinearFilter
    videoTexture.magFilter = THREE.LinearFilter
    videoTexture.format = THREE.RGBAFormat
    material.map = videoTexture
    material.needsUpdate = true
    vid.play()
    ElMessage.success('开始导航')
  })
  
  // 播放进度
  vid.addEventListener('timeupdate', () => {
    const progress = (vid.currentTime / vid.duration * 100).toFixed(0)
    console.log('导航进度:', progress, '%')
  })
  
  // 播放结束
  vid.onended = () => {
    ElMessage.success('已到达目的地')
    // 加载终点背景图
    loadBackgroundImage(videoId)
  }
  
  vid.src = videoUrl
}

// 重置
const resetRoute = () => {
  startId.value = null
  endId.value = null
  
  // 清理视频
  if (vid) {
    vid.pause()
    vid.src = ''
    vid.load()
    vid = null
  }
  
  if (videoTexture) {
    videoTexture.dispose()
    videoTexture = null
  }
  
  // 重置为白色背景
  material.map = null
  material.color.setHex(0xffffff)
  material.needsUpdate = true
  
  // 重置终点列表为空，等待用户选择起点
  endPointList.value = []
  
  ElMessage.info('已重置')
}
</script>

<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="header">
      <h1 class="site-title">超维新视界</h1>
      <nav class="nav">
        <router-link to="/home">首页</router-link>
        <router-link to="/VR_Map">校园全景</router-link>
        <router-link to="/navigate">校园导航</router-link>
      </nav>
    </div>

    <!-- 主体内容 -->
    <div class="main">
      <!-- 页面标题区域 -->
   
      <div class="page-header">
        <div class="title-container">
          <div class="title-icon">
  
              <img src="/logo.jpg" class="icon-image" >
            </img>
          </div>
          <div class="title-text">
            <h1 class="main-title">校园智能导航</h1>
            <p class="subtitle">智能规划路线，轻松到达目的地</p>
          </div>
        </div>
      </div>

  
        <!-- 导航工具区域 -->
        <div class="nav-tools">
          <div class="tools-container">
            <div class="tool-item">
              <label class="tool-label">起点</label>
              <el-select 
                v-model="startId" 
                placeholder="请选择起点" 
                @change="handleSelectStart"
                class="tool-select"
              > 
                <el-option 
                  v-for="p in startPointList" 
                  :key="p.id" 
                  :label="p.name" 
                  :value="p.id"
                />
              </el-select>
            </div>
            
            <div class="tool-item">
              <label class="tool-label">终点</label>
              <el-select 
                v-model="endId" 
                placeholder="请选择终点" 
           
                class="tool-select"
              >
                <el-option 
                  v-for="p in endPointList" 
                  :key="p.id" 
                  :label="p.name" 
                  :value="p.id" 
             
                />
              </el-select>
            </div>
            <p class="notice">
              选择起点后自动加载场景，点击规划路线开始导航，点击重置清空
            </p>
            <div class="tool-buttons">
              <el-button type="primary" @click="buildRoute" class="route-btn">
                <i class="el-icon-location"></i>
                规划路线
              </el-button>
              <el-button type="warning" @click="resetRoute" class="reset-btn">
                <i class="el-icon-refresh"></i>
                重置
              </el-button>
            </div>
          </div>
        </div>

      <!-- VR导航展示区域 -->
      <div class="map-container" ref="container">
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  height: 90px;
  position: relative;
 
}

.site-title {
  position: absolute;
  left: 30px;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", sans-serif;

}

.nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.nav a {
  color: #fff;
  text-decoration: none;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
  position: relative;
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", sans-serif;
}
.nav a:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.nav a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #fff;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav a:hover::after {
  width: 85%;
}

/* 主体布局 */
.main {
  flex: 1;
  padding: 30px;
  overflow: auto;
}

/* 页面标题区域 */
.page-header {
  text-align: center;
  margin-bottom: 10px;
}

.title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 25px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

}

.title-icon {
  font-size: 60px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 50%;
  width: 100px;
  height: 100px;
  padding:2px;

}

.icon-image{
  width:100px;
  height:100px;
  border-radius:50%;
  object-fit: cover;
}
.title-text {
  text-align: center;
}

.main-title {
  font-size: 36px;
  font-weight: 800;
  color: #1e293b;
}

.subtitle {
  font-size: 18px;
  color: #64748b;
  margin: 0;
 
}




/* 导航工具区域 */
.nav-tools {
  background: white;

  padding: 30px;

}

.tools-container {
  display: flex;
  align-items: center;
  gap: 80px;
 
}
.tool-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tool-label {
  font-size: 20px;
  font-weight: 600;
  color: #374151;
 
}


.tool-buttons {
  display: flex;
  gap: 15px;
  margin-left: auto;
}

/* 提示信息样式 */
.notice {
  font-size: 19px;
  color: #000000;
  background: #f8fafc;
  padding: 8px 8px;
  border-radius: 8px;
  border-left: 2px solid #5a75a1;

  
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.route-btn, .reset-btn {
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;

 
}


/* VR导航展示容器 */
.map-container {
margin-top:5px;
  width: 100%;
  height: 600px;
  background: #e8e8e8;

  overflow: hidden;
  

}
</style>