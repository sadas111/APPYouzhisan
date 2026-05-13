<template>
  <div class="address-list-page">
    <van-nav-bar
      title="我的地址"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    
    <div class="list-content">
      <van-address-list
        v-model="chosenAddressId"
        :list="list"
        default-tag-text="默认"
        @add="onAdd"
        @edit="onEdit"
        @select="onSelect"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();
const chosenAddressId = ref('');
const list = ref([]);

const onClickLeft = () => {
  router.back();
};

const onAdd = () => {
  router.push('/address/edit');
};

const onEdit = (item) => {
  router.push(`/address/edit?id=${item.id}`);
};

const onSelect = (item) => {
  // If coming from order confirmation, select and return
  if (route.query.from === 'order') {
    // Save selected address to localStorage or store
    localStorage.setItem('selectedAddress', JSON.stringify(item));
    router.back();
  }
};

const loadAddresses = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  try {
    const response = await request.get(`/addresses?userId=${userId}`);
    const payload = response?.data ?? response;
    if (payload) {
      list.value = payload.map(addr => ({
        id: addr.id,
        name: addr.name,
        tel: addr.tel,
        address: `${addr.province}${addr.city}${addr.county}${addr.addressDetail}`,
        isDefault: addr.isDefault,
        // Keep original data for editing
        province: addr.province,
        city: addr.city,
        county: addr.county,
        addressDetail: addr.addressDetail,
        areaCode: addr.areaCode
      }));
    }
  } catch (error) {
    console.error('Failed to load addresses:', error);
    showToast('加载地址失败');
  }
};

onMounted(() => {
  loadAddresses();
});
</script>

<style scoped>
.address-list-page {
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

/* remove decoration to show unified background */

.list-content {
  padding-top: 10px;
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

:deep(.van-nav-bar__text),
:deep(.van-nav-bar__icon) {
  color: #4A6A6F;
}

:deep(.van-button--danger) {
  background-color: #4A6A6F;
  border-color: #4A6A6F;
}

:deep(.van-tag--danger) {
  background-color: #4A6A6F;
}

:deep(.van-address-item .van-radio__icon--checked .van-icon) {
  background-color: #4A6A6F;
  border-color: #4A6A6F;
}
</style>
