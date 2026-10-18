<script setup>
import { ElCarousel, ElCarouselItem } from 'element-plus'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转到AI聊天页面
const goToAIChat = () => {
  router.push('/ai-chat')
}

const carouselItems = ref([
  { 
    image: new URL('@/assets/gate1.jpg', import.meta.url).href, 
    title: '校园风光 - 东南门',
    description: '欢迎来到美丽的校园'
  },
  { 
    image: '/first.png', 
    title: '一号教学楼',
    description: '欢迎来到一号教学楼'
  },
  { 
    image: new URL('@/assets/library.jpg', import.meta.url).href, 
    title: '图书馆一角',
    description: '知识的海洋'
  },
])

const carouselItems1 = ref([
  { 
    image:"/first.png"
   
  },
  { 
    image: '/B2.jpg', 
  
  },
  { 
    image: '/B3.jpg'
  },
])
const news = ref([
  {
    id: 1,
    title: '我校留学生在“理解当代中国”来华留学生中文短视频大赛中喜获佳绩',

    date: '23',
    month: '十月',
    url:"https://www.xync.edu.cn/info/1371/124441.htm"
  },
  {
    id: 2,
    title: '咸阳师范学院就业服务驿站正式挂牌',
 
    date: '27',
    month: '十月',
    url:"https://www.xync.edu.cn/info/1371/124421.htm"
  },
  {
    id: 3,
    title: '我校举办陕西省2026届普通高校师范类毕业生联盟招聘会',
  
    date: '03',
    month: '十一月',
    url:"https://www.xync.edu.cn/info/1371/124421.htm"
  },
  {
    id: 4,
    title: '我校举办第三届“我说我家乡，多彩民族风”演讲比赛',

    date: '31',
    month: '十月',
    url:'https://www.xync.edu.cn/info/1371/124401.htm'
  },
  {
    id: 5,
    title: '我校学子在2025年“外研社·国才杯”“理解当代中国”全国大学生外语能力大赛陕西赛区决赛中喜获佳绩',

    date: '30',
    month: '十月',
    url:'https://www.xync.edu.cn/info/1371/124281.htm'
  },
  {
    id: 6,
    title: '我校学子在第七届丝路朗诵大赛中喜获佳绩',

    date: '29',
    month: '十月',
    url:'https://www.xync.edu.cn/info/1371/124251.htm'
  }
])


const features = ref([
  {
    id: 1,
    title: '校园漫游',
   
    description: '作为核心功能模块，负责全景场景的渲染与基础交互，包含三大子功能：场景初始化：前端加载页面时，通过 getAllImageApi、getAllSceneEdgeApi获取当前场景的基础信息（ID、名称、初始图片）与邻接表中的数据，自动构建全局可访问的无向图邻接矩阵，明确各场景间的关联关系；视角控制：基于 Three.js 实现鼠标拖拽（PC 端）控制全景视角，支持 360° 无死角查看；相邻节点标签生成：根据邻接矩阵中当前场景的相邻节点数量，动态生成交互标签，标签文字为目标场景名称（如 “三号教学楼”），点击标签后触发对应过渡视频播放，完成场景切换。',
    route: '/VR_Map',
 
  },
  {
    id: 2,
    title: '校园导航',
    
    description: '针对新生报到场景设计，支持 “起点 - 终点” 路径规划与自动游览，具体功能包括：路径生成：用户选择起点与终点后，前端基于邻接矩阵进行动态构建导航路线。自动播放：用户点击后自动加载各场景间的过渡视频，播放过程中显示 “暂停 / 继续” 按钮，支持用户手动控制播放节奏。',
    route: '/navigate',

  }
])
const gotoNews=(url)=>{
  window.open(url)


}
</script>

<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <h1 class="site-title">超维新视界</h1>
      <nav class="nav">
        <router-link to="/home" class="nav-item">首页</router-link>
        <router-link to="/VR_Map" class="nav-item">校园全景</router-link>
        <router-link to="/navigate" class="nav-item">校园导航</router-link>
      
      </nav>
    </div>

    <!-- 主内容区 -->
   <div class="main">
    <div class="first-section">
      <!-- 轮播图区域 -->
      <div class="carousel-box">
        <el-carousel 
          :interval="4000" 
          :arrow="'never'" 
          height="600px" 
          :loop="true"
        >
          <el-carousel-item v-for="item in carouselItems1" :key="item.title">
            <div class="carousel-item-wrapper">
              <img :src="item.image" class="carousel-image" :alt="item.title" />
          
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <!-- 这里是轮播图下面的区域 -->
    </div>
    <div class="title_container">
  <div class="it1">
    <p class="w1">最</p>
    <p class="w2">新</p>
  </div>
<div class="it2">
  <p class="w3">News</p>
  <p class="w4">新闻</p>
  </div>
</div>
    <!-- 这是轮播图下面的区域：左边轮播图，右边新闻列表 -->
     <div class="second-section">
      <div class="left-content">
             <div class="carousel-box">
        <el-carousel 
          :interval="4000" 
          :arrow="none"
          height="600px" 
          :loop="true"
        >
             <el-carousel-item v-for="item in carouselItems" :key="item.title">
            <div class="carousel-item-wrapper">
               <img :src="item.image" class="carousel-image" :alt="item.title" />
              <div class="carousel-caption">
                <h3 class="caption-title">{{ item.title }}</h3>
                <p class="caption-description">{{ item.description }}</p>
              </div>
            </div>
             </el-carousel-item>
           </el-carousel>
         </div>
        </div>
        <div class="right-content">
          <!-- 右侧内容区域：新闻列表 -->
          <div class="news-container">
            <div class="news-header">
         
              <h2 class="news-title-text">校园新闻</h2>
            </div>
            <div class="news-list">
              <div 
                v-for="item in news"          
                class="news-item"
                @click="gotoNews(item.url)"
              >
                <div class="news-date">
                  <div class="date-circle">
                    <span class="date-day">{{ item.date }}</span>
                    <span class="date-month">{{ item.month }}</span>
                  </div>
                </div>
                <div class="news-content">
                  <h3 class="news-title">{{ item.title }}</h3>
                </div>
              </div>
            </div>
          </div>
         </div>


     </div>

    <div class="AI-container" @click="goToAIChat">
        <div class="AI-logo" >
          <img src="/logo.jpg" class="logo" alt="AI Logo"></img>   
      </div>
      <div class="AI-name">
        <p class="AI-name-text" >咸师小匠</p>
        </div>

      </div>
    <div class="title_container">
  <div class="it1">
    <p class="w1">功</p>
    <p class="w2">能</p>
  </div>
