<template>
  <div class="experience-page">
    <van-nav-bar 
      left-arrow 
      @click-left="$router.back()" 
      fixed 
      placeholder
      :border="false"
      class="custom-nav"
    >
      <template #title>
        <span class="ancient-font nav-title">非遗体验</span>
      </template>
    </van-nav-bar>
    
    <!-- 顶部 Banner -->
    <div class="banner-section">
      <div class="banner-content">
        <h2 class="ancient-font">匠心 · 手作</h2>
        <p>与非遗传承人面对面，亲手制作一把油纸伞</p>
      </div>
    </div>

    <div class="content-container">
      <!-- 匠人风采 -->
      <div class="section">
        <div class="section-header">
          <h3 class="ancient-font">特邀传承人</h3>
          <span class="subtitle">百年技艺 薪火相传</span>
        </div>
        <div class="craftsmen-list">
          <div class="craftsman-card" v-for="master in masters" :key="master.id">
            <div class="master-avatar">
              <img :src="master.avatar" :alt="master.name" />
            </div>
            <div class="master-info">
              <div class="name ancient-font">{{ master.name }}</div>
              <div class="title">{{ master.title }}</div>
              <div class="desc">{{ master.desc }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 体验活动 -->
      <div class="section">
        <div class="section-header">
          <h3 class="ancient-font">热门体验课程</h3>
        </div>
        <div class="activity-list">
          <div class="activity-card" v-for="item in activities" :key="item.id">
            <div class="card-image">
              <img :src="item.image" />
              <div class="tag">{{ item.duration }}分钟</div>
            </div>
            <div class="card-content">
              <h4 class="activity-title">{{ item.title }}</h4>
              <p class="activity-desc">{{ item.description }}</p>
              <div class="card-footer">
                <span class="price">¥{{ item.price }}<span class="unit">/人</span></span>
                <van-button 
                  round 
                  size="small" 
                  color="#4A6A6F" 
                  @click="openBooking(item)"
                >
                  立即预约
                </van-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预约弹窗 -->
    <van-popup 
      v-model:show="showBooking" 
      round 
      position="bottom" 
      class="booking-popup"
      closeable
    >
      <div class="popup-content">
        <h3 class="popup-title ancient-font">预约体验</h3>
        <div class="selected-activity" v-if="selectedActivity">
          <span class="label">当前选择：</span>
          <span class="value">{{ selectedActivity.title }}</span>
        </div>
        
        <van-form @submit="onSubmitBooking">
          <van-cell-group inset>
            <van-field
              v-model="bookingForm.name"
              name="name"
              label="姓名"
              placeholder="请输入您的姓名"
              :rules="[{ required: true, message: '请填写姓名' }]"
            />
            <van-field
              v-model="bookingForm.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号"
              :rules="[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]"
            />
            <van-field
              v-model="bookingForm.date"
              is-link
              readonly
              name="date"
              label="预约日期"
              placeholder="点击选择日期"
              @click="showCalendar = true"
              :rules="[{ required: true, message: '请选择日期' }]"
            />
            <van-field name="count" label="人数">
              <template #input>
                <van-stepper v-model="bookingForm.count" min="1" max="10" />
              </template>
            </van-field>
          </van-cell-group>
          
          <div class="popup-action">
            <van-button round block type="primary" native-type="submit" color="#4A6A6F">
              确认预约
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-calendar v-model:show="showCalendar" @confirm="onConfirmDate" color="#4A6A6F" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { showToast, showSuccessToast } from 'vant';

// Mock Data
const masters = [
  {
    id: 1,
    name: '李云鹤',
    title: '国家级非遗传承人',
    desc: '从事油纸伞制作50余年，精通繁复的穿线工艺。',
    avatar: '/images/avatar.svg' // Ensure these images exist or use placeholders
  },
  {
    id: 2,
    name: '张雅韵',
    title: '新锐纸伞设计师',
    desc: '致力于将现代美学融入传统制伞工艺。',
    avatar: '/images/avatar.svg'
  }
];

const activities = [
  {
    id: 1,
    title: '亲手制作一把油纸伞（入门）',
    description: '适合零基础，体验伞面绘制与组装，成品可带走。',
    price: 168,
    duration: 90,
    image: '/images/12.jpg'
  },
  {
    id: 2,
    title: '古法穿线技艺体验',
    description: '深度体验最考验耐心的穿线环节，感受匠心。',
    price: 88,
    duration: 60,
    image: '/images/13.jpg'
  },
  {
    id: 3,
    title: '大师亲授：全流程制伞课',
    description: '为期两天的深度课程，由传承人手把手教学。',
    price: 1280,
    duration: 1200,
    image: '/images/14.jpg'
  }
];

// State
const showBooking = ref(false);
const showCalendar = ref(false);
const selectedActivity = ref(null);
const bookingForm = reactive({
  name: '',
  phone: '',
  date: '',
  count: 1
});

// Methods
const openBooking = (item) => {
  selectedActivity.value = item;
  showBooking.value = true;
};

const onConfirmDate = (date) => {
  bookingForm.date = `${date.getMonth() + 1}/${date.getDate()}`;
  showCalendar.value = false;
};

const onSubmitBooking = (values) => {
  // Simulate API call
  showToast({
    type: 'loading',
    message: '提交中...',
    forbidClick: true,
  });
  
  setTimeout(() => {
    showSuccessToast('预约成功！请留意短信通知');
    showBooking.value = false;
    // Reset form
    bookingForm.name = '';
    bookingForm.phone = '';
    bookingForm.date = '';
    bookingForm.count = 1;
  }, 1000);
};
</script>

<style scoped>
.experience-page {
  min-height: 100vh;
  background-color: #F7F8FA;
  padding-bottom: 20px;
}

.custom-nav {
  --van-nav-bar-background: rgba(185, 222, 201, 0.4);
  --van-nav-bar-title-text-color: #4A6A6F;
  --van-nav-bar-icon-color: #4A6A6F;
}

.nav-title {
  font-size: 1.1rem;
  font-weight: bold;
}

.banner-section {
  height: 200px;
  background: url('/images/15.jpg') center/cover no-repeat;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-section::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.banner-content h2 {
  font-size: 2rem;
  margin-bottom: 10px;
  letter-spacing: 4px;
}

.banner-content p {
  font-size: 0.9rem;
  opacity: 0.9;
}

.content-container {
  padding: 20px 15px;
  margin-top: -20px;
  position: relative;
  z-index: 2;
  border-radius: 20px 20px 0 0;
  background: #F7F8FA;
}

.section {
  margin-bottom: 30px;
}

.section-header {
  margin-bottom: 15px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.section-header h3 {
  font-size: 1.2rem;
  color: #333;
  margin: 0;
}

.subtitle {
  font-size: 0.8rem;
  color: #999;
}

/* Craftsmen Styles */
.craftsmen-list {
  display: flex;
  overflow-x: auto;
  gap: 15px;
  padding-bottom: 10px;
  /* Hide scrollbar */
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.craftsmen-list::-webkit-scrollbar {
  display: none;
}

.craftsman-card {
  flex: 0 0 260px;
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.master-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #E0E0E0;
  flex-shrink: 0;
}

.master-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.master-info .name {
  font-size: 1.1rem;
  color: #333;
  margin-bottom: 4px;
}

.master-info .title {
  font-size: 0.8rem;
  color: #D2691E;
  margin-bottom: 6px;
  background: rgba(210, 105, 30, 0.1);
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
}

.master-info .desc {
  font-size: 0.8rem;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Activity Styles */
.activity-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}

.card-image {
  height: 160px;
  position: relative;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-image .tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}

.card-content {
  padding: 15px;
}

.activity-title {
  font-size: 1.1rem;
  color: #333;
  margin: 0 0 8px 0;
}

.activity-desc {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 15px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 1.2rem;
  color: #D2691E;
  font-weight: bold;
}

.unit {
  font-size: 0.8rem;
  font-weight: normal;
  color: #999;
}

/* Popup Styles */
.popup-content {
  padding: 20px;
}

.popup-title {
  text-align: center;
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #333;
}

.selected-activity {
  background: #F7F8FA;
  padding: 10px 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 0.9rem;
}

.selected-activity .label {
  color: #666;
}

.selected-activity .value {
  color: #333;
  font-weight: bold;
}

.popup-action {
  margin-top: 30px;
}
</style>