<template>
  <div class="page-container">
    <div class="header">
      <h1 class="site-title">超维新视界</h1>
      <nav class="nav">
        <router-link to="/home">首页</router-link>
        <router-link to="/VR_Map">校园全景</router-link>
        <router-link to="/navigate">校园导航</router-link>
      </nav>
    </div>
    
    <!-- 视频展示区域 -->
    <div class="video-area" ref="container">
      <!-- 热点标签 -->
      <div v-show="showTag" class="tags">
        <div
          v-for="(h) in hotspots"
          :key="h.id"
          class="tag-wrapper"
          v-show="h.visible"
        >
          <!-- 蓝色标签框 -->
          <div class="tag" @click="playVideoById(h.id)">
            {{ h.description }}
          </div>
          <!-- 堆叠的白色箭头 -->
          <div 
            class="tag-notice" 
            :style="{
              opacity: cameraPitch < 30 ? Math.max(0, (30 - cameraPitch) / 30):0,
              transform: `perspective(80px) rotateX(${-cameraPitch}deg) translateZ(0)`
            }"
          >
            <div class="arrow"></div>
            <div class="arrow"></div>
            <div class="arrow"></div>
          </div>
        </div>
      </div>
      

      <!-- 播放控制按钮（右上角） -->
      <div class="video-control" v-show="Isplaying">
    
        <el-button 
          type="success" 
          class="video-control-button" 
          size="large" 
          :disabled="!canPlay"
          @click="video_play"
        >播放</el-button>
        <el-button 
          type="warning" 
          class="video-control-button" 
          size="large" 
          :disabled="!canPause"
          @click="video_pause"
        >暂停</el-button>
        <el-button type="danger" class="video-control-button" size="large" @click="mute_all">静音</el-button>
      </div>
      <!-- 这里新加一个地图缩略图的容器 -->
      <div class="map-container" @click="openMapPreview">
        <div class="map-content">
          <img src="/map.jpg" alt="校园地图缩略图" />
          <div
            class="map-marker"
            :style="{
              left: mapSmall.x + '%',
              top:  mapSmall.y + '%'
            }"
          >
            <span class="map-marker-spot"></span>
            <span class="map-marker-info"><p>您的位置</p></span>
          </div>
        </div>
      </div>
  
        <div
          v-if="isMapvisible"
          class="map-preview-overlay"
          @click="closeMapPreview"
        >
          <div class="map-preview-container">
            <div class="map-preview-header">
              <span>校园地图</span>
              <button class="map-preview-close" @click="closeMapPreview">✕</button>
            </div>
            <div class="map-preview-body">
              <img src="/map.jpg" alt="校园地图大图" />
              <div
                class="map-preview-marker"
                :style="{
                  left: mapLarge.x + '%',
                  top: mapLarge.y + '%'
                }"
              >
                <span class="map-marker-spot"></span>
                <span class="map-marker-info"><p>您的位置</p></span>
              </div>
            </div>
          </div>
        </div>
   

      <!-- 角度标记辅助工具（左下角） -->
      <div class="angle-marker">
        <div class="angle-display">
          <span class="angle-label">当前角度:</span>
          <span class="angle-value">{{ currentAngle }}°</span>
        </div>
  
      </div>
      <!-- AI语音助手 -->
      <div class="AI-container">
            <router-link class="AI-logo" :to="{ name: 'ai-chat' }" target="_blank">
          <img src="/logo.jpg" :class="['logo', { 'floating': isSpeaking }]" alt="AI Logo" />
        </router-link>
        <div class="AI-read">
          <div class="speaker-icon">🔈</div>
          <!-- 声波效果 -->
          <div class="sound-waves" v-if="isSpeaking">
            <div class="wave wave1"></div>
            <div class="wave wave2"></div>
            <div class="wave wave3"></div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

import { ElMessage } from 'element-plus'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { getAllImageApi, getVideoFileUrl, getImageFileUrl, getAllSceneEdgeApi } from '@/api/Video.js'
const Isplaying = ref(false)
const allowShowTag = ref(true)  // 是否允许显示标签

const isMapvisible = ref(false)
const mapSmall = ref({ x: 50, y: 50 })
const mapLarge = ref({ x: 50, y: 50 })



