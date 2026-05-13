<template>
  <div class="page-container">
    <van-nav-bar
      title="编辑个人信息"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />

    <div class="form-container">
      <van-form @submit="onSubmit">
        <div class="avatar-section">
          <van-image
            round
            width="80px"
            height="80px"
            :src="userInfo.avatar || '/images/16.jpg'"
            @click="showAvatarEdit = true"
          />
          <div class="avatar-hint">点击头像修改</div>
        </div>

        <van-cell-group inset>
          <van-field
            v-model="userInfo.username"
            name="username"
            label="用户名"
            placeholder="用户名"
            readonly
          />
          <van-field
            v-model="userInfo.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称"
            :rules="[{ required: true, message: '请填写昵称' }]"
          />
        </van-cell-group>

        <div style="margin: 16px;">
          <van-button round block type="primary" color="#8B4513" native-type="submit">
            保存修改
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- Avatar Selection Dialog -->
    <van-dialog v-model:show="showAvatarEdit" title="选择头像" :show-confirm-button="false" close-on-click-overlay>
      <div class="avatar-grid">
        <div 
          v-for="(avatar, index) in defaultAvatars" 
          :key="index"
          class="avatar-option"
          @click="selectAvatar(avatar)"
        >
          <img :src="avatar" class="avatar-img" />
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const showAvatarEdit = ref(false);

const userInfo = ref({
  username: '',
  nickname: '',
  avatar: ''
});

const defaultAvatars = [
  '/images/16.jpg',
  'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
  'https://fastly.jsdelivr.net/npm/@vant/assets/leaf.jpeg',
  'https://fastly.jsdelivr.net/npm/@vant/assets/tree.jpeg'
];

onMounted(async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  try {
    const response = await request.get(`/users/${userId}`);
    userInfo.value = response?.data ?? response;
  } catch (error) {
    console.error('Failed to fetch user info:', error);
    showToast('获取用户信息失败');
  }
});

const onClickLeft = () => {
  router.back();
};

const selectAvatar = (url) => {
  userInfo.value.avatar = url;
  showAvatarEdit.value = false;
};

const onSubmit = async () => {
  const userId = localStorage.getItem('userId');
  try {
    await request.put(`/users/${userId}`, userInfo.value);
    showToast({ type: 'success', message: '保存成功' });
    setTimeout(() => {
      router.back();
    }, 1000);
  } catch (error) {
    console.error('Failed to update user:', error);
    showToast('保存失败');
  }
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

.form-container {
  margin-top: 20px;
  position: relative;
  z-index: 1;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  background: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.avatar-hint {
  font-size: 12px;
  color: #4A6A6F;
  margin-top: 8px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 20px;
}

.avatar-option {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
}

.avatar-option:hover {
  border-color: #4A6A6F;
}

.avatar-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 50%;
}

:deep(.van-nav-bar) {
  background-color: transparent;
}

:deep(.van-nav-bar__title) {
  color: #4A6A6F;
  font-family: var(--font-family-ancient);
  font-weight: bold;
}

:deep(.van-nav-bar__text),
:deep(.van-nav-bar__icon) {
  color: #4A6A6F;
}
</style>
