<template>
  <div class="page-container">
    <div class="header-fixed">
      <h2 class="ancient-font text-center">伞友圈</h2>
    </div>
    
    <van-tabs v-model:active="active" color="#4A6A6F" title-active-color="#4A6A6F" background="transparent" sticky>
      <van-tab title="推荐">
        <div class="waterfall-container">
           <div class="post-item" v-for="(post, index) in posts" :key="post.id">
              <div class="post-img-wrapper" :style="{ height: 150 + (index % 3) * 40 + 'px' }">
                 <img :src="post.imageUrl" class="post-img-real" alt="Post Image" />
              </div>
              <div class="post-info">
                <p class="post-text">{{ post.content }}</p>
                <div class="user-row">
                   <div class="avatar">
                      <img :src="post.userAvatar" alt="User" />
                   </div>
                   <span class="username">{{ post.username }}</span>
                   <div class="like-btn" @click="handleLike(post)">
                     <van-icon name="like" /> {{ post.likeCount }}
                   </div>
                </div>
              </div>
           </div>
        </div>
      </van-tab>
      <van-tab title="话题">
        <div class="topic-list">
          <div class="topic-item" v-for="tag in topics" :key="tag">
            <span class="hash">#</span> {{ tag }}
            <van-icon name="arrow" class="arrow" />
          </div>
        </div>
      </van-tab>
    </van-tabs>
    
    <div class="fab-btn" @click="showAddPost = true">
      <van-icon name="plus" size="24" color="#fff" />
    </div>

    <van-dialog
      v-model:show="showAddPost"
      title="发布新动态"
      show-cancel-button
      confirm-button-text="发布"
      confirm-button-color="#4A6A6F"
      @confirm="handleCreatePost"
    >
      <div class="post-form">
        <van-field
          v-model="newPostContent"
          rows="3"
          autosize
          type="textarea"
          maxlength="200"
          placeholder="分享你的油纸伞故事..."
          show-word-limit
        />
        <div class="image-select-label">选择配图</div>
        <div class="image-options">
           <div 
             class="image-option" 
             v-for="(img, idx) in availableImages" 
             :key="idx"
             :class="{ active: selectedImage === img }"
             @click="selectedImage = img"
           >
             <img :src="img" />
           </div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import request from '@/utils/request'

const active = ref(0)
const posts = ref([])
const showAddPost = ref(false)
const newPostContent = ref('')
const selectedImage = ref('/images/10.png')

const availableImages = [
  '/images/10.png',
  '/images/11.png',
  '/images/12.jpg',
  '/images/13.jpg',
  '/images/14.jpg',
  '/images/15.jpg'
]

const topics = [
  '晒晒我的油纸伞',
  '非遗技艺交流',
  '周末去哪儿',
  '汉服与伞',
  '手工DIY教程'
]

const fetchPosts = async () => {
  try {
    const res = await request.get('/posts')
    posts.value = res?.data ?? res
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchPosts()
})

const handleLike = async (post) => {
  try {
    const res = await request.post(`/posts/${post.id}/like`)
    const payload = res?.data ?? res
    post.likeCount = payload?.likeCount
  } catch (e) {
    console.error(e)
  }
}

const handleCreatePost = async () => {
  if (!newPostContent.value.trim()) {
    showToast('请输入内容')
    return
  }
  
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    // router.push('/login'); // Optional: redirect or just warn
    return;
  }

  let username = '非遗爱好者';
  let userAvatar = '/images/16.jpg';

  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const userObj = JSON.parse(userStr);
      username = userObj.nickname || userObj.username || username;
      userAvatar = userObj.avatar || userAvatar;
    } catch (e) {
      console.error('Error parsing user info', e);
    }
  }

  try {
    await request.post('/posts', {
      content: newPostContent.value,
      imageUrl: selectedImage.value,
      userId: parseInt(userId),
      username: username,
      userAvatar: userAvatar
    })
    showToast('发布成功')
    newPostContent.value = ''
    showAddPost.value = false
    // Refresh list
    await fetchPosts()
  } catch (e) {
    console.error(e)
    showToast('发布失败')
  }
}
</script>

<style scoped>
.page-container {
  padding-bottom: 60px;
  background-color: linear-gradient(rgba(255,255,255,0.85), rgba(221, 255, 238, 0.8));
  min-height: 100vh;
  position: relative;
}

/* Background Decoration */
.page-container::before {
  content: "";
  position: absolute;
  top: -100px;
  right: -50px;
  width: 300px;
  height: 300px;
  background: rgba(74, 106, 111, 0.1);
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
}

.page-container::after {
  content: "伞友";
  position: absolute;
  top: 10%;
  left: 5%;
  font-size: 8rem;
  color: rgba(74, 106, 111, 0.03);
  writing-mode: vertical-rl;
  font-weight: bold;
  pointer-events: none;
  z-index: 0;
  font-family: var(--font-family-ancient);
}

.header-fixed {
  background: rgba(185, 222, 201, 0.4);
  padding: 10px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(5px);
  box-shadow: 0 1px 4px rgba(74, 106, 111, 0.05);
}

.text-center {
  margin: 0;
  color: #4A6A6F;
}

:deep(.van-tabs__nav) {
  background: transparent;
}

.waterfall-container {
  padding: 10px;
  column-count: 2;
  column-gap: 10px;
  position: relative;
  z-index: 1;
}

.post-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 10px;
  break-inside: avoid;
  box-shadow: 0 2px 5px rgba(74, 106, 111, 0.1);
}

.post-img-wrapper {
  background: #f5f5f5;
  width: 100%;
  overflow: hidden;
}

.post-img-real {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.post-info {
  padding: 10px;
}

.post-text {
  margin: 0 0 10px 0;
  font-size: 0.85rem;
  line-height: 1.4;
  color: #333;
}

.user-row {
  display: flex;
  align-items: center;
  font-size: 0.75rem;
  color: #666;
}

.avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-right: 5px;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  flex: 1;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff4d4f; /* Red color for visibility */
  font-weight: bold;
  font-size: 0.9rem;
  cursor: pointer;
  transition: transform 0.1s;
}

.like-btn:active {
  transform: scale(1.2); /* Scale up on click */
}

.like-btn .van-icon {
  font-size: 1.1rem; /* Larger icon */
}

.topic-list {
  padding: 10px;
  position: relative;
  z-index: 1;
}

.topic-item {
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  font-weight: bold;
  color: #333;
  box-shadow: 0 2px 5px rgba(74, 106, 111, 0.05);
}

.hash {
  color: #4A6A6F;
  margin-right: 5px;
  font-size: 1.2rem;
}

.arrow {
  margin-left: auto;
  color: #ccc;
}

.fab-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 50px;
  height: 50px;
  background: #4A6A6F;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.4);
  z-index: 99;
  cursor: pointer;
}

.post-form {
  padding: 10px;
}

.image-select-label {
  margin: 10px 0 5px;
  font-size: 0.9rem;
  color: #666;
}

.image-options {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.image-option {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  border: 2px solid transparent;
  flex-shrink: 0;
}

.image-option.active {
  border-color: #4A6A6F;
}

.image-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
