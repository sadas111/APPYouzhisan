<template>
  <div class="register-page">
    <div class="bg-decoration">
      <div class="circle-bg"></div>
      <div class="text-bg ancient-font">结缘</div>
    </div>
    
    <div class="register-container">
      <div class="register-header">
        <h2 class="ancient-font main-title">加入撑花</h2>
        <p class="sub-title">传承非遗 · 共赏伞韵</p>
      </div>
      
      <div class="form-box">
        <van-form @submit="onSubmit">
          <div class="input-group">
            <van-field
              v-model="username"
              name="username"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请填写手机号' }]"
              class="custom-input"
            >
              <template #left-icon>
                <van-icon name="user-o" color="#4A6A6F" />
              </template>
            </van-field>
          </div>
          
          <div class="input-group">
            <van-field
              v-model="password"
              type="password"
              name="password"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请填写密码' }]"
              class="custom-input"
            >
              <template #left-icon>
                <van-icon name="lock" color="#4A6A6F" />
              </template>
            </van-field>
          </div>

          <div class="input-group">
            <van-field
              v-model="confirmPassword"
              type="password"
              name="confirmPassword"
              placeholder="请确认密码"
              :rules="[{ required: true, message: '请确认密码' }]"
              class="custom-input"
            >
              <template #left-icon>
                <van-icon name="lock" color="#4A6A6F" />
              </template>
            </van-field>
          </div>

          <div class="action-btn">
            <van-button 
              block 
              type="primary" 
              native-type="submit" 
              color="#4A6A6F"
              class="register-btn"
            >
              注 册
            </van-button>
          </div>
        </van-form>
        
        <div class="links">
          <router-link to="/login">已有账号？去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'

const router = useRouter()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')

const onSubmit = async (values) => {
  // Phone validation
  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!phoneRegex.test(values.username)) {
    showToast('请输入正确的11位手机号');
    return;
  }

  // Basic illegal character check
  const illegalPattern = /[<>]/;
  if (illegalPattern.test(values.username) || illegalPattern.test(values.password) || illegalPattern.test(values.confirmPassword)) {
    showToast('输入包含非法字符');
    return;
  }

  if (values.password !== values.confirmPassword) {
    showToast('两次密码不一致')
    return
  }
  
  try {
    const response = await request.post('/users', {
      username: values.username,
      password: values.password,
      nickname: '撑花客' + Math.floor(Math.random() * 1000),
      role: 'USER',
      avatar: '/images/16.jpg'
    });
    
    if (response) {
      showToast({ type: 'success', message: '注册成功' });
      setTimeout(() => {
        router.push('/login');
      }, 1000);
    }
  } catch (error) {
    console.error('Registration failed:', error);
    showToast('注册失败，请稍后重试');
  }
}
</script>

<style scoped>
.register-page {
  height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.bg-decoration {
  display: none;
}

/* remove decoration to show unified background */

.register-container {
  position: relative;
  z-index: 1;
  width: 85%;
  max-width: 400px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.main-title {
  font-size: 2.2rem;
  color: #4A6A6F;
  margin: 0 0 10px 0;
  letter-spacing: 4px;
}

.sub-title {
  color: #8B8B83;
  font-size: 0.9rem;
  letter-spacing: 2px;
}

.form-box {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  padding: 30px 20px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(74, 106, 111, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.input-group {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.custom-input {
  background: transparent;
  padding: 15px;
}

.register-btn {
  height: 48px;
  font-size: 1.1rem;
  letter-spacing: 4px;
  border-radius: 12px;
  margin-top: 10px;
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.3);
}

.links {
  margin-top: 20px;
  text-align: center;
  font-size: 0.85rem;
  color: #8B8B83;
}

.links a {
  color: #4A6A6F;
  text-decoration: none;
}
</style>
