<template>
  <div class="security-page">
    <van-nav-bar
      title="安全中心"
      left-arrow
      fixed
      placeholder
      class="security-nav"
      @click-left="goBack"
    />

    <div class="content">
      <!-- 油纸伞主题小横幅 -->
      <div class="umbrella-hero group--animated">
        <div class="umbrella-graphic" aria-hidden="true" />
        <div class="umbrella-text">
          <div class="umbrella-title">油纸伞 · 安全守护</div>
          <div class="umbrella-subtitle">
            像伞面一样为站点撑起一层细腻而温柔的保护。
          </div>
        </div>
      </div>

      <!-- 顶部防护开关 -->
      <van-cell-group inset class="group group--animated">
        <van-cell
          center
          class="card-row"
        >
          <template #title>
            <div class="card-title-wrap">
              <div class="card-title-main">
                <span class="card-title">防爬虫保护</span>
                <span class="card-subtitle">
                  {{ antiCrawlerEnabled
                    ? '已开启 · 实时监测异常高频访问'
                    : '已关闭 · 不进行高频检测与拦截' }}
                </span>
              </div>
              <div class="card-pill" :class="{ 'card-pill--off': !antiCrawlerEnabled }">
                <span class="card-pill-dot" />
                <span class="card-pill-text">
                  {{ antiCrawlerEnabled ? '保护中' : '未启用' }}
                </span>
              </div>
            </div>
          </template>
          <template #right-icon>
            <van-switch
              v-model="antiCrawlerEnabled"
              size="22px"
              class="anti-switch"
              active-color="#3AB08C"
              @change="handleToggle"
            />
          </template>
        </van-cell>
        <div class="card-helper">
          {{ antiCrawlerEnabled
            ? '系统将对短时间内的高频接口访问进行识别与拦截。'
            : '当前未对访问频率做保护，建议在正式环境开启防护。' }}
        </div>
      </van-cell-group>

      <!-- 当前访问状态 -->
      <van-cell-group inset class="group group--animated group--delay-1">
        <van-cell center class="card-row status-card">
          <template #title>
            <div class="status-title-row">
              <div class="status-icon-wrap">
                <van-icon :name="statusIcon" :color="statusColor" size="20" />
                <div class="status-icon-glow" />
              </div>
              <div class="status-text-block">
                <span class="status-title">{{ statusTitle }}</span>
                <span class="status-desc">{{ statusDesc }}</span>
              </div>
              <van-tag
                :type="statusTagType"
                class="status-tag"
                round
              >
                {{ statusTagText }}
              </van-tag>
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 触发记录 -->
      <van-cell-group
        v-if="securityInfo"
        inset
        class="group group--animated group--delay-2"
      >
        <van-cell
          title="最近一次触发"
          :value="formatTime(securityInfo.lastBlockedAt)"
        />
        <van-cell
          title="预计解除时间"
          :value="securityInfo.blockUntil ? formatTime(securityInfo.blockUntil) : '—'"
        />
        <van-cell
          title="触发时请求数"
          :value="securityInfo.recentRequestCount ?? '—'"
        />
      </van-cell-group>

      <!-- 操作区 -->
      <van-cell-group inset class="group group--animated group--delay-3">
        <van-cell title="操作中心" />
        <div class="actions">
          <van-button
            v-if="isBlocked"
            type="primary"
            block
            color="linear-gradient(135deg, #3AB08C, #2A8F6C)"
            class="action-primary"
            @click="handleVerify"
          >
            完成验证并解除保护
          </van-button>
          <van-button
            v-else
            block
            :loading="refreshing"
            loading-text="刷新中..."
            class="action-secondary"
            @click="handleRefresh"
          >
            刷新状态
          </van-button>

          <van-button
            v-if="securityInfo"
            block
            plain
            type="danger"
            class="action-secondary action-danger"
            @click="clearProtectionRecord"
          >
            清除保护记录
          </van-button>
        </div>
      </van-cell-group>

      <!-- 爬虫访问日志 -->
      <van-cell-group inset class="group group--animated group--delay-4">
        <van-cell title="爬虫访问日志" />
        <div class="tips">
          <div v-if="loadingLogs" class="log-loading">
            正在加载爬虫日志...
          </div>
          <div v-else-if="!crawlerLogs || crawlerLogs.length === 0" class="log-empty">
            暂无拦截到的异常访问记录。
          </div>
          <div v-else class="log-list">
            <div
              v-for="item in crawlerLogs"
              :key="item.id"
              class="log-item"
            >
              <div class="log-main-row" @click="toggleLog(item.id)">
                <div class="log-main-left">
                  <span class="log-ip">{{ item.ip || '未知 IP' }}</span>
                  <span class="log-time">{{ formatTime(item.detectedAt) }}</span>
                </div>
                <div class="log-toggle">
                  <span class="log-toggle-text">
                    {{ isLogExpanded(item.id) ? '收起详情' : '查看详情' }}
                  </span>
                  <van-icon
                    :name="isLogExpanded(item.id) ? 'arrow-up' : 'arrow-down'"
                    size="14"
                    color="#8ea4c8"
                  />
                </div>
              </div>

              <transition name="log-expand">
                <div
                  v-show="isLogExpanded(item.id)"
                  class="log-detail"
                >
                  <div class="log-path">
                    <span class="log-label">路径</span>
                    <span class="log-value">{{ item.path || '—' }}</span>
                  </div>
                  <div class="log-ua">
                    <span class="log-label">User-Agent</span>
                    <span class="log-value">{{ item.userAgent || '—' }}</span>
                  </div>
                  <div class="log-reason">
                    <span class="reason-tag">拦截原因</span>
                    <span class="log-value">{{ item.reason || '—' }}</span>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </van-cell-group>

      <!-- 说明区域 -->
      <van-cell-group inset class="group group--animated group--delay-5">
        <van-cell title="防爬虫说明" />
        <div class="tips">
          <ul class="bullet-list">
            <li>监控短时间内的高频接口访问，识别异常行为。</li>
            <li>触发规则时，自动暂时拦截请求，保护服务稳定。</li>
            <li>你可以在本页快速开启 / 关闭防爬虫保护。</li>
          </ul>
        </div>
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const SECURITY_STORAGE_KEY = 'security_protection';
const SECURITY_SETTINGS_KEY = 'security_settings';