// 语音阅读相关
let currentUtterance = null  // 当前语音对象，用于暂停和恢复
const isSpeaking = ref(false)  // AI是否正在阅读
let isReading = false  // 标记当前视频是否已经阅读过
let isControl=true;
// 语音阅读的文本
let descriptionText = ''

// 防抖timer相关
const canPause = ref(true)
const canPlay = ref(true)
let pauseTimer = null
let playTimer = null

// 防抖timer：暂停按钮
const setPauseTimer = (delay = 1700) => {
  canPause.value = false
  if (pauseTimer) {
    clearTimeout(pauseTimer)
  }
  pauseTimer = setTimeout(() => {
    canPause.value = true
  }, delay)
}

// 防抖timer：播放按钮
const setPlayTimer = (delay = 1700) => {
  canPlay.value = false
  if (playTimer) {
    clearTimeout(playTimer)
  }
  playTimer = setTimeout(() => {
    canPlay.value = true
  }, delay)
}

// 停止/暂停语音播放
const stopReading = () => {
  if (window.speechSynthesis&&isControl) {
        window.speechSynthesis.pause()
        isSpeaking.value=false;
  
  }
}
// 继续/恢复语音播放
// iSControl默认是true，表示当前能否控制暂停和播放按钮，影响isSpeaking状态，isSpeaking用于控制语音阅读的动效
const continueReading = () => {
  if (window.speechSynthesis&&isControl) {
    window.speechSynthesis.resume();
    isSpeaking.value=true;
  
  }
}

//控制函数，控制视频和语音的播放
const video_pause = () => {
// 和定时器的时间间一致
  if (!vid || !canPause.value) {
    return
  }

  vid.pause()
  stopReading()  // 暂停时也暂停语音
  allowShowTag.value = false  // 暂停时禁止显示标签
  setPauseTimer()
}

const video_play = () => {
  if (!vid || !canPlay.value) {
    return
  }

  vid.play()
  allowShowTag.value = false  // 播放时禁止显示标签
  setPlayTimer()

  // 如果视频是静音的，则不能播放语音
  if (!vid.muted)
   {
    if (currentUtterance) 
    {
      continueReading()  // 播放时恢复语音（仅在视频未静音时）
    }
     else if (descriptionText) 
    {
      autoRead(descriptionText)
      isReading = true
    }
  } 
  else {
    stopReading()  // 如果视频静音，确保语音也停止
  }
}
const mute_all = () => {
  if (!vid) {
    return
  }
// 逻辑：如果视频是静音的，取消正在阅读的文本
// 如果视频不是静音的，并且视频没读完，并且全局变量descriptionText不为空，则自动调用autoRead(Text)开始阅读
  vid.muted = !vid.muted
  if (vid.muted) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    isReading = false
  } else
   {
    const isVideoPlaying = !vid.paused && !vid.ended
    if (isVideoPlaying && descriptionText)
     {
      autoRead(descriptionText)
      isSpeaking.value = true
      isReading = true
    }
  }
}


const container = ref(null)
const showTag = ref(false)
const currentAngle = ref(0)  // 当前相机角度（用于角度标记工具）
const cameraPitch = ref(0)  // 相机俯仰角（用于箭头显示）


let scene, camera, renderer, controls
let sphere, material, videoTexture, vid

// 坐标系相关



// 全景图与视频,定义数据结构从后端调用
const IMAGE = ref({ id: '', url: '', description: '' })
const VIDEO = ref({ id: '', url: '' })
const SpotsInfo=ref([{id:'1',description:'一般不想出校门但是又想买小零食或者日常用品的话，在各个食堂里都有小卖店，小吃街走到尽头有一个稍微大一些的店。出校门的话，西门对面市场往里走有一个比较大的超市，东门出门左拐直走，到十字路口过马路就会看到一个大超市，东西最齐全但是稍微有点远。蛋糕甜品'},
{id:'2',description:`校歌

                       毕原放歌

       峻阳   词  

                             渭水泱泱   北蟒苍苍

          帝都铅华洗尽    秦汉余韵长

                地创业写执著

               风雨兼程育栋梁

             让青春见证记忆沧桑

            重教尊师国运昌 国运昌

           南湖灵秀       西山豪壮

          文林韶华初显   毕原正梳妆

               莘莘学子志气昂

               师院奇葩笑艳阳

             看智者跨越世纪梦想

            厚德博学兴家帮  兴家帮

            征歌嘹亮        行路无疆

           春华秋实岁月    真情共寒窗

               渭城朝雨催新蕾

               和谐教苑数芬芳

              用汗水谱就千秋乐章

            科学发展铸辉煌铸辉煌







`},
{id:'3',description:'一般不想出校门但是又想买小零食或者日常用品的话，在各个食堂里都有小卖店，小吃街走到尽头有一个稍微大一些的店。出校门的话，西门对面市场往里走有一个比较大的超市，东门出门左拐直走，到十字路口过马路就会看到一个大超市，东西最齐全但是稍微有点远。蛋糕甜品'}






])
// 地图上标记点
const hotspots = ref([])
// 场景边数据
const edgeList = ref([])
// 邻接表：key为起点ID，value为可到达的场景信息数组
const AllNeighbourList = ref(new Map())
// 所有场景数据，用来创建邻接表
const allScenes = ref([])



