<template>
  <div class="home-page">
    <!-- Header -->
    <div class="header">
      <div class="header-left" @click="showCityPicker = true">
        <span class="location">{{  }} <van-icon name="arrow-down" /></span>
      </div>
      <div class="header-center">
        <span class="app-name ancient-font">撑花</span>
      </div>
      <div class="header-right">
      </div>
    </div>

    <!-- Banner -->
    <div class="banner-section">
      <van-swipe class="my-swipe" :autoplay="3000" indicator-color="#4A6A6F">
        <van-swipe-item v-for="(image, index) in images" :key="index">
          <div class="banner-card">
            <img :src="image" class="banner-img" />
            <div class="banner-overlay">
              <div class="banner-title ancient-font">千年非遗 · 油纸伞</div>
              <div class="banner-subtitle">探寻东方美学之源</div>
            </div>
          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 核心功能入口 -->
    <div class="core-functions">
      <div class="function-card" @click="$router.push('/history')">
        <div class="card-icon icon-history"></div>
        <div class="card-text">
          <span class="card-title">溯源</span>
        </div>
      </div>
      <div class="function-card" @click="$router.push('/learning')">
        <div class="card-icon icon-learning">
        </div>
        <div class="card-text">
          <span class="card-title">学伞</span>
        </div>
      </div>
      <div class="function-card" @click="$router.push('/diy')">
        <div class="card-icon icon-diy">
        </div>
        <div class="card-text">
          <span class="card-title">DIY</span>
        </div>
      </div>
      <div class="function-card" @click="$router.push('/experience')">
        <div class="card-icon icon-market">
        </div>
        <div class="card-text">
          <span class="card-title">体验</span>
        </div>
      </div>
    </div>

    <!-- 非遗体验活动 (新增) -->
    <div class="section experience-section">
      <div class="section-header">
        <h3 class="section-title ancient-font">非遗体验</h3>
        <span class="more-link">更多活动 ></span>
      </div>
      
      <div class="activity-list">
        <div class="activity-card" v-for="item in activities" :key="item.id">
          <div class="activity-img">
            <img :src="item.image" />
            <div class="activity-tag" :class="{ free: item.price === 0 }">
              {{ item.price === 0 ? '免费体验' : '¥' + item.price }}
            </div>
          </div>
          <div class="activity-info">
            <h4 class="ancient-font">{{ item.title }}</h4>
            <p class="desc">{{ item.desc }}</p>
            <div class="activity-action">
              <span class="location-tag"><van-icon name="location-o" /> {{ item.location }}</span>
              <van-button 
                size="small" 
                round 
                :color="item.price === 0 ? '#4A6A6F' : '#D2691E'"
                @click="handleBook(item)"
              >
                {{ item.price === 0 ? '立即预约' : '购买体验' }}
              </van-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 每日一伞 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title ancient-font">每日一伞</h3>
        <span class="more-link">查看更多 ></span>
      </div>
      <div class="daily-card" @click="$router.push('/market')">
        <div class="card-img-wrapper">
           <img src="/images/1.jpg" alt="Daily Product" class="daily-img" />
           <div class="daily-placeholder-overlay">
              <span class="card-tag">大师亲制</span>
           </div>
        </div>
        <div class="card-info">
           <div class="info-top">
             <h4 class="ancient-font">经典·水墨竹韵</h4>
             <span class="price">¥268.00</span>
           </div>
           <p class="desc">承载千年匠心，72道工序纯手工制作。</p>
        </div>
      </div>
    </div>
    
    <!-- City Picker Popup -->
    <van-popup v-model:show="showCityPicker" round position="bottom">
      <van-picker
        :columns="cityColumns"
        @cancel="showCityPicker = false"
        @confirm="onCityConfirm"
        title="选择城市"
      />
    </van-popup>

    <!-- 支付/预约弹窗 (Mock) -->
    <van-action-sheet v-model:show="showPayment" :title="currentActivity?.price === 0 ? '预约确认' : '收银台'">
      <div class="payment-content">
        <div class="order-info">
          <p class="order-title">{{ currentActivity?.title }}</p>
          <p class="order-price" :class="{ free: currentActivity?.price === 0 }">
            {{ currentActivity?.price === 0 ? '免费' : '¥' + currentActivity?.price.toFixed(2) }}
          </p>
        </div>

        <div class="payment-methods" v-if="currentActivity?.price > 0">
          <van-radio-group v-model="paymentMethod">
            <van-cell-group inset>
              <van-cell clickable @click="paymentMethod = 'wechat'">
                <template #title>
                  <div class="pay-method-item">
                    <van-icon name="wechat-pay" color="#07c160" size="24" />
                    <span>微信支付</span>
                  </div>
                </template>
                <template #right-icon>
                  <van-radio name="wechat" checked-color="#07c160" />
                </template>
              </van-cell>
              <van-cell clickable @click="paymentMethod = 'alipay'">
                <template #title>
                  <div class="pay-method-item">
                    <van-icon name="alipay" color="#1677ff" size="24" />
                    <span>支付宝</span>
                  </div>
                </template>
                <template #right-icon>
                  <van-radio name="alipay" checked-color="#1677ff" />
                </template>
              </van-cell>
            </van-cell-group>
          </van-radio-group>
        </div>

        <div class="action-area">
          <van-button round block type="primary" :color="currentActivity?.price === 0 ? '#4A6A6F' : '#D2691E'" @click="processPayment">
            {{ currentActivity?.price === 0 ? '确认预约' : '立即支付' }}
          </van-button>
        </div>
      </div>
    </van-action-sheet>

    <!-- 支付成功弹窗 -->
    <van-dialog v-model:show="showSuccess" :show-confirm-button="false">
      <div class="success-dialog">
        <van-icon name="checked" color="#07c160" size="64" />
        <h3>{{ currentActivity?.price === 0 ? '预约成功' : '支付成功' }}</h3>
        <p>凭二维码到店核销体验</p>
        <van-button round block type="primary" color="#4A6A6F" size="small" @click="showSuccess = false">确定</van-button>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showLoadingToast, showToast } from 'vant'