const securityInfo = ref(null);
const isBlocked = ref(false);
const antiCrawlerEnabled = ref(true);
const crawlerLogs = ref([]);
const loadingLogs = ref(false);
const refreshing = ref(false);
const expandedLogs = ref({});

const loadSecuritySettings = () => {
  if (typeof window === 'undefined') return;
  try {
    const raw = localStorage.getItem(SECURITY_SETTINGS_KEY);
    const parsed = raw ? JSON.parse(raw) : null;
    antiCrawlerEnabled.value = parsed?.antiCrawlerEnabled ?? true;
  } catch {
    antiCrawlerEnabled.value = true;
  }
};

const loadSecurityInfo = () => {
  if (typeof window === 'undefined') return;
  try {
    const raw = localStorage.getItem(SECURITY_STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : null;
    securityInfo.value = parsed;
    const now = Date.now();
    isBlocked.value = !!(parsed && parsed.blockUntil && now < parsed.blockUntil);
  } catch {
    securityInfo.value = null;
    isBlocked.value = false;
  }
};

const handleRefresh = async () => {
  if (refreshing.value) return;
  refreshing.value = true;
  try {
    loadSecurityInfo();
    await loadCrawlerLogs();
  } finally {
    refreshing.value = false;
  }
};

const loadCrawlerLogs = async () => {
  loadingLogs.value = true;
  try {
    const res = await request.get('/security/crawler-logs');
    const data = Array.isArray(res) ? res : res?.data;
    crawlerLogs.value = Array.isArray(data) ? data : [];
  } catch {
    showToast('获取爬虫日志失败');
    crawlerLogs.value = [];
  } finally {
    loadingLogs.value = false;
  }
};

const toggleLog = (id) => {
  const current = expandedLogs.value[id];
  expandedLogs.value = {
    ...expandedLogs.value,
    [id]: !current
  };
};

const isLogExpanded = (id) => !!expandedLogs.value[id];

onMounted(() => {
  loadSecuritySettings();
  loadSecurityInfo();
  loadCrawlerLogs();
});

const goBack = () => {
  router.back();
};

const handleToggle = (val) => {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(
      SECURITY_SETTINGS_KEY,
      JSON.stringify({ antiCrawlerEnabled: !!val })
    );
  } catch {
    // ignore
  }

  if (!val) {
    // 关闭保护：同时清空已触发的锁定状态，避免“关了还被拦截”
    try {
      localStorage.removeItem(SECURITY_STORAGE_KEY);
    } catch {
      // ignore
    }
    showToast('已关闭防爬虫保护');
  } else {
    showToast('已开启防爬虫保护');
  }

  loadSecurityInfo();
};

