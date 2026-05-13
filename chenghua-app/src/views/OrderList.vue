<template>
  <div class="order-list-page">
    <van-nav-bar
      title="我的订单"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
      class="custom-nav-bar"
    />
    
    <van-tabs v-model:active="activeTab" color="#4A6A6F" title-active-color="#4A6A6F" background="transparent" sticky>
      <van-tab title="全部" name="all"></van-tab>
      <van-tab title="待付款" name="PENDING"></van-tab>
      <van-tab title="待发货" name="PAID"></van-tab>
      <van-tab title="已完成" name="COMPLETED"></van-tab>
    </van-tabs>

    <div class="order-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <span class="order-status" :class="order.status">{{ getStatusText(order.status) }}</span>
        </div>
        
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <van-image :src="item.productImage" width="60" height="60" radius="4" fit="cover" />
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-price">¥{{ item.price }} x {{ item.quantity }}</div>
            </div>
          </div>
        </div>
        
        <div class="order-footer">
          <div class="total">合计: <span class="price">¥{{ order.totalAmount }}</span></div>
          <div class="actions" v-if="order.status === 'PENDING'">
             <van-button size="small" round type="primary" color="#4A6A6F" @click="payOrder(order)">去支付</van-button>
          </div>
        </div>
      </div>
      
      <van-empty v-if="filteredOrders.length === 0" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const activeTab = ref('all');
const orders = ref([]);

const onClickLeft = () => {
  router.back();
};

const loadOrders = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    router.push('/login');
    return;
  }
  try {
    const res = await request.get(`/orders?userId=${userId}`);
    const payload = res?.data ?? res;
    orders.value = payload || [];
  } catch (e) {
    showToast('加载订单失败');
  }
};

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orders.value;
  }
  return orders.value.filter(order => order.status === activeTab.value);
});

const getStatusText = (status) => {
  const map = {
    'PENDING': '待付款',
    'PAID': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return map[status] || status;
};

const payOrder = (order) => {
  showToast({
    type: 'loading',
    message: '支付中...',
    forbidClick: true,
    duration: 1000
  });
  
  setTimeout(() => {
    order.status = 'PAID';
    showToast('支付成功');
  }, 1000);
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.order-list-page {
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

/* remove decoration to show unified background */

.custom-nav-bar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
}

:deep(.van-nav-bar__title), :deep(.van-nav-bar__text), :deep(.van-nav-bar__icon) {
  color: #4A6A6F;
}

:deep(.van-tabs__nav) {
  background: transparent;
}

.order-list {
  padding: 12px;
  position: relative;
  z-index: 1;
}

.order-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 5px rgba(74, 106, 111, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  padding-bottom: 8px;
}

.order-status {
  color: #4A6A6F;
  font-weight: bold;
}

.order-item {
  display: flex;
  margin-bottom: 10px;
}

.item-info {
  margin-left: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-name {
  font-size: 14px;
  color: #333;
}

.item-price {
  font-size: 12px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.total {
  font-size: 14px;
  color: #333;
}

.price {
  color: #D2691E;
  font-weight: bold;
  font-size: 16px;
}
</style>