const currentCity = ref('成都')
const showCityPicker = ref(false)
const cityColumns = [
  { text: '成都', value: 'Chengdu' },
  { text: '泸州', value: 'Luzhou' },
  { text: '杭州', value: 'Hangzhou' },
  { text: '婺源', value: 'Wuyuan' },
]

const onCityConfirm = ({ selectedOptions }) => {
  currentCity.value = selectedOptions[0].text
  showCityPicker.value = false
}

const images = [
  '/images/2.jpg',
  '/images/3.jpg',
  '/images/1.jpg'
]

// Mock Activities Data
const activities = ref([
  {
    id: 1,
    title: '亲手绘伞 · 非遗体验课',
    desc: '资深传承人手把手教学，体验伞面绘制乐趣',
    price: 98.0,
    image: '/images/3.jpg',
    location: '文殊院店'
  },
  {
    id: 2,
    title: '油纸伞制作工艺展览',
    desc: '了解72道工序，近距离感受非遗魅力',
    price: 0,
    image: '/images/2.jpg',
    location: '锦里旗舰店'
  }
])

// Payment Logic
const showPayment = ref(false)
const showSuccess = ref(false)
const currentActivity = ref(null)
const paymentMethod = ref('wechat')

const handleBook = (item) => {
  currentActivity.value = item
  showPayment.value = true
}

const processPayment = () => {
  showPayment.value = false
  const loadingText = currentActivity.value.price === 0 ? '正在预约...' : '正在支付...'
  
  const toast = showLoadingToast({
    message: loadingText,
    forbidClick: true,
    duration: 1500
  })

  setTimeout(() => {
    showSuccess.value = true
  }, 1500)
}
</script>

<style scoped>
.home-page {
  padding-bottom: 70px;
  /* background: #f9f4dc; */
  background: linear-gradient(rgba(255,255,255,0.85), rgba(221, 255, 238, 0.8));
  min-height: 100vh;
  position: relative;
}

/* Background Decoration from Login Style */
.home-page::before {
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

.home-page::after {
  content: "";
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

.header, .banner-section, .core-functions, .section {
  position: relative;
  z-index: 1;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: rgba(185, 222, 201, 0.4);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.header-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.app-name {
  font-size: 1.4rem;
  font-weight: bold;
  color: #333;
}

.banner-section {
  padding: 0 15px;
  margin-top: 10px;
}

.banner-card {
  position: relative;
  height: 180px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 20px 15px;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
  color: #fff;
}

.banner-title {
  font-size: 1.2rem;
  margin-bottom: 4px;
}

.banner-subtitle {
  font-size: 0.8rem;
  opacity: 0.9;
}

.core-functions {
  display: flex;
  justify-content: space-between;
  padding: 20px 15px;
  gap: 10px;
}

.function-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  color: #fff;
  font-size: 1.2rem;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.icon-history { background: url('/images/18.jpg') center / cover no-repeat; }
.icon-learning { background: url('/images/19.jpg') center / cover no-repeat; }
.icon-diy { background: url('/images/20.jpg') center / cover no-repeat; }
.icon-market { background: url('/images/21.jpg') center / cover no-repeat; }

.card-title {
  font-size: 0.9rem;
  color: #333;
  font-weight: 500;
}

.section {
  margin: 15px;
  padding: 15px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title {
  margin: 0;
  font-size: 1.1rem;
  border-left: 4px solid #4A6A6F;
  padding-left: 10px;
}

.more-link {
  font-size: 0.8rem;
  color: #999;
}

/* Activity List Styles */
.activity-card {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.activity-card:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.activity-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

.activity-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-tag {
  position: absolute;
  top: 0;
  left: 0;
  background: #D2691E;
  color: #fff;
  font-size: 0.6rem;
  padding: 2px 6px;
  border-radius: 0 0 8px 0;
}

.activity-tag.free {
  background: #4A6A6F;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.activity-info h4 {
  margin: 0;
  font-size: 1rem;
  color: #333;
}

.activity-info .desc {
  font-size: 0.8rem;
  color: #666;
  margin: 4px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-tag {
  font-size: 0.75rem;
  color: #999;
}

/* Daily Card Styles */
.daily-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-img-wrapper {
  height: 180px;
  position: relative;
}

.daily-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.daily-placeholder-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  padding: 10px;
  background: linear-gradient(to top, rgba(0,0,0,0.35), transparent 60%);
}

.card-tag {
  display: inline-block;
  color: #fff;
  background: rgba(74,106,111,0.9);
  font-size: 12px;
  border-radius: 12px;
  padding: 4px 10px;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.info-top h4 {
  margin: 0;
  font-size: 1rem;
  max-width: 70%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price {
  color: #D2691E;
  font-weight: bold;
}

/* Payment Sheet */
.payment-content {
  padding: 20px 15px;
}

.order-info {
  text-align: center;
  margin-bottom: 20px;
}

.order-title {
  font-size: 1rem;
  color: #333;
  margin-bottom: 5px;
}

.order-price {
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
}

.order-price.free {
  color: #4A6A6F;
}

.pay-method-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-area {
  margin-top: 30px;
}

.success-dialog {
  padding: 30px 20px;
  text-align: center;
}

.success-dialog h3 {
  margin: 10px 0;
  color: #333;
}

.success-dialog p {
  color: #999;
  font-size: 0.9rem;
  margin-bottom: 20px;
}
</style>
