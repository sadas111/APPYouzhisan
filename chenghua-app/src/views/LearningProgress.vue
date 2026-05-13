<template>
  <div class="learning-progress-page">
    <van-nav-bar
      title="学习进度"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
      class="custom-nav-bar"
    />

    <div class="progress-container">
      <div class="summary-card">
        <h3 class="ancient-font">总体进度</h3>
        <van-circle
          v-model:current-rate="currentRate"
          :rate="80"
          :color="gradientColor"
          :text="text"
          size="120px"
          layer-color="#EAEAE0"
        />
        <p class="summary-text">已学习 12 天，累计 24 小时</p>
      </div>

      <div class="course-list">
        <h3 class="section-title ancient-font">正在学习</h3>
        
        <div class="course-item" v-for="course in courses" :key="course.id">
          <div class="course-info">
            <h4 class="course-name">{{ course.name }}</h4>
            <span class="course-status">{{ course.percentage }}%</span>
          </div>
          <van-progress 
            :percentage="course.percentage" 
            :color="course.color" 
            stroke-width="8"
            track-color="#f5f5f5"
          />
          <p class="course-desc">上次学到：{{ course.lastChapter }}</p>
        </div>
      </div>

      <div class="history-list">
        <h3 class="section-title ancient-font">已完成课程</h3>
        <van-cell-group inset class="custom-cell-group">
          <van-cell title="基础刺绣针法" value="100%" label="完成时间：2023-10-15" icon="checked" class="custom-cell" />
          <van-cell title="色彩搭配入门" value="100%" label="完成时间：2023-10-20" icon="checked" class="custom-cell" />
        </van-cell-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const onClickLeft = () => {
  router.back();
};

const currentRate = ref(0);
const gradientColor = {
  '0%': '#8B8B83',
  '100%': '#4A6A6F',
};

const text = computed(() => currentRate.value.toFixed(0) + '%');

const courses = ref([
  {
    id: 1,
    name: '高级苏绣技法',
    percentage: 65,
    lastChapter: '第三章：平针绣法详解',
    color: '#4A6A6F'
  },
  {
    id: 2,
    name: '传统图案解析',
    percentage: 30,
    lastChapter: '第一章：龙凤呈祥寓意',
    color: '#6B8E23'
  },
  {
    id: 3,
    name: '非遗文化鉴赏',
    percentage: 85,
    lastChapter: '第五章：各地绣法对比',
    color: '#D2691E'
  }
]);
</script>

<style scoped>
.learning-progress-page {
  min-height: 100vh;
  background-color: #F5F5DC;
  position: relative;
  overflow: hidden;
}

/* Background Decoration */
.learning-progress-page::before {
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

.learning-progress-page::after {
  content: "进益";
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

.custom-nav-bar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
}

:deep(.van-nav-bar__title), :deep(.van-nav-bar__text), :deep(.van-nav-bar__icon) {
  color: #4A6A6F;
}

.progress-container {
  padding: 16px;
  position: relative;
  z-index: 1;
}

.summary-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(74, 106, 111, 0.1);
}

.summary-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #4A6A6F;
}

.summary-text {
  margin-top: 15px;
  color: #666;
  font-size: 14px;
}

.section-title {
  margin: 15px 0 10px;
  color: #333;
  font-size: 18px;
  padding-left: 8px;
  border-left: 4px solid #4A6A6F;
}

.course-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 2px 5px rgba(74, 106, 111, 0.05);
}

.course-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.course-name {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.course-status {
  font-size: 14px;
  color: #999;
}

.course-desc {
  margin: 8px 0 0;
  font-size: 12px;
  color: #999;
}

.history-list {
  margin-top: 20px;
}

.custom-cell-group {
  background: transparent;
}

.custom-cell {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
}

:deep(.van-cell__value) {
  color: #4A6A6F;
}

:deep(.van-icon-checked) {
  color: #4A6A6F;
}
</style>
