<template>
  <div class="page-container">
    <div class="header-fixed">
      <h2 class="ancient-font text-center">我的名帖</h2>
      <van-icon name="setting-o" class="setting-icon" size="24" color="#4A6A6F" @click="goToSettings" />
    </div>

    <div class="user-card-container">
      <div class="user-card ancient-paper-bg">
        <!-- 油纸伞装饰背景 -->
        <div class="umbrella-decoration"></div>
        
        <div class="user-info-row">
          <div class="avatar-area">
            <div class="avatar-border-outer">
              <div class="avatar-border-inner">
                <div class="avatar">
                   <img :src="userInfo.avatar || '/images/16.jpg'" alt="Avatar" />
                </div>
              </div>
            </div>
          </div>
          <div class="user-info">
            <h3 class="username ancient-font">{{ userInfo.nickname || userInfo.username || '非遗传承人' }}</h3>
            <div class="user-tags">
              <span class="tag ancient-tag">LV.5 匠人</span>
              <span class="tag gold-tag">非遗认证</span>
            </div>
            <p class="user-id ancient-font">名帖号: {{ userInfo.id || '888888' }}</p>
          </div>
          <div class="user-arrow" @click="goToSettings">
            <span class="ancient-arrow">›</span>
          </div>
        </div>
        
        <div class="divider-line">
          <span class="divider-decoration">❖</span>
        </div>

        <div class="stats-row">
          <div class="stat-item">
            <span class="count ancient-font">12</span>
            <span class="label ancient-font">珍藏</span>
          </div>
          <div class="stat-item">
            <span class="count ancient-font">5</span>
            <span class="label ancient-font">同好</span>
          </div>
          <div class="stat-item">
            <span class="count ancient-font">3</span>
            <span class="label ancient-font">履迹</span>
          </div>
          <div class="stat-item">
            <span class="count ancient-font">8</span>
            <span class="label ancient-font">礼券</span>
          </div>
        </div>
      </div>
    </div>

    <div class="menu-container scroll-style">
      <van-cell title="我的藏品" is-link to="/orders" class="ancient-cell">
        <template #icon>
          <div class="menu-icon-box">
             <van-icon name="bag-o" color="#4A6A6F" />
          </div>
        </template>
      </van-cell>
      <van-cell title="匠心之作" is-link to="/my-works" class="ancient-cell">
        <template #icon>
          <div class="menu-icon-box">
             <van-icon name="brush-o" color="#4A6A6F" />
          </div>
        </template>
      </van-cell>
      <van-cell title="修习之路" is-link value="八成" to="/learning-progress" class="ancient-cell">
        <template #icon>
          <div class="menu-icon-box">
             <van-icon name="bookmark-o" color="#4A6A6F" />
          </div>
        </template>
      </van-cell>
      <van-cell title="收货府邸" is-link to="/address" class="ancient-cell">
        <template #icon>
          <div class="menu-icon-box">
             <van-icon name="location-o" color="#4A6A6F" />
          </div>
        </template>
      </van-cell>
    </div>

    <div class="menu-group scroll-style">
      <van-cell title="飞鸽传书" is-link @click="showContactService" class="ancient-cell">
        <template #icon>
          <div class="menu-icon-box">
             <van-icon name="service-o" color="#4A6A6F" />
          </div>
        </template>
      </van-cell>
	  
      <van-cell title="安全中心" is-link to="/security" class="ancient-cell">
        <template #icon>
          <div class="menu-icon-box">
             <van-icon name="shield-o" color="#4A6A6F" />
          </div>
        </template>
      </van-cell>
	  
	  
    </div>

    <div class="logout-container">
      <div class="logout-btn ancient-btn" @click="handleLogout">
        <span class="btn-text">退出登录</span>
        <div class="btn-border"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showDialog, showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const userInfo = ref({});

onMounted(async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  try {
    const data = await request.get(`/users/${userId}`);
    userInfo.value = data?.data ?? data;
  } catch (error) {
    console.error('Failed to fetch user info:', error);
    // Use default mock data if fetch fails (or if user doesn't exist yet in clean DB)
    userInfo.value = {
      id: userId || 888888,
      username: 'User' + (userId || 888),
      nickname: '非遗传承人',
      avatar: '/images/16.jpg'
    };
  }
});

const goToSettings = () => {
  router.push('/profile/edit');
};

const showContactService = () => {
  showDialog({
    title: '飞鸽传书',
    message: '掌柜电话：400-888-8888\n当值时间：巳时 - 酉时',
    confirmButtonColor: '#8B4513'
  });
};