const handleVerify = () => {
  if (typeof window === 'undefined') return;
  // 简单的人机验证占位逻辑：点击按钮即视为通过
  try {
    localStorage.removeItem(SECURITY_STORAGE_KEY);
  } catch {
    // ignore
  }
  showToast({
    type: 'success',
    message: '验证通过，已解除安全保护'
  });
  loadSecurityInfo();
};

const clearProtectionRecord = () => {
  if (typeof window === 'undefined') return;
  try {
    localStorage.removeItem(SECURITY_STORAGE_KEY);
  } catch {
    // ignore
  }
  showToast('已清除保护记录');
  loadSecurityInfo();
};

const formatTime = (ts) => {
  if (!ts) return '—';
  try {
    const date = new Date(ts);
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    const ss = String(date.getSeconds()).padStart(2, '0');
    return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
  } catch {
    return '—';
  }
};

const statusDesc = computed(() => {
  if (!antiCrawlerEnabled.value) {
    return '当前已关闭防爬虫保护：不会做高频访问检测与拦截。建议在公共环境或上线环境开启。';
  }
  if (isBlocked.value) {
    return '由于短时间内访问过于频繁，系统已暂时拦截请求。完成验证后即可恢复正常使用。';
  }
  return '暂无检测到异常访问行为，正常使用即可。如遇频繁拦截，可联系管理员调整策略。';
});

const statusTitle = computed(() => {
  if (!antiCrawlerEnabled.value) return '防爬虫保护已关闭';
  return isBlocked.value ? '已启动安全保护' : '当前访问状态正常';
});

const statusTagText = computed(() => {
  if (!antiCrawlerEnabled.value) return '已关闭';
  return isBlocked.value ? '拦截中' : '正常';
});

const statusTagType = computed(() => {
  if (!antiCrawlerEnabled.value) return 'primary';
  return isBlocked.value ? 'danger' : 'success';
});

const statusIcon = computed(() => {
  if (!antiCrawlerEnabled.value) return 'info-o';
  return isBlocked.value ? 'warning-o' : 'passed';
});

const statusColor = computed(() => {
  if (!antiCrawlerEnabled.value) return '#1989FA';
  return isBlocked.value ? '#EE0A24' : '#07c160';
});
</script>

<style scoped>
.security-page {
  min-height: 100vh;
  background:
    radial-gradient(1200px 600px at 20% 0%, rgba(58, 176, 140, 0.12), transparent 60%),
    radial-gradient(1000px 500px at 90% 20%, rgba(74, 106, 111, 0.1), transparent 55%),
    linear-gradient(180deg, rgba(185, 222, 201, 0.2), rgba(255, 255, 255, 0));
  color: #333;
}

.security-nav :deep(.van-nav-bar) {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(14px);
}

.security-nav :deep(.van-icon) {
  color: #4A6A6F;
}

.security-nav :deep(.van-nav-bar__title) {
  color: #4A6A6F;
  font-weight: 700;
  letter-spacing: 1px;
}

.content {
  padding: 14px 12px 22px;
  animation: page-fade-up 0.5s ease-out;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Segoe UI', system-ui,
    -system-ui, sans-serif;
}

