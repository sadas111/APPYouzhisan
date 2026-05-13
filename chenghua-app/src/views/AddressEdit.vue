<template>
  <div class="address-edit-page">
    <van-nav-bar
      :title="isEdit ? '编辑地址' : '新增地址'"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    
    <van-address-edit
      :area-list="areaList"
      :address-info="addressInfo"
      show-delete
      show-set-default
      show-search-result
      :search-result="searchResult"
      :area-columns-placeholder="['请选择', '请选择', '请选择']"
      @save="onSave"
      @delete="onDelete"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showDialog } from 'vant';
import { areaList } from '@vant/area-data';
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();

const addressId = route.query.id;
const isEdit = computed(() => !!addressId);
const addressInfo = ref({});
const searchResult = ref([]);

const onClickLeft = () => {
  router.back();
};

const onSave = async (content) => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    showToast('请先登录');
    return;
  }

  const payload = {
    userId: parseInt(userId),
    name: content.name,
    tel: content.tel,
    province: content.province,
    city: content.city,
    county: content.county,
    addressDetail: content.addressDetail,
    areaCode: content.areaCode,
    isDefault: content.isDefault,
  };

  if (isEdit.value) {
    payload.id = parseInt(addressId);
  }

  try {
    await request.post('/addresses', payload);
    showToast('保存成功');
    setTimeout(() => {
      router.back();
    }, 500);
  } catch (error) {
    console.error('Save failed:', error);
    showToast('保存失败');
  }
};

const onDelete = () => {
  showDialog({
    title: '确认删除',
    message: '确认要删除这个地址吗？',
    showCancelButton: true,
  }).then(async (action) => {
    if (action === 'confirm') {
      const userId = localStorage.getItem('userId');
      try {
        await request.delete(`/addresses/${addressId}?userId=${userId}`);
        showToast('删除成功');
        setTimeout(() => {
          router.back();
        }, 500);
      } catch (error) {
        console.error('Delete failed:', error);
        showToast('删除失败');
      }
    }
  });
};

const loadAddress = async () => {
  if (!isEdit.value) return;
  
  const userId = localStorage.getItem('userId');
  if (!userId) return;

  try {
    // Since we don't have a direct get-by-id endpoint yet, fetch all and filter
    const response = await request.get(`/addresses?userId=${userId}`);
    const payload = response?.data ?? response;
    if (payload) {
      const found = payload.find(addr => addr.id == addressId);
      if (found) {
        addressInfo.value = {
          id: found.id,
          name: found.name,
          tel: found.tel,
          province: found.province,
          city: found.city,
          county: found.county,
          addressDetail: found.addressDetail,
          areaCode: found.areaCode,
          isDefault: found.isDefault
        };
      }
    }
  } catch (error) {
    console.error('Load address failed:', error);
    showToast('加载地址失败');
  }
};

onMounted(() => {
  loadAddress();
});
</script>

<style scoped>
.address-edit-page {
  min-height: 100vh;
  background-color: #F5F5DC;
  position: relative;
  overflow: hidden;
}

/* Background Decoration */
.address-edit-page::before {
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

.address-edit-page::after {
  content: "地址";
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
</style>