const handleLogout = () => {
  showDialog({
    title: '提示',
    message: '确定要退出登录吗？',
    showCancelButton: true,
    confirmButtonColor: '#8B4513'
  }).then((action) => {
    if (action === 'confirm') {
      // Clear user data (mock)
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('userId');
      showToast({
        message: '已退出登录',
        icon: 'success'
      });
      // Redirect to login or home
      setTimeout(() => {
        router.push('/login');
      }, 1000);
    }
  });
};
</script>

<style scoped>
.page-container {
  padding-bottom: 80px;
  background: transparent;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  z-index: 0;
}

/* remove decoration to show unified background */

.header-fixed {
  background: rgba(185, 222, 201, 0.4);
  padding: 15px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 2;
}

.text-center {
  margin: 0;
  color: #4A6A6F;
  font-size: 1.4rem;
  font-weight: bold;
  letter-spacing: 2px;
}

.setting-icon {
  position: absolute;
  right: 20px;
}

.user-card-container {
  padding: 15px;
  margin-bottom: 10px;
  position: relative;
  z-index: 2;
}

.user-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(74, 106, 111, 0.1);
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(74, 106, 111, 0.1);
}

/* 宣纸纹理背景模拟 */
.ancient-paper-bg {
  background-color: rgba(255, 255, 255, 0.9);
}

.ancient-paper-bg::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.05'/%3E%3C/svg%3E");
  pointer-events: none;
}

/* 油纸伞装饰背景 */
.umbrella-decoration {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 150px;
  height: 150px;
  background: repeating-conic-gradient(
    from 0deg,
    rgba(74, 106, 111, 0.05) 0deg 10deg,
    transparent 10deg 20deg
  );
  border-radius: 50%;
  border: 1px solid rgba(74, 106, 111, 0.1);
  opacity: 0.6;
}

.user-info-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.avatar-area {
  margin-right: 15px;
}

.avatar-border-outer {
  padding: 2px;
  border: 1px solid #4A6A6F;
  border-radius: 50%;
  display: inline-block;
}

.avatar-border-inner {
  padding: 3px;
  border: 1px solid #8B8B83;
  border-radius: 50%;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.username {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  color: #333;
  letter-spacing: 1px;
}

.user-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.tag {
  font-size: 0.65rem;
  padding: 2px 8px;
  border-radius: 2px;
  border: 1px solid transparent;
}

.ancient-tag {
  color: #4A6A6F;
  border-color: #4A6A6F;
  background-color: transparent;
}

.gold-tag {
  color: #D2691E;
  border-color: #D2691E;
  background-color: transparent;
}

.user-id {
  margin: 0;
  font-size: 0.8rem;
  color: #666;
  font-family: 'Courier New', Courier, monospace; /* 模拟打字机字体或保留衬线体 */
}

.user-arrow {
  padding: 10px;
}

.ancient-arrow {
  font-size: 24px;
  color: #4A6A6F;
  font-family: serif;
  font-weight: bold;
}

.divider-line {
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(74, 106, 111, 0.2), transparent);
  margin-bottom: 20px;
  position: relative;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.divider-decoration {
  background-color: #fff; /* 这里的背景色要和卡片背景色一致 */
  padding: 0 10px;
  color: #4A6A6F;
  font-size: 12px;
  position: relative;
  top: -1px; /* 微调垂直居中 */
}

.stats-row {
  display: flex;
  justify-content: space-around;
  position: relative;
  z-index: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.count {
  font-size: 1.6rem;
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
  font-family: 'Times New Roman', serif;
}

.label {
  font-size: 0.85rem;
  color: #666;
}

.menu-container, .menu-group {
  margin: 15px;
  position: relative;
  z-index: 2;
}

/* 卷轴样式容器 */
.scroll-style {
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(74, 106, 111, 0.1);
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(74, 106, 111, 0.05);
  overflow: hidden;
}

/* 自定义 Cell 样式 */
.ancient-cell {
  background-color: transparent;
  padding: 16px;
}

.ancient-cell::after {
  border-bottom-color: rgba(74, 106, 111, 0.1) !important;
}

.menu-icon-box {
  width: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 12px;
}

.logout-container {
  margin: 40px 40px;
  position: relative;
  z-index: 2;
}

/* 印章风格按钮 */
.logout-btn.ancient-btn {
  text-align: center;
  padding: 12px;
  position: relative;
  cursor: pointer;
  background-color: transparent;
  transition: all 0.3s;
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid #4A6A6F;
  border-radius: 4px; /* 小圆角，更像印章 */
}

.btn-border::before {
  content: "";
  position: absolute;
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
  border: 1px solid #4A6A6F;
  border-radius: 2px;
}

.btn-text {
  font-size: 1.1rem;
  color: #4A6A6F;
  font-weight: bold;
  letter-spacing: 4px;
  position: relative;
  z-index: 1;
}

.logout-btn.ancient-btn:active .btn-border {
  background-color: rgba(74, 106, 111, 0.05);
}
</style>