.umbrella-hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px 12px;
  margin-bottom: 10px;
  border-radius: 16px;
  background:
    radial-gradient(circle at 0% 0%, rgba(245, 222, 179, 0.45), transparent 60%),
    radial-gradient(circle at 90% 10%, rgba(58, 176, 140, 0.18), transparent 65%),
    rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.umbrella-graphic {
  position: relative;
  flex-shrink: 0;
  width: 54px;
  height: 54px;
  border-radius: 999px;
  background:
    radial-gradient(circle at 30% 25%, #fff7e6 0, #f4d9a4 30%, #e4b676 65%, #d08f4a 100%);
  border: 1px solid rgba(184, 135, 72, 0.55);
  box-shadow:
    0 6px 16px rgba(160, 110, 50, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.6) inset;
}

.umbrella-graphic::before {
  content: '';
  position: absolute;
  inset: 8px;
  border-radius: 999px;
  background:
    conic-gradient(
      from -90deg,
      rgba(214, 143, 70, 0.7),
      rgba(249, 224, 188, 0.7),
      rgba(214, 143, 70, 0.7),
      rgba(249, 224, 188, 0.7),
      rgba(214, 143, 70, 0.7)
    );
  mask:
    radial-gradient(circle at 50% 50%, transparent 0 18px, #000 19px 100%);
}

.umbrella-graphic::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 10px;
  height: 18px;
  transform: translate(-50%, 2px);
  border-radius: 999px;
  background: linear-gradient(180deg, #b27a38, #805021);
  box-shadow: 0 4px 6px rgba(128, 80, 33, 0.35);
}

.umbrella-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.umbrella-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #4A3B30;
}

.umbrella-subtitle {
  font-size: 0.78rem;
  color: #7a6a5a;
  line-height: 1.4;
}

.group {
  margin-bottom: 12px;
}

.group--animated {
  animation: card-fade-up 0.5s ease-out both;
}

.group--delay-1 {
  animation-delay: 0.05s;
}

.group--delay-2 {
  animation-delay: 0.1s;
}

.group--delay-3 {
  animation-delay: 0.15s;
}

.group--delay-4 {
  animation-delay: 0.2s;
}

.group--delay-5 {
  animation-delay: 0.25s;
}

.group :deep(.van-cell-group--inset) {
  background:
    radial-gradient(600px 320px at 0% 0%, rgba(58, 176, 140, 0.08), transparent 65%),
    radial-gradient(520px 280px at 100% 0%, rgba(74, 106, 111, 0.06), transparent 60%),
    rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  box-shadow:
    0 10px 26px rgba(0, 0, 0, 0.06),
    0 0 0 1px rgba(74, 106, 111, 0.06);
  overflow: hidden;
  transition:
    transform 0.2s ease-out,
    box-shadow 0.2s ease-out,
    border-color 0.2s ease-out;
}

.group :deep(.van-cell) {
  background: transparent;
}

.group :deep(.van-cell__title) {
  font-size: 0.9rem;
  color: #333;
}

.group :deep(.van-cell__value) {
  font-size: 0.84rem;
  color: #4A6A6F;
}

@media (hover: hover) {
  .group:hover :deep(.van-cell-group--inset) {
    transform: translateY(-2px);
    box-shadow:
      0 14px 32px rgba(0, 0, 0, 0.09),
      0 0 0 1px rgba(58, 176, 140, 0.18);
  }
}

.card-row {
  padding: 12px 14px;
}

.card-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.card-subtitle {
  font-size: 0.78rem;
  color: #666;
}

.card-pill {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(58, 176, 140, 0.1);
  border: 1px solid rgba(58, 176, 140, 0.45);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.card-pill--off {
  background: rgba(153, 153, 153, 0.08);
  border-color: rgba(153, 153, 153, 0.5);
}

.card-pill-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #3AB08C;
}

.card-pill--off .card-pill-dot {
  background: #f97373;
}

.card-pill-text {
  font-size: 0.76rem;
  color: #333;
}

.card-helper {
  padding: 0 14px 12px;
  font-size: 0.78rem;
  color: #666;
}

.anti-switch :deep(.van-switch__node) {
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

.anti-switch :deep(.van-switch__node::before) {
  background: radial-gradient(circle at 30% 30%, #ffffff 0, #d9f4eb 45%, #3AB08C 100%);
}

.status-card {
  padding: 12px 14px 10px;
}

.status-title-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.status-icon-wrap {
  position: relative;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 30% 0, rgba(58, 176, 140, 0.3), transparent 60%);
}

.status-icon-glow {
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  background: conic-gradient(
    from 160deg,
    rgba(58, 176, 140, 0.55),
    rgba(217, 244, 235, 0),
    rgba(74, 106, 111, 0.5),
    rgba(217, 244, 235, 0)
  );
  opacity: 0.7;
  filter: blur(3px);
  z-index: -1;
}

.status-text-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.status-title {
  color: #333;
  font-size: 0.98rem;
  font-weight: 600;
}

.status-tag {
  margin-left: auto;
  font-size: 0.72rem;
  padding: 2px 10px;
}

.status-desc {
  font-size: 0.78rem;
  color: #666;
  line-height: 1.55;
}

.actions {
  padding: 10px 12px 13px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-primary,
.action-secondary {
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.02em;
  transition:
    transform 0.18s ease-out,
    box-shadow 0.18s ease-out,
    filter 0.18s ease-out;
}

.action-secondary {
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

.action-danger {
  box-shadow: 0 4px 10px rgba(248, 113, 113, 0.3);
}

@media (hover: hover) {
  .action-primary:hover,
  .action-secondary:hover {
    transform: translateY(-1px);
    filter: brightness(1.05);
  }

  .action-primary:active,
  .action-secondary:active {
    transform: translateY(0);
    filter: brightness(0.97);
  }
}

.tips {
  padding: 6px 12px 14px;
  color: #666;
  font-size: 0.8rem;
}

.log-loading,
.log-empty {
  font-size: 0.8rem;
  color: #666;
  padding: 6px 0 4px;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
}

.log-item {
  padding: 8px 10px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(74, 106, 111, 0.08);
  transition:
    transform 0.16s ease-out,
    box-shadow 0.16s ease-out,
    border-color 0.16s ease-out;
}

@media (hover: hover) {
  .log-item:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-color: rgba(58, 176, 140, 0.35);
  }
}

.log-main-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 2px;
  cursor: pointer;
  font-size: 0.8rem;
}

.log-main-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.log-ip {
  font-weight: 600;
  color: #333;
  font-size: 0.86rem;
}

.log-time {
  font-size: 0.74rem;
  color: #999;
}

.log-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(74, 106, 111, 0.06);
  border: 1px solid rgba(74, 106, 111, 0.2);
}

.log-toggle-text {
  font-size: 0.74rem;
  color: #666;
}

.log-detail {
  margin-top: 6px;
  border-top: 1px dashed rgba(148, 163, 184, 0.6);
  padding-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.log-path,
.log-ua,
.log-reason {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.log-label {
  min-width: 60px;
  font-size: 0.75rem;
  color: #999;
}

.log-value {
  font-size: 0.78rem;
  color: #555;
  line-height: 1.45;
  word-break: break-all;
}

.reason-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  height: 20px;
  font-size: 0.72rem;
  border-radius: 999px;
  background: linear-gradient(135deg, #f97373, #ef4444);
  color: #fff;
  white-space: nowrap;
}

.bullet-list {
  padding: 4px 10px 2px 20px;
  margin: 0;
  font-size: 0.8rem;
  color: #666;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
}

@keyframes page-fade-up {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes card-fade-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.log-expand-enter-active,
.log-expand-leave-active {
  transition:
    opacity 0.18s ease-out,
    transform 0.18s ease-out,
    max-height 0.18s ease-out;
}

.log-expand-enter-from,
.log-expand-leave-to {
  opacity: 0;
  transform: translateY(-2px);
  max-height: 0;
}

.log-expand-enter-to,
.log-expand-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 800px;
}

@media (max-width: 480px) {
  .content {
    padding-inline: 10px;
  }

  .umbrella-hero {
    padding-inline: 10px;
  }

  .card-row,
  .status-card {
    padding-inline: 12px;
  }

  .card-title {
    font-size: 0.96rem;
  }

  .card-subtitle,
  .status-desc {
    font-size: 0.76rem;
  }
}
</style>

