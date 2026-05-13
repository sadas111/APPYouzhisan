<template>
  <div class="page-container">
    <div class="header-fixed">
      <h2 class="ancient-font text-center">撑花市集</h2>
      <div class="cart-icon" @click="showCart = true">
         <van-badge :content="totalCount" v-if="totalCount > 0">
           <van-icon name="shopping-cart-o" size="24" color="#4A6A6F" />
         </van-badge>
         <van-icon name="shopping-cart-o" size="24" color="#4A6A6F" v-else />
      </div>
    </div>
    
    <div class="product-grid">
      <div class="product-card" v-for="(item, index) in products" :key="index">
        <div class="product-img">
          <img :src="item.imageUrl || item.image_url" :alt="item.title" v-if="item.imageUrl || item.image_url" class="real-img" />
          <div class="tag" v-if="item.hot">热销</div>
        </div>
        <div class="product-info">
          <h3 class="product-title ancient-font">
            {{ item.title }}
            <span class="artisan-tag" v-if="item.artisan">工匠：{{ item.artisan }}</span>
          </h3>
          <p class="product-desc">{{ item.desc }}</p>
          <div class="product-footer">
            <span class="price">¥{{ item.price }}</span>
            <!-- <span class="artisan-name" v-if="item.artisan">{{ item.artisan }}</span> -->
            
            <div class="action-area" v-if="getProductQuantity(item) > 0">
              <div class="minus-btn" @click.stop="decreaseQuantityHandler(item)">
                <van-icon name="minus" size="12" color="#4A6A6F" />
              </div>
              <span class="qty-display">{{ getProductQuantity(item) }}</span>
              <div class="add-btn" @click.stop="addToCartHandler(item)">
                <van-icon name="plus" size="12" color="#fff" />
              </div>
            </div>
            <div class="add-btn-wrapper" v-else @click="addToCartHandler(item)">
              <div class="add-btn">
                <van-icon name="plus" size="12" color="#fff" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Cart Popup -->
    <van-popup
      v-model:show="showCart"
      position="bottom"
      round
      :style="{ height: '60%' }"
    >
      <div class="cart-popup">
        <div class="cart-header">
          <span class="ancient-font">购物车</span>
          <span class="clear-btn" @click="clearCartHandler">清空</span>
        </div>
        
        <div class="cart-content" v-if="cartItems.length > 0">
          <div class="cart-item" v-for="item in cartItems" :key="item.id">
            <div class="item-info">
              <div class="item-name">{{ item.title }}</div>
              <div class="item-price">¥{{ item.price }}</div>
            </div>
            <div class="item-action">
              <van-stepper 
                v-model="item.quantity" 
                theme="round" 
                button-size="22" 
                disable-input
                @change="(val) => onQuantityChange(item, val)"
              />
            </div>
          </div>
        </div>
        
        <div class="empty-cart" v-else>
          <van-empty description="购物车是空的" />
        </div>

        <div class="cart-footer">
          <div class="total-info">
            <span>合计:</span>
            <span class="total-price">¥{{ totalPrice }}</span>
          </div>
          <van-button 
            color="#4A6A6F" 
            round 
            block 
            @click="submitOrder"
            :disabled="cartItems.length === 0"
          >
            立即支付
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 支付弹窗 (Mock) -->
    <van-action-sheet v-model:show="showPayment" title="收银台">
      <div class="payment-content">
        <div class="order-info">
          <p class="order-title">商品总额</p>
          <p class="order-price">¥{{ totalPrice }}</p>
        </div>

        <div class="payment-methods">
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
          <van-button round block type="primary" color="#D2691E" @click="processPayment">
            确认支付 ¥{{ totalPrice }}
          </van-button>
        </div>
      </div>
    </van-action-sheet>

    <!-- 支付成功弹窗 -->
    <van-dialog v-model:show="showSuccess" :show-confirm-button="false">
      <div class="success-dialog">
        <van-icon name="checked" color="#07c160" size="64" />
        <h3>支付成功</h3>
        <p>订单已生成，商家将尽快发货</p>
        <van-button round block type="primary" size="small" @click="handleSuccessClose">确定</van-button>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { showToast, showDialog, showLoadingToast } from 'vant';
import { useCartStore } from '@/store/cart';
import { useRouter } from 'vue-router';
import request from '@/utils/request';

const router = useRouter();
const products = ref([]);
const loading = ref(false);
const showCart = ref(false);

// Payment Logic State
const showPayment = ref(false);
const showSuccess = ref(false);
const paymentMethod = ref('wechat');

// Use Store
const cartStore = useCartStore();

const cartItems = computed(() => cartStore.state.items);
const totalCount = cartStore.totalCount;
const totalPrice = cartStore.totalPrice;

