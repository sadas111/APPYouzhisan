import axios from 'axios';
import router from '@/router';
import { showToast } from 'vant';

// 前端反爬虫与安全中心支持配置
const REQUEST_WINDOW_MS = 60 * 1000; // 统计窗口：60 秒
const MAX_REQUESTS_PER_WINDOW = 80;  // 单窗口最大请求数（根据项目实际流量可调整）
const BLOCK_DURATION_MS = 10 * 60 * 1000; // 触发保护后锁定 10 分钟

// 仅在当前标签页内生效的请求时间记录
const requestTimeline = [];

const SECURITY_STORAGE_KEY = 'security_protection';
const SECURITY_SETTINGS_KEY = 'security_settings';

function loadSecuritySettings() {
  if (typeof window === 'undefined') return { antiCrawlerEnabled: true };
  try {
    const raw = localStorage.getItem(SECURITY_SETTINGS_KEY);
    const parsed = raw ? JSON.parse(raw) : null;
    // 默认开启，保证原有行为不变；用户可在安全中心手动关闭
    return {
      antiCrawlerEnabled: parsed?.antiCrawlerEnabled ?? true
    };
  } catch {
    return { antiCrawlerEnabled: true };
  }
}

function loadSecurityInfo() {
  if (typeof window === 'undefined') return null;
  try {
    const raw = localStorage.getItem(SECURITY_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function saveSecurityInfo(info) {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(SECURITY_STORAGE_KEY, JSON.stringify(info));
  } catch {
    // ignore storage error
  }
}

function isCurrentlyBlocked(info) {
  if (!info || !info.blockUntil) return false;
  return Date.now() < info.blockUntil;
}

function recordRequestAndDetectAbuse() {
  const now = Date.now();
  const windowStart = now - REQUEST_WINDOW_MS;

  // 追加当前请求时间
  requestTimeline.push(now);

  // 清理窗口外的旧记录
  while (requestTimeline.length && requestTimeline[0] < windowStart) {
    requestTimeline.shift();
  }

  // 超过阈值，认为是可疑的高频访问
  if (requestTimeline.length > MAX_REQUESTS_PER_WINDOW) {
    const info = {
      blockUntil: now + BLOCK_DURATION_MS,
      lastBlockedAt: now,
      blockedReason: 'high_request_rate',
      recentRequestCount: requestTimeline.length
    };
    saveSecurityInfo(info);
    return info;
  }

  return null;
}

// Create an axios instance
const service = axios.create({
  baseURL: 'http://localhost:8080/api', // Backend API URL
  timeout: 5000 // Request timeout
});

// Request interceptor
service.interceptors.request.use(
  config => {
    if (typeof window !== 'undefined') {
      const settings = loadSecuritySettings();

      // 用户关闭防爬虫保护时：不做频率检测与拦截，但仍保留正常鉴权头逻辑
      if (settings?.antiCrawlerEnabled === false) {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers = config.headers || {};
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      }

      // 1. 先检查当前是否已经被安全中心锁定
      const securityInfo = loadSecurityInfo();
      if (isCurrentlyBlocked(securityInfo)) {
        showToast('检测到异常访问频率，已开启安全保护，请前往安全中心解除');
        // 引导用户前往安全中心
        try {
          if (router?.currentRoute?.value?.path !== '/security') {
            router.push('/security');
          }
        } catch {
          // ignore router error
        }
        return Promise.reject(new Error('Blocked by client security protection'));
      }

      // 2. 记录请求行为，用于简单的反爬虫检测
      const newInfo = recordRequestAndDetectAbuse();
      if (newInfo && isCurrentlyBlocked(newInfo)) {
        showToast('访问过于频繁，已触发安全保护，请前往安全中心验证');
        try {
          if (router?.currentRoute?.value?.path !== '/security') {
            router.push('/security');
          }
        } catch {
          // ignore
        }
        return Promise.reject(new Error('Blocked by client security protection (rate limit)'));
      }

      // 3. 正常附加认证信息
      const token = localStorage.getItem('token');
      if (token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  error => {
    console.log(error); // for debug
    return Promise.reject(error);
  }
);

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data;
    // You can add custom response handling here
    return res;
  },
  error => {
    const status = error?.response?.status;
    if (status === 401 && typeof window !== 'undefined') {
      try {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('userId');
      } catch {
        // ignore
      }

      const currentPath = router?.currentRoute?.value?.path;
      if (currentPath !== '/login') {
        try {
          router.push('/login');
        } catch {
          // ignore
        }
      }
    }

    console.log('err' + error); // for debug
    return Promise.reject(error);
  }
);

export default service;
