<template>
  <van-tabbar v-model="active" active-color="#000000" inactive-color="#333333" route class="custom-tabbar">
    <van-tabbar-item replace to="/home">
      <span>首页</span>
      <template #icon>
        <img src="/images/home.png" class="tab-icon" />
      </template>
    </van-tabbar-item>
    <van-tabbar-item replace to="/market">
      <span>市集</span>
      <template #icon>
        <img src="/images/market.png" class="tab-icon" />
      </template>
    </van-tabbar-item>
    
    <van-tabbar-item replace to="/ai" class="ai-tab">
      <template #icon>
        <div class="ai-logo-wrapper">
          <img src="/images/ai.png" class="ai-icon" />
        </div>
      </template>
    </van-tabbar-item>
    
    <van-tabbar-item replace to="/community">
      <span>伞友</span>
      <template #icon>
        <img src="/images/friend.png" class="tab-icon" />
      </template>
    </van-tabbar-item>
    <van-tabbar-item replace to="/mine">
      <span>我的</span>
      <template #icon>
        <img src="/images/mine.png" class="tab-icon" />
      </template>
    </van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref } from 'vue'

const active = ref(0)
</script>

<style scoped>
.tab-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.ai-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.ai-logo-wrapper {
  background-color: #fff;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: -20px;
  box-shadow: 0 -2px 10px rgba(74, 106, 111, 0.2);
  border: 2px solid #fff;
  position: relative;
  z-index: 10;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
  100% {
    transform: translateY(0);
  }
}

.custom-tabbar {
  background-color: #b9dec9;
  /* background-color: #000; */
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.ai-tab) {
  overflow: visible;
}

:deep(.van-tabbar-item__text) {
  font-size: 10px;
}
:deep(.van-tabbar-item--active) {
  background-color: #dfe;
}


/* Hide text for AI tab if needed, but user didn't explicitly ask to remove space, just rename/restyle */
/* But since I didn't provide text content for the AI tab item, it shouldn't render text */
</style>
