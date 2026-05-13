<template>
  <div class="guide-page">
    <ParticleBackground theme="fresh" :particleCount="70" class="guide-particles" />
    
    <div class="content">
      <div class="logo-area">
        <InkLogo :size="180" class="mb-4" />
        <p class="subtitle ancient-font">数筑古韵 · 撑花传遗</p>
      </div>
      
      <div class="action-area">
        <van-button 
          type="primary" 
          round 
          block 
          class="enter-btn ancient-font" 
          color="#3AB08C"
          @click="enterApp"
        >
          开启传承之旅
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import InkLogo from '@/components/InkLogo.vue'
import ParticleBackground from '../components/ParticleBackground.vue'

const router = useRouter()

const enterApp = () => {
  router.push('/login')
}
</script>

<style scoped>
.guide-page {
  height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

.guide-particles {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 4rem 2rem;
  box-sizing: border-box;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10vh;
}

.subtitle {
  font-size: 1.2rem;
  color: #8B8B83;
  letter-spacing: 4px;
  margin-top: 20px;
}

.action-area {
  width: 100%;
  margin-bottom: 2rem;
}

.enter-btn {
  height: 50px;
  font-size: 1.2rem;
  box-shadow: 0 4px 15px rgba(74, 106, 111, 0.3);
  letter-spacing: 2px;
}

.mb-4 {
  margin-bottom: 1rem;
}
</style>