// 标签的方向向量现在从数据库动态获取（根据edge.angle字段计算）
// 不再使用固定的 TARGET_DIRS
/*
数据结构说明：
@allScenes.value: 所有场景数据，用来创建邻接表，从后端数据库获得的最原始的数据
allScenes.value = [
  {
    id: Number,           // 场景ID（Number类型）
    url: String,          // 场景图片URL
    description: String   // 场景描述/名称
  }
]
allScenes.value = [
  {
    id: 1,
    url: '/api/image/1',
    description: '东大门'
}]
 @edgeList.value: 场景边数据，用来创建邻接表，从后端数据库获得的数据，查询的是边表
 edgeList.value = [
   {
     id: Number,              // 数据的顺序
     startId: Number,         // 起点场景ID
     endId: Number,           // 终点场景ID
     videoId: Number,         // 过渡视频ID
     createTime: String,      // 创建时间
     updateTime: String,      // 更新时间
     angle: Number            // 标签应该在的角度（度数，0°=正前方，60°=右前方，-60°=左前方）
   }
 ]
edgeList.value = [
   {
    id: 1,
    startId: 1,              // 从场景1（东大门）
    endId: 2,                // 到场景2（东门广场）
    videoId: 1,              // 播放视频1
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00',
    angle: 60                // 标签在右前方60度位置
  }
]
 @AllNeighbourList.value: 邻接表，用来创建邻接表，是根据edgeList.value构建的，key为起点场景ID，value为终点场景ID数组
 AllNeighbourList.value = Map {
   Number => [                // key: 起点场景ID
     {
       sceneId: Number,       // 目标场景ID，是edgeList.value中的endId
       videoId: Number,       // 过渡视频ID，是edgeList.value中的videoId
       description: String,   // 目标场景描述，是allScenes.value中的description
       direction: Vector3,    // 标签的方向向量（根据edge.angle计算得出）
       angle: Number          // 原始角度值（度数）
     }
   ]
 }


*/



onMounted(() => {
  // 页面刷新时取消所有语音播放
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  currentUtterance = null
  isSpeaking.value = false
  isReading = false
  isControl = true
  canPause.value = true
  canPlay.value = true
  
  initThree()
  initData()
  Render()
})

// 页面卸载前清理资源
onBeforeUnmount(() => {
  // 清理timer
  if (pauseTimer) {
    clearTimeout(pauseTimer)
    pauseTimer = null
  }
  if (playTimer) {
    clearTimeout(playTimer)
    playTimer = null
  }
  canPause.value = false
  canPlay.value = false

  // 停止语音播放
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  currentUtterance = null
  isSpeaking.value = false  // 清理阅读状态
  
  // 停止并清理视频
  if (vid) {
    vid.pause()
    vid.src = ''
    vid.load()
    vid = null
  }
  
  // 清理视频纹理
  if (videoTexture) {
    videoTexture.dispose()
    videoTexture = null
  }
  
  // 清理Three.js资源
  if (controls) {
    controls.dispose()
    controls = null
  }
  
  if (renderer) {
    // 从DOM中移除canvas元素
    if (renderer.domElement && renderer.domElement.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement)
    }
    // 清理渲染器
    renderer.dispose()
    renderer = null
  }
  
  // 清理材质
  if (material) {
    if (material.map) {
      material.map.dispose()
    }
    material.dispose()
    material = null
  }
  
  // 清理几何体和场景
  if (sphere) {
    if (sphere.geometry) {
      sphere.geometry.dispose()
    }
    scene.remove(sphere)
    sphere = null
  }
  
  if (scene) {
    scene.clear()
    scene = null
  }
  
  if (camera) {
    camera = null
  }
})

