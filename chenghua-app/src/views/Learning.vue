<template>
  <div class="page-container">
    <!-- <div class="header-fixed">
      <h2 class="ancient-font text-center">匠心学伞</h2>
    </div> -->
	<van-nav-bar
	  left-arrow 
	  @click-left="$router.back()" 
	  fixed 
	  placeholder
	  :border="false"
	  class="custom-nav"
	>
	  <template #title>
	    <span class="ancient-font nav-title">匠心学伞</span>
	  </template>
	</van-nav-bar>
    
    <div class="video-section">
      <div class="video-wrapper">
         <video controls class="real-video" poster="/images/17.jpg">
           <source src="/images/1.mp4" type="video/mp4">
           您的浏览器不支持 video 标签。
         </video>
      </div>
      <p class="current-step-title ancient-font text-center">
        工序 {{ active + 1 }}: {{ steps[active].title }}
      </p>
    </div>

    <div class="steps-container">
      <van-steps direction="vertical" :active="active" active-color="#4A6A6F">
        <van-step v-for="(step, index) in steps" :key="index">
          <div class="step-content" @click="active = index">
            <h3 class="step-title ancient-font">{{ step.title }}</h3>
            <p class="step-desc">{{ step.desc }}</p>
            <van-tag v-if="index === active" type="primary" plain size="mini">当前学习</van-tag>
          </div>
        </van-step>
      </van-steps>
    </div>

    <div class="interactive-area" v-if="active === 1">
      <div class="interactive-box">
        <p class="hint">请尝试滑动屏幕模拟削骨</p>
        <div class="gesture-target"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

console.log('Learning.vue loaded')

const active = ref(0)
const steps = [
  { title: '选竹', desc: '选用5年以上楠竹，质地坚韧，富有弹性。需在冬季砍伐，防虫蛀。' },
  { title: '削骨', desc: '手工削制伞骨，要求粗细均匀，光滑无刺。这是决定伞架质量的关键。' },
  { title: '钻孔', desc: '在伞骨上钻出细小的孔洞，用于穿线组装，精度要求极高。' },
  { title: '拼架', desc: '将伞骨组装成伞架，结构需稳固，开合顺畅。' },
  { title: '糊纸', desc: '选用特制皮纸，用柿子漆粘贴在伞架上，需平整无褶皱。' },
  { title: '绘画', desc: '在伞面上绘制水墨丹青，赋予油纸伞文化内涵。' },
  { title: '上油', desc: '涂刷熟桐油，增强防水性能，使伞面通透光亮。' },
  { title: '穿线', desc: '用五色丝线穿插在伞骨之间，既加固又美观，寓意“五子登科”。' }
]
</script>

<style scoped>
.page-container {
  padding-bottom: 80px;
  background: transparent;
  min-height: 100vh;
  position: relative;
}

/* remove decoration to show unified background */

.header-fixed, .video-section, .steps-container {
  position: relative;
  z-index: 1;
}

.header-fixed {
  position: sticky;
  top: 0;
  background: rgba(185, 222, 201, 0.4);
  padding: 10px 0;
  z-index: 10;
}

.custom-nav {
  --van-nav-bar-background: rgba(185, 222, 201, 0.4);
  --van-nav-bar-title-text-color: #4A6A6F;
  --van-nav-bar-icon-color: #4A6A6F;
}

.text-center {
  text-align: center;
  color: #4A6A6F;
  margin: 0;
}

.video-section {
  padding: 10px 20px;
}

.video-wrapper {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.1);
  background: #000;
}

.real-video {
  width: 100%;
  display: block;
  max-height: 240px;
  object-fit: contain;
}

.current-step-title {
  margin-top: 15px;
  margin-bottom: 5px;
  font-size: 1.1rem;
  font-weight: bold;
}

.steps-container {
  padding: 10px 20px;
  background: #fff;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  min-height: 300px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.step-content {
  padding: 5px 0;
}

.step-title {
  margin: 0 0 5px 0;
  font-size: 1rem;
}

.step-desc {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
  line-height: 1.4;
}

.interactive-area {
  position: fixed;
  bottom: 70px;
  left: 20px;
  right: 20px;
  z-index: 20;
}

.interactive-box {
  background: rgba(0,0,0,0.8);
  color: #fff;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}

.gesture-target {
  height: 50px;
  border: 2px dashed #aaa;
  margin-top: 10px;
  border-radius: 4px;
}
</style>