<div class="it2">
  <p class="w3">Introduce</p>
  <p class="w4">介绍</p>
  </div>
</div>
      <div class="third-content">
        <div class="features-container">
          <div 
            v-for="feature in features" 
            class="card"
            @click="$router.push(feature.route)"
          >
            <div class="feature-content">
                <h3 class="feature-title">
                  <span class="feature-title-zh">{{ feature.title }}</span>
                  <span class="feature-action">点击体验</span>
                </h3>
              <p class="feature-description">{{ feature.description }}</p>
            </div>
          
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.logo{
  width: 80px;
  height: 80px;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
  border:1px solid rgba(0, 0, 0);

}
.logo:hover{

  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.AI-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  position: fixed;
  top: 50%;
  right: 1%;
  z-index: 999;
  cursor: pointer;
  transition: all 0.3s ease;
}

.AI-container:hover {
  transform: scale(1.05);
}

.AI-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;  
}
.AI-logo:hover{
  transform: scale(1.1);

}


.AI-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.AI-name-text {

  font-size: 16px;
  font-weight: 600;
  color: #000000;

}
.title_container {
  display: flex;
  flex-direction: row;
  justify-content: center;
  margin-bottom: 20px;
  
}

/* 最新 */
.it1 {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #bf3434;
  color: #fff;
  margin-right: 8px;
  border-radius: 2px;
  width: 50px;
}

/* 最新二字 */
.w1, .w2 {
  font-size: 32px;
  font-family: "STZhongsong", "华文中宋", serif;
}

.it2 {
  display: flex;
  flex-direction: column;
}

/* news */
.w3 {
  font-size: 60px;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1;
  color: hwb(0 82% 18% / 0.961);
}

/* 消息 */
.w4 {
  font-size: 35px;
  line-height: 1.1;
  color: #000000;
  font-weight: bold;
  font-family: "Microsoft YaHei", "微软雅黑", sans-serif;
  border-bottom: 1px solid #ec1e25;

}

.second-section {
  margin-bottom: 20px;
  display: flex;
  flex-direction: row;
  margin: 20px auto;
  width: 95%;

}

.left-content {
  flex: 2;
  display: flex;
  height: 600px;
}




.right-content {
  flex: 1;
  min-width: 300px;
  height: 600px;
}

/* 新闻列表样式 */
.news-container {
  background: #ffffff;

  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  border-left: 1px solid rgba(0, 0, 0);


}

.news-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}


.news-title-text {
  color: #000000;
  font-size: 30px;
  font-weight: 600;

  text-align: center;
}

.news-list {

overflow:auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.news-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 10px 0;
  cursor: pointer;
  transition: opacity 0.2s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.news-item:hover {
  opacity: 0.8;
}

.news-date {
  flex-shrink: 0;
}

.date-circle {
  width: 50px;
  height: 50px;
  background: #fff;
  border: 1px solid #CCCCCC;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5px;

}

.date-day {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1;
}

.date-month {
  font-size: 12px;
  color: #666;
  line-height: 1;
  margin-top: 2px;
}

.news-content {

  min-width: 0;
}

.news-title {
  color: #333;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  
}

/* 滚动条样式 */
.news-list::-webkit-scrollbar {
  width: 6px;
}

.news-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.news-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.news-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}


.third-content {
  display: flex;
  justify-content: center;
  margin: 20px auto;
  width: 100%;
}

.features-container {
  display: flex;
  flex-direction: row;
  gap: 30px;
  justify-content: center;

  width: 95%;
}

.card {


  max-width: 600px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s ease;

  border: 1px solid rgba(0, 0, 0, 0.06);
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

}

.feature-content {
  width: 100%;
}

.feature-title {
  margin: 0 0 20px 0;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 20px;
  width: 100%;
}

.feature-title-zh {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;

}

.feature-action {
  font-size: 16px;
  font-weight: 500;
  color: #000000;
  padding: 8px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.02);
}


.feature-description {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #000000;
  text-indent: 2em;
}


.home-container {
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", "Helvetica Neue", Arial, sans-serif;
  background: linear-gradient(180deg, #ffffff 0%, #ffffff 100%);
  display: flex;
  flex-direction: column;
  min-height: 100vh;

}

.main {
  display: flex;
  flex-direction: column;

}

.first-section {

  margin-bottom: 20px;
}


.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  
  height: 90px;
  
 
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
.carousel-box {
  width: 100%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}


.carousel-item-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.carousel-item-wrapper:hover .carousel-image {
  transform: scale(1.05);
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  color: #fff;
  padding:20px 20px;
  z-index: 2;
}

.caption-title {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 700;
  
  text-align: center;
}

.caption-description {
  margin: 0;
  font-size: 16px;
  font-weight: 400;
  text-align: center;
}


/* 自定义轮播图按钮样式 */
:deep(.el-carousel__arrow) {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  font-size: 18px;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 1);

  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}


</style>