// 初始化数据，从后端获取热点数据并添加status属性
const openMapPreview = () => {
  isMapvisible.value = true
}

const closeMapPreview = () => {
  isMapvisible.value = false
}





const mapDisplay = ref({
  // TODO: 根据实际地图上的位置替换为正确坐标（百分比）
  1: {
    mini: { x: 80, y: 90 },
    preview: { x: 75, y: 85 },

  },
  2: {
    mini: { x: 80, y: 70 },
    preview: { x: 75, y: 65 },
   
  },
  3: {
    mini: { x: 50, y: 52 },
    preview: { x: 50, y: 52 },

  }
})

const halfMapspot = ref({
  1: {
    mini: { x: 80, y: 80 },
    preview: { x: 75, y: 75 },
    markInfo: '东门小道'
  },
  2: {
    mini: { x: 80, y: 80 },
    preview: { x: 75, y: 75 },
    markInfo: '东门小道'
  }
})



const updateMapPosition = (sceneId) => {
  const targetPosition = mapDisplay.value[sceneId]
  if (!targetPosition) {  
    return
  }
  mapSmall.value = targetPosition.mini 
  mapLarge.value = targetPosition.preview 

}

const updateHalfMapPosition = (sceneId) => {
    const halfPosition = halfMapspot.value[sceneId]
    if (!halfPosition) 
    {
    return
  }
  mapSmall.value = halfPosition.mini;
  mapLarge.value = halfPosition.preview;

}