const fetchProducts = async () => {
  loading.value = true;
  // Use static data as requested by user
  products.value = [
    {
      id: 1,
      title: '梅兰竹菊',
      desc: '纯手工绘制，桐油防水',
      price: 299,
      imageUrl: '/images/4.jpg',
      category: 'Classic',
      hot: false,
      artisan: '许玲'
    },
    {
      id: 2,
      title: '青花瓷韵',
      desc: '蓝白相间，古朴典雅',
      price: 268,
      imageUrl: '/images/5.jpg',
      category: 'Classic',
      hot: false,
      artisan: '毕六福'
    },
    {
      id: 3,
      title: '粉黛佳人',
      desc: '少女情怀，桃花灼灼',
      price: 188,
      imageUrl: '/images/6.jpg',
      category: 'Modern',
      hot: true,
      artisan: '余万伦'
    },
    {
      id: 4,
      title: '水墨山水',
      desc: '大师手笔，意境深远',
      price: 399,
      imageUrl: '/images/7.jpg',
      category: 'Master',
      hot: false,
      artisan: '闻士善'
    },
    {
      id: 5,
      title: '锦绣河山',
      desc: '色彩艳丽，喜庆吉祥',
      price: 328,
      imageUrl: '/images/8.jpg',
      category: 'Classic',
      hot: false,
      artisan: '张朝全'
    },
    {
      id: 6,
      title: '素雅清风',
      desc: '简约大方，日常百搭',
      price: 158,
      imageUrl: '/images/9.jpg',
      category: 'Modern',
      hot: true,
      artisan: '曹正新'
    }
  ];
  loading.value = false;
};

onMounted(() => {
  fetchProducts();
});

const getProductQuantity = (product) => {
  const item = cartItems.value.find(i => i.id === product.id);
  return item ? item.quantity : 0;
};

const decreaseQuantityHandler = (item) => {
  cartStore.decreaseQuantity(item.id);
};

const addToCartHandler = (item) => {
  cartStore.addToCart(item);
  showToast('已加入购物车');
};

const clearCartHandler = () => {
  showDialog({
    title: '提示',
    message: '确认清空购物车吗？',
    showCancelButton: true,
  }).then(() => {
    cartStore.clearCart();
  }).catch(() => {});
};

const onQuantityChange = (item, val) => {
  // Logic for quantity change if needed
};

const submitOrder = () => {
  if (cartItems.value.length === 0) return;
  showPayment.value = true;
};

const processPayment = async () => {
  showPayment.value = false;
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    return;
  }
  const selectedAddressStr = localStorage.getItem('selectedAddress');
  let addressId = null;
  if (selectedAddressStr) {
    try {
      addressId = JSON.parse(selectedAddressStr).id;
    } catch (e) {}
  }
  const items = cartItems.value.map(it => ({
    productId: it.id,
    productName: it.title,
    productImage: it.imageUrl,
    price: it.price,
    quantity: it.quantity
  }));
  showLoadingToast({
    message: '支付中...',
    forbidClick: true,
    duration: 1500
  });
  try {
    await request.post('/orders', {
      userId: parseInt(userId),
      addressId,
      totalAmount: parseFloat(totalPrice.value),
      items
    });
    showSuccess.value = true;
  } catch (error) {
    showToast('订单创建失败，请稍后再试');
  }
};

const handleSuccessClose = () => {
  showSuccess.value = false;
  cartStore.clearCart();
  showCart.value = false;
  router.push('/orders');
};
</script>

<style scoped>
.page-container {
  padding-bottom: 60px;
  background: transparent;
  min-height: 100vh;
  position: relative;
}

/* remove decoration to show unified background */

.header-fixed, .product-grid {
  position: relative;
  z-index: 1;
}

.header-fixed {
  position: sticky;
  top: 0;
  background: rgba(185, 222, 201, 0.4); /* Match bg color */
  padding: 10px 0;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 5px rgba(74, 106, 111, 0.05);
}

.text-center {
  margin: 0;
  color: #4A6A6F;
}

.cart-icon {
  position: absolute;
  right: 20px;
  cursor: pointer;
}

.product-grid {
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.product-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(74, 106, 111, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  flex-direction: column;
}

.product-img {
  height: 160px;
  background: #f5f5f5;
  position: relative;
  overflow: hidden;
}

.real-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #D2691E;
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 4px;
}

.product-info {
  padding: 10px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-title {
  margin: 0 0 5px 0;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.artisan-tag {
  font-size: 0.7rem;
  color: #4A6A6F;
  background-color: rgba(74, 106, 111, 0.1);
  padding: 1px 4px;
  border-radius: 4px;
  margin-left: 8px;
  font-weight: normal;
  flex-shrink: 0;
}

.product-desc {
  margin: 0 0 10px 0;
  font-size: 0.75rem;
  color: #8B8B83;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price {
  color: #D2691E;
  font-weight: bold;
  font-size: 1rem;
  line-height: 1;
}

.action-area {
  display: flex;
  align-items: center;
  height: 24px;
}

.add-btn-wrapper {
  display: flex;
  align-items: center;
  height: 24px;
}

.add-btn {
  width: 22px;
  height: 22px;
  background-color: #4A6A6F;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.minus-btn {
  width: 22px;
  height: 22px;
  border: 1px solid #4A6A6F;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  box-sizing: border-box;
}

.qty-display {
  margin: 0 8px;
  font-size: 0.9rem;
  font-weight: bold;
  color: #333;
  min-width: 16px;
  text-align: center;
}

/* Cart Popup Styles */
.cart-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.cart-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  font-weight: bold;
}

.clear-btn {
  color: #999;
  font-size: 0.85rem;
  font-weight: normal;
}

.cart-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #fff;
  border-bottom: 1px solid #f9f9f9;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 0.95rem;
  margin-bottom: 5px;
}

.item-price {
  color: #d32f2f;
  font-weight: bold;
}

.empty-cart {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-footer {
  padding: 15px;
  border-top: 1px solid #eee;
  background: #fff;
}

.total-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-weight: bold;
}

.total-price {
  color: #d32f2f;
  font-size: 1.2rem;
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
