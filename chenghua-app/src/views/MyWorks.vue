<template>
  <div class="page-container">
    <van-nav-bar
      title="我的作品"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    
    <div class="content">
      <van-empty v-if="works.length === 0" description="暂无作品，快去DIY吧" />
      
      <van-grid :column-num="2" :gutter="10" v-else>
        <van-grid-item v-for="work in works" :key="work.id">
          <van-image :src="work.imageData" fit="cover" height="150" @click="previewImage(work.imageData)" />
          <div class="work-date">{{ formatDate(work.createTime) }}</div>
        </van-grid-item>
      </van-grid>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showImagePreview, showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const works = ref([]);

const onClickLeft = () => {
  router.back();
};

onMounted(async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    return;
  }

  try {
    const response = await request.get(`/works/user/${userId}`);
    works.value = response?.data ?? response;
  } catch (error) {
    console.error('Failed to fetch works:', error);
  }
});

const previewImage = (url) => {
  showImagePreview([url]);
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return dateStr.split(' ')[0]; // Just show date part
};
</script>

<style scoped>
.page-container {
  background: transparent;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* remove decoration to show unified background */

.content {
  padding: 10px;
  position: relative;
  z-index: 1;
}

:deep(.van-nav-bar) {
  background-color: transparent;
}

:deep(.van-nav-bar__title) {
  color: #4A6A6F;
  font-family: var(--font-family-ancient);
  font-weight: bold;
}

:deep(.van-nav-bar__text), :deep(.van-nav-bar__icon) {
  color: #4A6A6F;
}

.work-date {
  font-size: 12px;
  color: #8B8B83;
  margin-top: 5px;
  text-align: center;
  width: 100%;
}
</style>