const initData = async () => {  
    
        ElMessage.success('正在加载场景数据...')
        
        // 获取所有场景图片数据
        const response = await getAllImageApi()
        
        if (response.code !== 1) {
           ElMessage.error('获取场景数据失败:', response)
           return
        }
        
        // 保存所有场景数据,用来创建邻接表
      
        for (let i = 0; i < response.data.length; i++) {
            const image = response.data[i]
            allScenes.value.push({
                id: Number(image.id),  // 统一使用Number类型
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
        
        // 获取第一张图片作为初始背景
        IMAGE.value = {
            id: Number(allScenes.value[0].id),  // 统一使用Number类型
            url:'/B1.jpg', // 初始化直接调用本地的图片避免id加载错误
            description: allScenes.value[0].description
        }
        updateMapPosition(IMAGE.value.id)
      
  
        // 设置背景图
        setBackground()
    
        // 根据邻接表构建热点显示
        buildHotspots()
   
}

// 构建邻接表
const buildNeighbourList = () => {
    const neighbourList = new Map()
    // 遍历场景边数据是从数据库获取的EdgeList数据

      for (let i = 0; i < edgeList.value.length; i++)
     {
        const edge = edgeList.value[i]
        
        // 初始化起点场景的边数组
        if (!neighbourList.has(edge.startId))
         {
            neighbourList.set(edge.startId, [])
        }
      /*
        // 查找终点场景并添加边信息
        edge = { startId: 1, endId: 2, videoId: 1 }
        //初始化后的邻接表：
        1，[]

        // 所有场景数据
        allScenes.value = [
        { id: 1, url: '/api/image/1', description: '东大门' },
        { id: 2, url: '/api/image/2', description: '东门广场' },
        { id: 3, url: '/api/image/3', description: '中心广场' },
      1 => [  // 从场景1（东大门）可以去：
       {
      sceneId: 2,            // 场景2（东门广场）
      videoId: 1,            // 播放视频1
      description: '东门广场',
      visible: true
    },

]
        */
         const endScene = allScenes.value.find(allScene => allScene.id == edge.endId)
         if (endScene) 
         {
             // edge.angle 是标签应该放在哪个方向的角度（以相机正前方为0°，向右转为正，向左转为负）
             // 保存角度值，稍后在渲染时根据相机实际朝向计算方向向量
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



const initThree = () => {
 
  
  // 加载场景
  scene = new THREE.Scene()
  
  // 创建相机
  const containerWidth = container.value.clientWidth
  const containerHeight = container.value.clientHeight
  camera = new THREE.PerspectiveCamera(75, containerWidth / containerHeight, 0.1, 1000)
  camera.position.set(1, -0.1, 0)

  
  // 创建渲染器，启用跨域支持
  renderer = new THREE.WebGLRenderer({ 
    antialias: true,
    preserveDrawingBuffer: true,
    alpha: false
  })
  renderer.setSize(containerWidth, containerHeight)
  // 设置渲染器的跨域支持
  renderer.domElement.crossOrigin = 'anonymous'
  // 将渲染器添加到容器
  container.value.appendChild(renderer.domElement)

  
  // 创建球体
  const geometry = new THREE.SphereGeometry(500, 60, 40)
  geometry.scale(-1, 1, 1)
  
  
  // 创建材质
  material = new THREE.MeshBasicMaterial({ color: 0xffffff })

  // 创建网格
  sphere = new THREE.Mesh(geometry, material)
  // 将网格添加到场景
  scene.add(sphere)

  
  // 创建控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  

  



}

// 添加坐标系显示
const addCoordinateSystem = () => {
  // 创建坐标轴辅助器
  const axesHelper = new THREE.AxesHelper(100)  // 100是坐标轴长度
  scene.add(axesHelper)
  
  // 创建网格辅助器（可选）
  const gridHelper = new THREE.GridHelper(200, 20)  // 200是网格大小，20是网格数量
  scene.add(gridHelper)
  
  // 添加相机位置指示器
  const cameraHelper = new THREE.CameraHelper(camera)
  scene.add(cameraHelper)
  

}

// 通过ID加载背景图
const loadBackgroundImage = async (imageId) => {
    const hotspot = hotspots.value.find(h => h.id == imageId)
    IMAGE.value = { 
        id: imageId,  // 统一使用Number类型，与邻接表的key类型一致
        url: getImageFileUrl(imageId),
        description: hotspot.description
    }
    updateMapPosition(imageId)
    
    if (hotspot && hotspot.description) {
        ElMessage.success(`已到达：${hotspot.description}`)
    }
    
    setBackground()
}

const setBackground = () => {
  if (!IMAGE.value.url) {
    return
  }
  
  const loader = new THREE.TextureLoader()
   loader.setCrossOrigin('anonymous')
  
  loader.load(
    IMAGE.value.url,
    (texture) => {
      material.map = texture
      material.needsUpdate = true
      
      // 清理视频相关资源
      if (videoTexture) {
        videoTexture.dispose();
        videoTexture = null
      }
      if (vid) {
        vid.src = ''
        vid.load()
        vid = null
      }
    }
  )
}


const playVideoById = async (targetSceneId) => {
  showTag.value = false
  allowShowTag.value = false  // 开始播放视频时禁止显示标签
  
  // 停止之前的语音播放
  stopReading()
  // 重置阅读标记，新视频开始时允许阅读
 isReading= false

  
  // 根据标签ID在SpotsInfo中查找对应的描述信息
  const spotInfo = SpotsInfo.value.find(spot => String(spot.id) == String(targetSceneId))
  
  
  if (spotInfo && spotInfo.description)
   {
    descriptionText = spotInfo.description

  }
  
  // 从当前场景到目标场景，找到对应的边
  const currentSceneId = IMAGE.value.id  // 确保是Number类型
  // 从邻接表中获取当前场景的边，邻接表是一个map数据结构，key为场景ID，value为边数组，所以从邻接表找出一个元素

  const edges = AllNeighbourList.value.get(currentSceneId)
  
  if (!edges || edges.length === 0) {
    ElMessage.error('找不到对应的路径')
    return
  }
  
  const targetEdge = edges.find(e => e.sceneId == targetSceneId)
  
  if (!targetEdge) {
   
    return
  }
  
  // 如果目标场景没有视频，则直接加载背景图片并返回
  if (targetEdge.videoId == null || targetEdge.videoId === null) {
    await loadBackgroundImage(targetSceneId)
    buildHotspots()  // 重建热点标签
    allowShowTag.value = true  // 允许显示标签
    return  // 直接返回，不执行后续视频播放逻辑
  }

  ElMessage.info(`正在前往：${targetEdge.description}`)
  
  // 使用边数据中的videoId获取视频
  const videoUrl = getVideoFileUrl(targetEdge.videoId)
  VIDEO.value = { id: String(targetEdge.videoId), url: videoUrl }

  // 播放视频
  canPause.value = false
  canPlay.value = false
  vid = document.createElement('video')
  vid.crossOrigin = 'anonymous'
  vid.muted = false
  vid.loop = false
  vid.playsInline = true
  vid.preload = 'preload'  // 只预加载元数据，减少请求
  vid.className = 'video'
  
  // 视频可以播放时（只触发一次）
  vid.addEventListener('canplay', () => {
    Isplaying.value = true
    videoTexture = new THREE.VideoTexture(vid)
    videoTexture.minFilter = THREE.LinearFilter
    videoTexture.magFilter = THREE.LinearFilter
    videoTexture.format = THREE.RGBAFormat
    material.map = videoTexture
    material.needsUpdate = true
    vid.play()
    setPauseTimer()
    setPlayTimer()
    // 调用ai阅读功能（只调用一次，防止快进时重复调用）
    // 如果视频是静音的，则不播放语音
    if (!isReading && !vid.muted) {
      const textToRead = descriptionText
      if (textToRead) {
        autoRead(textToRead);
        isReading = true  // 标记已阅读
      }
    }
  }, { once: true })  // 使用 once: true 确保事件只触发一次
  

  vid.addEventListener('timeupdate', () => {
    const progress = (vid.currentTime / vid.duration) * 100
    if ( progress >= 50) {
      updateHalfMapPosition(currentSceneId)
   
    }
    console.log('播放进度:', progress.toFixed(0), '%')
  })
  
 
  vid.src = VIDEO.value.url

  // 视频播放结束时
  vid.onended = async () => {
    await loadBackgroundImage(targetSceneId)
    updateMapPosition(targetSceneId)
    buildHotspots()
    Isplaying.value = false
    allowShowTag.value = true  // 视频结束后允许显示标签
    isSpeaking.value = false  // 动效清空
    window.speechSynthesis.cancel()  // 停止阅读
    
    // 清理timer
    canPause.value = false
    canPlay.value = false
    if (pauseTimer) {
      clearTimeout(pauseTimer)
      pauseTimer = null
    }
    if (playTimer) {
      clearTimeout(playTimer)
      playTimer = null
    }
  }
}
  // 获取语音包的辅助函数
  const getVoices = () => {
    return window.speechSynthesis.getVoices()
  }
// @10.22日，增加ai阅读功能，训练好数据集，调用api接口，实现ai阅读功能，传递的是播放视频中获取的边信息，返回的是阅读内容
const autoRead = (text) => 
{
  // 确保取消之前的语音
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  currentUtterance = null
  // 如果语音包还没加载完成，等待加载
 
    const voices = getVoices()
    
    const newUtterance = new SpeechSynthesisUtterance(text)
    currentUtterance = newUtterance
    newUtterance.text = text
    
    // 直接查找并设置 Microsoft Yunxia Online (Natural) 语音包
    const yunxia = voices.find(voice => 
      voice.name.includes('Microsoft Yunxia Online (Natural)') ||
      voice.name.includes('Yunxia')
    )
    
    if (yunxia) {
      newUtterance.voice = yunxia
      newUtterance.lang = yunxia.lang || 'zh-CN'
    } else {
      newUtterance.lang = 'zh-CN'
    }
    
    newUtterance.rate = 1.0
    newUtterance.pitch = 1.0
    newUtterance.volume = 1.0

    window.speechSynthesis.speak(newUtterance);
    
    newUtterance.onstart = () => {
      // 检查语音是否被暂停，避免异步时序问题
      // 如果视频暂停了，stopReading() 会暂停语音，所以这里只需要检查语音状态
      if (!window.speechSynthesis.paused && newUtterance == currentUtterance) {
        isSpeaking.value = true  // 开始阅读时显示动画
      }
    }
    
    newUtterance.onend = () => {
      // 延迟清理，确保状态正确
        currentUtterance = null
        isSpeaking.value = false  // 阅读结束时停止动画
        isControl=false;//阅读结束时不允许再通过播放或暂停来控制动效
    
    }
}

const buildHotspots = () => {
  // 根据当前场景ID从邻接表获取可到达的场景
  const currentSceneId = Number(IMAGE.value.id)  // 确保是Number类型
  const AvailableScenes = AllNeighbourList.value.get(currentSceneId)
  
  if (!AvailableScenes || AvailableScenes.length === 0) {
    hotspots.value = []
    showTag.value = false
    ElMessage.info('当前场景没有可前往的路径')
    return
  }
  
  // 设置热点数据，包含方向向量
  hotspots.value = AvailableScenes.map(scene => ({
    id: scene.sceneId,
    videoId: scene.videoId,
    description: scene.description,
    direction: scene.direction,  // 标签的方向向量
    angle: scene.angle,  // 原始角度（用于调试）
    visible: true
  }))
  
  showTag.value = true
}


const Render = () => {
  const dir = new THREE.Vector3()
  
  const render = () => {
  
    controls.update()
    const playing = vid && !vid.paused && !vid.ended
    
    // 获取并更新相机角度（用于角度标记工具）
    camera.getWorldDirection(dir)
    const cameraAngle = Math.atan2(dir.x, -dir.z) * (180 / Math.PI)
    currentAngle.value = cameraAngle.toFixed(1)
    
    // 计算相机俯仰角（pitch）- 用于箭头显示
    // 俯仰角：相机向下看的角度，0度是水平，90度是垂直向下
    const pitch = Math.asin(dir.y) * (180 / Math.PI)
    cameraPitch.value = pitch
    
    // 视频未播放且允许显示标签时才判断
    // allowShowTag标志在视频中不能显示标签
    if (!playing && allowShowTag.value) {
      
      // 判断每个标签是否可见
      hotspots.value.forEach(h => {
        let diff = Math.abs(cameraAngle - h.angle)
        if (diff > 180)
         diff = 360 - diff  // 处理角度边界
        h.visible = diff <= 25  // ±25度范围内显示
      })
      
      showTag.value = hotspots.value.some(h => h.visible)
    } else {
      showTag.value = false
    }
    
    renderer.render(scene, camera)
    requestAnimationFrame(render)
  }
  render()
}


</script>

<style scoped>
.map-container{
  position:fixed;
  top: 20%;
  right: 5%;
  width:240px;
  border:1px solid rgba(175, 175, 175, 0.4);
  border-radius:20px;
  background-color: rgba(0,0,0,0.55);
  z-index: 1000;
  padding: 10px;
 
  backdrop-filter: blur(10px);
}
.map-content{
  position: relative;
  width: 100%;
  border-radius: 20px;
  overflow: hidden;
}
.map-content img{
  width: 100%;

  display: block;

}
.map-marker{
  position: absolute;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  
}
.map-marker-spot{
  width: 12px;
  height: 12px;
  border: 1px solid #fff;
  border-radius: 50%;
  background: #ff5b5b;

  animation: pulse 1.8s ease-in-out infinite;
}
.map-marker-info{
width:40px;
text-align: center;
  font-size: 10px;
  color: #ffffff;
  padding: 2px 6px;
  border-radius:20px;
  background: rgba(0,0,0,0.55);

}
@keyframes pulse{
  0%{
    transform: scale(1);
    opacity: 1;
  }
  70%{
    transform: scale(1.1);
    opacity: 0.2;
  }
  100%{
    transform: scale(1);
    opacity: 1;
  }
}

.map-preview-overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
}

.map-preview-container{
  width: min(720px, 92vw);
  background: rgba(20, 20, 20, 0.92);
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(255,255,255,0.08);
}

.map-preview-header{
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  font-size: 18px;
  margin-bottom: 10px;
}

.map-preview-close{
  border: none;
  background: rgba(255,255,255,0.15);
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  line-height: 32px;
  transition: background 0.2s ease, transform 0.2s ease;
}

.map-preview-close:hover{
  background: rgba(255,255,255,0.3);
  transform: rotate(90deg);
}

.map-preview-body{
  position: relative;
  width: 100%;
  border-radius: 18px;
  overflow: hidden;
  background: #000;
}

.map-preview-body img{
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

.map-preview-marker{
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}



.header {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  
  height: 90px;
  
 
  position: relative;
  z-index: 1000 !important; /* 确保导航栏在最上层 */
  pointer-events: auto !important; /* 强制允许接收点击事件 */
}

.site-title {
  position: absolute;
  left: 30px;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", sans-serif;
  z-index: 999; /* 确保标题不会遮挡导航链接 */
  pointer-events: none; /* 标题不需要接收点击事件 */
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


  position: relative;
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", sans-serif;

}

/* 导航栏和视频展示区域用弹性布局，导航栏在上面，视频展示区域在下面，column布局 */
.page-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  position: relative; /* 确保子元素定位正确 */
}

.video-area {
  flex: 1;
  margin: 10px;
  border-radius: 30px;
  overflow: hidden;
  background: #000;
  position: relative; /* 确保子元素的定位正确 */
}
/* 标签包装器 */
.tag-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

/* 导向箭头容器 */
.tag-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin-top: 5px;
  transform-style: preserve-3d;
  transition: opacity 0.3s ease;
  transform-origin: center top;
}

/* 白色箭头样式 */
.arrow {
  width: 0;
  height: 0;
  border-left: 100px solid transparent;
  border-right: 100px solid transparent;
  opacity: 0.8;
 
  border-bottom: 25px solid #ffffff;
 
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}






/* 热点标签容器 */
.tags {
  display: flex;
  flex-direction: row;
 
  justify-content: center;
  align-items: center;
  position: fixed;
  left: 50%;
  top: 55%;
  transform: translate(-50%, -50%);
  gap: 10px;
  z-index: 100;
}

/* 单个标签 - 蓝色矩形框 */
.tag {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(5px);
  color: #000000;
  padding: 12px 24px;
  border-radius: 15px;
  font-size: 16px;
  font-weight: 600;

  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);

  min-width: 80px;
  text-align: center;
}

/* 悬停效果 */
.tag:hover {
  background: #f4f5f7;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

/* 视频控制按钮（右上角） */
.video-control {
  position: fixed;
 top: 80%;
  right: 5%;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(5px);
  padding: 5px 5px;
  border-radius: 15px;
  transition: all 0.2s ease;
}

.video-control-button {
  border-radius: 15px;
  font-weight: 300px;
  transition: all 0.2s ease;
}

.video-control-button:hover {
  transform: translateY(-3px);

}

.video-control:hover {
  background: rgba(0, 0, 0, 0.1);

}

/* 角度标记辅助工具（左上角） */
.angle-marker {
  position: fixed;
  top: 80%;
  left: 5%;
  z-index: 100;
  background: rgba(0, 0, 0, 0.7);

  padding: 15px 20px;
  border-radius: 15px;

}

.angle-display {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 5px;
}

.angle-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 500;
}

.angle-value {
  color: rgb(235, 80, 129);
  font-size: 24px;
  font-weight: 700;
  font-family: 'Consolas', 'Monaco', monospace;
}

/* AI容器样式 */
.AI-container {
  position: fixed;
  top: 20%;
  left: 5%;
  border-radius: 20px;
  display: flex;
  flex-direction: row;
  align-items: center;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(5px);
  
}

.AI-logo {
  display: flex;
  align-items: center;
  justify-content: center;
}
.AI-read {
  
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 80px;

}

/* 喇叭图标 */
.speaker-icon {
  font-size: 55px;

  margin-right: 16px;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0));
}



/* 声波效果 */
.sound-waves {
  position: absolute;
  top: 10px;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;


}

.wave {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  border: 3px solid rgba(247, 181, 115, 0.6);
  border-radius: 60%;
  animation: waveExpand 1.5s ease-out infinite;
}

.wave1 {
  animation-delay: 0s;
}

.wave2 {
  animation-delay: 0.5s;
  width: 100px;
  height: 100px;
  border-color: rgba(243, 161, 10, 0.6);
}

.wave3 {
  animation-delay: 1s;
  width: 120px;
  height: 120px;
  border-color: rgba(255, 165, 0, 0.6);
}



/* 声波扩散动画 */
@keyframes waveExpand {
  0% {
    width: 80px;
    height: 80px;
   
  }
  100% {
    width: 150px;
    height: 150px;
 
  }
}


.logo{
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0));
}

.logo.floating {
  animation: float 3s ease-in-out infinite;
}

.logo:hover {
  transform: scale(1.05); 
 
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-6px);
  }
}



</style>
