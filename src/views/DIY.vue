<template>
  <div class="page-container">
    <!-- 顶部导航栏 -->
    <header class="header-bar">
      <div class="header-left">
        <h2 class="header-title">创意工坊</h2>
        <span class="header-subtitle">油纸伞 DIY 定制</span>
      </div>
      <van-button
        class="save-btn"
        size="small"
        type="primary"
        color="#4A6A6F"
        icon="success"
        @click="saveWork"
      >
        保存作品
      </van-button>
    </header>
    
    <!-- 装饰性背景 -->
    <div class="decorative-bg">
      <div class="cloud-decoration cloud-1"></div>
      <div class="cloud-decoration cloud-2"></div>
      <div class="ink-blob blob-1"></div>
      <div class="ink-blob blob-2"></div>
    </div>

    <main class="main-layout">
      <!-- 左侧：3D 视图区 -->
      <section class="viewer-section">
        <div class="viewer-panel" ref="viewerPanelRef">
          <div class="viewer-bg"></div>
          <PaperUmbrella3D
            ref="umbrella3dRef"
            class="viewer"
            :canopy-color="canopyColor"
            :ribs-color="ribsColor"
            :handle-color="handleColor"
            :pattern-id="patternId"
            :pattern-scale="patternScale"
            :custom-pattern-url="customPatternUrl"
            h5-mode
            :exploded="exploded"
            @assembled="onAssembled"
          />
        </div>

        <!-- 工艺卡片横向滚动条 -->
        <div class="craft-strip">
          <div class="craft-header">
            <span class="craft-title">工艺细节</span>
            <span class="craft-count">{{ craftDetails.length }} 项</span>
          </div>
          <div class="craft-list">
            <div
              v-for="item in craftDetails"
              :key="item.id"
              class="craft-card"
              @click="openCraftDetail(item)"
            >
              <div class="craft-card-icon">
                <svg v-if="item.part === 'canopy'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2v8"/><path d="m4.93 10.93 1.41 1.41"/><path d="M2 18h2"/><path d="M20 18h2"/><path d="m19.07 10.93-1.41 1.41"/><path d="M22 22H2"/><circle cx="12" cy="18" r="4"/><path d="M12 2a9.86 9.86 0 0 1 9.52 7H2.48A9.86 9.86 0 0 1 12 2Z"/>
                </svg>
                <svg v-else-if="item.part === 'ribs'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="3"/><path d="M12 2v4"/><path d="M12 18v4"/><path d="m4.93 4.93 2.83 2.83"/><path d="m16.24 16.24 2.83 2.83"/><path d="M2 12h4"/><path d="M18 12h4"/><path d="m4.93 19.07 2.83-2.83"/><path d="m16.24 7.76 2.83-2.83"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22v-5"/><path d="M9 12V2"/><path d="M15 12V2"/><path d="M9 12a3 3 0 0 0 3 3 3 3 0 0 0 3-3"/><path d="M6 19a6 6 0 0 1 12 0"/>
                </svg>
              </div>
              <div class="craft-card-content">
                <div class="craft-card-title">{{ item.title }}</div>
                <div class="craft-card-desc">{{ item.short }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 右侧：工具面板 -->
      <aside class="tools-panel">
        <!-- 拆分拼接控制 -->
        <div class="panel-card primary-card">
          <div class="card-header">
            <span class="card-icon card-icon--photo">
              <img src="/images/koutu.png" alt="" />
            </span>
            <span class="card-title">拖动拼接</span>
          </div>
          <div class="toggle-group">
			  
			<div class="toggle-item">
				  <span class="toggle-label">拆分视图</span>
				  <van-switch v-model="exploded" size="22" active-color="#4A6A6F" />
				</div>
			
            <div class="toggle-item">
              <span class="toggle-label">语音讲解</span>
              <van-switch v-model="voiceEnabled" size="22" active-color="#4A6A6F" />
              <button
                v-if="voiceEnabled"
                type="button"
                class="voice-control"
                @click="toggleVoicePause"
              >
                {{ isVoicePaused ? '继续' : '暂停' }}
              </button>
            </div>
          </div>
          <div class="tip-banner" :class="{ active: exploded }">
            <span class="tip-icon">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/>
              </svg>
            </span>
            <span>{{ exploded ? '拖动伞面 / 伞骨 / 伞柄靠近原位自动吸附' : '开启拆分视图后可拖动部件进行拼接' }}</span>
          </div>
        </div>

        <!-- 色彩选择 -->
        <div class="panel-card">
          <div class="card-header">
            <span class="card-icon card-icon--box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="13.5" cy="6.5" r=".5" fill="currentColor"/><circle cx="17.5" cy="10.5" r=".5" fill="currentColor"/><circle cx="8.5" cy="7.5" r=".5" fill="currentColor"/><circle cx="6.5" cy="12.5" r=".5" fill="currentColor"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.025a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/>
              </svg>
            </span>
            <span class="card-title">色彩定制</span>
          </div>
          
          <div class="color-section">
            <span class="color-label">伞面颜色</span>
            <div class="color-grid">
              <button
                v-for="c in palette"
                :key="'canopy-' + c.value"
                class="color-swatch"
                :class="{ active: canopyColor === c.value }"    
                :style="{ background: c.value }"
                type="button"
                @click="setColor('canopy', c.value)"
              >
                <span class="swatch-name">{{ c.label }}</span>
              </button>
            </div>
          </div>

          <div class="color-section">
            <span class="color-label">伞骨颜色</span>
            <div class="color-grid compact">
              <button
                v-for="c in paletteRibs"
                :key="'ribs-' + c.value"
                class="color-swatch"
                :class="{ active: ribsColor === c.value }"
                :style="{ background: c.value }"
                type="button"
                @click="setColor('ribs', c.value)"
              >
                <span class="swatch-name">{{ c.label }}</span>
              </button>
            </div>
          </div>

          <div class="color-section">
            <span class="color-label">伞柄颜色</span>
            <div class="color-grid compact">
              <button
                v-for="c in paletteHandle"
                :key="'handle-' + c.value"
                class="color-swatch"
                :class="{ active: handleColor === c.value }"
                :style="{ background: c.value }"
                type="button"
                @click="setColor('handle', c.value)"
              >
                <span class="swatch-name">{{ c.label }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 图案选择 -->
        <div class="panel-card">
          <div class="card-header">
            <span class="card-icon card-icon--box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 7.5a4.5 4.5 0 1 1 4.5 4.5M12 7.5A4.5 4.5 0 1 0 7.5 12M12 7.5V9m-4.5 3H12m0 0v3m-1.5 6a4.5 4.5 0 1 0 9 0"/><path d="M12 15a3 3 0 1 0 6 0"/><path d="M12 15a3 3 0 1 1-6 0"/>
              </svg>
            </span>
            <span class="card-title">图案纹样</span>
          </div>
          <div class="pattern-grid">
            <button
              v-for="p in patterns"
              :key="p.id"
              class="pattern-card"
              :class="{ active: patternId === p.id }"
              type="button"
              @click="onSelectPattern(p.id)"
            >
              <div class="pattern-thumb" :style="patternThumbStyle(p.id)"></div>
              <div class="pattern-info">
                <span class="pattern-name">{{ p.label }}</span>
                <span class="pattern-desc" v-if="p.id !== 'none'">{{ getPatternDescription(p.id) }}</span>
              </div>
            </button>
          </div>

          <div class="upload-row">
            <span class="upload-label">自定义图案</span>
            <div class="upload-actions">
              <van-uploader class="pattern-uploader" :after-read="onUploadPattern" accept="image/*" :max-count="1" />
              <van-button v-if="customPatternUrl" class="upload-clear-btn" size="mini" plain type="primary" color="#4A6A6F" @click="clearCustomPattern">
                清除
              </van-button>
            </div>
          </div>

          <div class="slider-section">
            <div class="slider-header">
              <span>图案大小</span>
              <span class="slider-value">{{ patternScale.toFixed(1) }}x</span>
            </div>
            <van-slider
              v-model="patternScale"
              :min="0.6"
              :max="2.2"
              :step="0.1"
              bar-height="6px"
              active-color="#4A6A6F"
              @change="onPatternScaleChange"
            />
          </div>

          <div class="oil-section">
            <span class="oil-label">桐油层数</span>
            <div class="oil-pills">
              <button
                v-for="n in [1, 2, 3]"
                :key="n"
                type="button"
                class="oil-btn"
                :class="{ active: oilLayers === n }"
                @click="oilLayers = n"
              >
                {{ n }} 层
              </button>
            </div>
          </div>
        </div>
      </aside>
    </main>

    <!-- 悬浮按钮：纹样图鉴 -->
    <van-button
      class="floating-btn"
      round
      size="small"
      type="primary"
      color="#4A6A6F"
      icon="cluster-o"
      @click="showPatternDrawer = true"
    >
      纹样图鉴
    </van-button>

    <!-- 纹样图鉴抽屉 -->
    <van-popup
      v-model:show="showPatternDrawer"
      position="right"
      round
      class="pattern-drawer"
      :style="{ width: '85%', maxWidth: '380px' }"
    >
      <div class="drawer-header">
        <div class="drawer-header-bg">
          <div class="drawer-pattern-bg"></div>
          <div class="drawer-gradient-overlay"></div>
        </div>
        <div class="drawer-header-content">
          <div class="drawer-icon-wrap">
            <svg class="drawer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2v8M4.93 10.93l1.41 1.41M2 18h2M20 18h2M19.07 10.93l-1.41 1.41M22 22H2"/>
              <circle cx="12" cy="18" r="4"/>
              <path d="M12 2a9.86 9.86 0 0 1 9.52 7H2.48A9.86 9.86 0 0 1 12 2Z"/>
            </svg>
          </div>
          <div class="drawer-title">油纸伞纹样图鉴</div>
          <div class="drawer-subtitle">
            <span class="subtitle-dot"></span>
            点击纹样卡片一键应用到当前伞面
          </div>
        </div>
      </div>
      <div class="catalog-list">
        <div
          v-for="(item, idx) in patternCatalog"
          :key="item.id"
          class="catalog-item"
          :style="{ animationDelay: `${idx * 0.06}s` }"
          @click="applyCatalogPattern(item)"
        >
          <div class="catalog-thumb-wrap">
            <div class="catalog-thumb" :style="patternThumbStyle(item.patternId)"></div>
            <div class="catalog-thumb-overlay"></div>
          </div>
          <div class="catalog-content">
            <div class="catalog-name-row">
              <span class="catalog-name">{{ item.name }}</span>
              <span :class="['heritage-badge', heritageBadgeClass(item.level)]">{{ item.level }}</span>
            </div>
            <div class="catalog-meta">{{ item.origin }}</div>
            <div class="catalog-meaning">{{ item.meaning }}</div>
            <div class="catalog-action">
              <span class="apply-hint">一键应用</span>
              <svg class="apply-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
      <div class="drawer-footer">
        <div class="footer-toggle-wrap">
          <van-switch v-model="enableTips" size="22" active-color="#4A6A6F" />
          <span class="footer-label">开启非遗知识小提示</span>
        </div>
        <div class="footer-hint">选择纹样时将展示相关文化背景</div>
      </div>
    </van-popup>

    <!-- 知识弹窗 -->
    <van-popup
      v-model:show="showKnowledgePopup"
      round
      position="bottom"
      class="knowledge-popup"
    >
      <div class="knowledge-content">
        <div class="knowledge-tag">非遗小知识</div>
        <p class="knowledge-text">{{ currentTip?.text }}</p>
        <div class="knowledge-actions">
          <van-checkbox v-model="dontShowTipsAgain" shape="square" icon-size="14px">
            不再提示
          </van-checkbox>
          <van-button
            size="small"
            round
            type="primary"
            color="#4A6A6F"
            @click="closeKnowledgePopup"
          >
            我知道了
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 工艺详情弹窗 -->
    <van-popup
      v-model:show="showCraftPopup"
      round
      position="bottom"
      class="knowledge-popup"
    >
      <div class="knowledge-content">
        <div class="knowledge-tag">工艺细节拆解</div>
        <p class="knowledge-title">{{ activeCraft?.title }}</p>
        <p class="knowledge-text">{{ activeCraft?.description }}</p>
        <div class="knowledge-actions">
          <span class="knowledge-hint">结合 3D 视图观察更易理解</span>
          <van-button
            size="small"
            round
            type="primary"
            color="#4A6A6F"
            @click="showCraftPopup = false"
          >
            关闭
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue';
import { showToast, showImagePreview } from 'vant';
import request from '@/utils/request';

import PaperUmbrella3D from '../components/PaperUmbrella3D.vue';
import { buildPatternThumbDataUrl } from '../components/umbrellaTextures.js';
import { patternCatalog } from '../content/patternCatalog.js';
import { getRandomTip } from '../content/knowledgeTips.js';
import { craftDetails as craftDetailsSource } from '../content/craftDetails.js';
const umbrella3dRef = ref(null);
const viewerPanelRef = ref(null);

const exploded = ref(false);

const canopyColor = ref('#1F3B4D');
const ribsColor = ref('#8C6B4A');
const handleColor = ref('#5A3E2B');

const patternId = ref('none');
const patternScale = ref(1);
const customPatternUrl = ref('');

// 纹样图鉴 & 知识提示
const showPatternDrawer = ref(false);
const showKnowledgePopup = ref(false);
const currentTip = ref(null);
const enableTips = ref(true);
const dontShowTipsAgain = ref(false);

const TIP_PREF_KEY = 'chenghua_diy_tip_pref_v1';

let shownTipIdSet = new Set();

// 工艺细节
const craftDetails = craftDetailsSource;
const showCraftPopup = ref(false);
const activeCraft = ref(null);

// 语音讲解
const voiceEnabled = ref(false);
const audioCache = {};
let currentUtterance = null;
const isVoicePaused = ref(false);
let currentGuideAudio = null;
let preferredZhVoice = null;
let ttsAbortController = null;
const ttsAudioUrlCache = new Map();

const GUIDE_AUDIO_SRC_MAP = {
  select_pattern: '/audio/pattern.mp3',
  scale_pattern: '/audio/scale.mp3',
  upload_pattern: '/audio/upload.mp3',
  save_work: '/audio/save.mp3',
  exploded_on: '/audio/explode.mp3',
};

const readJsonFromStorage = (key) => {
  if (typeof window === 'undefined') return null;
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
};

const writeJsonToStorage = (key, value) => {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // ignore quota errors
  }
};

// 精选图案：只保留最有特色、最精美的4个
const patterns = [
  { id: 'none', label: '留白' },
  { id: 'cloud', label: '祥云' },
  { id: 'plum', label: '梅花' },
  { id: 'lotus', label: '缠枝莲' },
  { id: 'landscape', label: '山水' },
];

// 精选颜色：伞面保留6个经典色
const palette = [
  { label: '黛青', value: '#1F3B4D' },
  { label: '朱砂', value: '#C11F2A' },
  { label: '石绿', value: '#3A7D5C' },
  { label: '粉红', value: '#E8A5C2' },
  { label: '米白', value: '#F7F1E3' },
  { label: '墨', value: '#1B1B1B' },
];

// 伞骨颜色：保留3个
const paletteRibs = [
  { label: '原木', value: '#8C6B4A' },
  { label: '深木', value: '#5A3E2B' },
  { label: '墨', value: '#1B1B1B' },
];

// 伞柄颜色：保留3个
const paletteHandle = [
  { label: '深木', value: '#5A3E2B' },
  { label: '原木', value: '#8C6B4A' },
  { label: '墨', value: '#1B1B1B' },
];

// 桐油层数：默认两层，可选 1 / 2 / 3
const oilLayers = ref(2);

const speakText = (text) => {
  if (!voiceEnabled.value) return;
  if (typeof window === 'undefined') return;
  if (isVoicePaused.value) return;

  const content = (text ?? '').trim();
  if (!content) return;

  const cachedUrl = ttsAudioUrlCache.get(content);
  if (cachedUrl) {
    try {
      const audio = new Audio(cachedUrl);
      if (currentGuideAudio && currentGuideAudio !== audio) {
        try {
          currentGuideAudio.pause();
          currentGuideAudio.currentTime = 0;
        } catch {
          // ignore
        }
      }
      currentGuideAudio = audio;
      audio.currentTime = 0;
      audio.play().catch(() => {
        // ignore autoplay errors
      });
    } catch {
      // ignore
    }
    return;
  }

  if (ttsAbortController) {
    try {
      ttsAbortController.abort();
    } catch {
      // ignore
    }
  }
  ttsAbortController = new AbortController();

  fetch('http://localhost:8080/api/tts/xfyun', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text: content }),
    signal: ttsAbortController.signal,
  })
    .then((res) => {
      if (!res.ok) throw new Error('tts_failed');
      return res.blob();
    })
    .then((blob) => {
      if (ttsAbortController?.signal?.aborted) return;
      const url = URL.createObjectURL(blob);
      ttsAudioUrlCache.set(content, url);
      const audio = new Audio(url);
      if (currentGuideAudio && currentGuideAudio !== audio) {
        try {
          currentGuideAudio.pause();
          currentGuideAudio.currentTime = 0;
        } catch {
          // ignore
        }
      }
      currentGuideAudio = audio;
      if (isVoicePaused.value) {
        try {
          audio.pause();
        } catch {
          // ignore
        }
        return;
      }
      audio.currentTime = 0;
      audio.play().catch(() => {
        // ignore autoplay errors
      });
    })
    .catch(() => {
      // ignore
    });
};

const openCraftDetail = (item) => {
  activeCraft.value = item;
  showCraftPopup.value = true;
  if (item.part && umbrella3dRef.value?.setSelected) {
    umbrella3dRef.value.setSelected(item.part);
  }
  speakText(`${item.title}。${item.description}`);
};

const onAssembled = () => {
  exploded.value = false;
  showToast({ type: 'success', message: '拼接完成，一把完整的纸伞诞生啦' });
  maybeShowTip('assemble_success');
};

const setColor = (part, color) => {
  if (part === 'canopy') canopyColor.value = color;
  if (part === 'ribs') ribsColor.value = color;
  if (part === 'handle') handleColor.value = color;
};

const thumbCache = new Map();

watch(
  () => canopyColor.value,
  () => {
    thumbCache.clear();
  }
);

const patternThumbStyle = (id) => {
  const key = `${id}|${canopyColor.value}`;
  if (!thumbCache.has(key)) {
    const url = buildPatternThumbDataUrl(id, canopyColor.value);
    thumbCache.set(key, url);
  }

  const url = thumbCache.get(key);
  return {
    backgroundImage: `url(${url})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  };
};

const heritageBadgeClass = (level) => {
  if (level?.includes('国家级')) return 'badge-national';
  if (level?.includes('地方级')) return 'badge-local';
  return 'badge-craft';
};

const playGuide = (key) => {
  if (!voiceEnabled.value) return;
  if (typeof window === 'undefined') return;
  if (isVoicePaused.value) return;

  const src = GUIDE_AUDIO_SRC_MAP[key];
  if (!src) return;

  let audio = audioCache[src];
  if (!audio) {
    audio = new Audio(src);
    audioCache[src] = audio;
  }

  try {
    if (currentGuideAudio && currentGuideAudio !== audio) {
      try {
        currentGuideAudio.pause();
        currentGuideAudio.currentTime = 0;
      } catch {
        // ignore pause errors
      }
    }
    currentGuideAudio = audio;
    audio.currentTime = 0;
    audio.play().catch(() => {
      // ignore autoplay errors
    });
  } catch {
    // ignore unexpected errors
  }
};

const toggleVoicePause = () => {
  if (typeof window === 'undefined') return;
  if (!voiceEnabled.value) return;

  const next = !isVoicePaused.value;
  isVoicePaused.value = next;

  if (currentGuideAudio) {
    try {
      if (next) {
        currentGuideAudio.pause();
      } else {
        currentGuideAudio.play().catch(() => {
          // ignore autoplay errors
        });
      }
    } catch {
      // ignore
    }
  }
};

const stopVoicePlayback = () => {
  if (typeof window === 'undefined') return;
  currentUtterance = null;

  if (ttsAbortController) {
    try {
      ttsAbortController.abort();
    } catch {
      // ignore
    }
    ttsAbortController = null;
  }

  if (currentGuideAudio) {
    try {
      currentGuideAudio.pause();
      currentGuideAudio.currentTime = 0;
    } catch {
      // ignore
    }
  }
  currentGuideAudio = null;
};

const pickPreferredZhVoice = () => {
  if (typeof window === 'undefined' || !('speechSynthesis' in window)) return;
  try {
    const voices = window.speechSynthesis.getVoices?.() ?? [];
    if (!Array.isArray(voices) || voices.length === 0) return;

    const zhVoices = voices.filter((v) => {
      const lang = (v?.lang ?? '').toLowerCase();
      return lang.startsWith('zh');
    });
    if (zhVoices.length === 0) return;

    const score = (v) => {
      const name = `${v?.name ?? ''} ${v?.voiceURI ?? ''}`.toLowerCase();
      let s = 0;
      if ((v?.lang ?? '').toLowerCase() === 'zh-cn') s += 20;
      if (v?.localService) s += 5;
      if (name.includes('xiaoxiao') || name.includes('yunxi') || name.includes('xiaoyi') || name.includes('xiaohan')) s += 10;
      if (name.includes('microsoft')) s += 6;
      if (name.includes('google')) s += 4;
      if (name.includes('tts') || name.includes('robot')) s -= 10;
      return s;
    };

    preferredZhVoice = zhVoices.slice().sort((a, b) => score(b) - score(a))[0] ?? null;
  } catch {
    // ignore
  }
};

const loadTipPrefs = () => {
  if (typeof window === 'undefined') return;
  const data = readJsonFromStorage(TIP_PREF_KEY);
  if (!data) return;
  if (typeof data.enableTips === 'boolean') {
    enableTips.value = data.enableTips;
  }
  if (Array.isArray(data.shownIds)) {
    shownTipIdSet = new Set(data.shownIds);
  }
};

const saveTipPrefs = () => {
  if (typeof window === 'undefined') return;
  writeJsonToStorage(TIP_PREF_KEY, {
    enableTips: enableTips.value,
    shownIds: Array.from(shownTipIdSet),
  });
};

watch(
  oilLayers,
  (val, oldVal) => {
    if (val === 3 && val !== oldVal) {
      speakText('刷三遍桐油是泸州油纸伞的特色，防水性更好，伞面也更耐用。');
    }
  }
);

watch(
  voiceEnabled,
  (enabled) => {
    if (!enabled) {
      isVoicePaused.value = false;
      stopVoicePlayback();
      return;
    }
    pickPreferredZhVoice();
  }
);

const maybeShowTip = (trigger) => {
  if (!enableTips.value) return;
  const tip = getRandomTip(trigger, Array.from(shownTipIdSet));
  if (!tip) return;
  currentTip.value = tip;
  showKnowledgePopup.value = true;
  shownTipIdSet.add(tip.id);
  saveTipPrefs();
};

const closeKnowledgePopup = () => {
  showKnowledgePopup.value = false;
  if (dontShowTipsAgain.value) {
    enableTips.value = false;
  }
  saveTipPrefs();
};

const applyCatalogPattern = (item) => {
  if (item.patternId) {
    patternId.value = item.patternId;
  }
  if (item.customPatternUrl) {
    customPatternUrl.value = item.customPatternUrl;
  } else if (!item.patternId) {
    customPatternUrl.value = '';
  }
  showPatternDrawer.value = false;
  maybeShowTip('catalog_apply');
};

const onSelectPattern = (id) => {
  patternId.value = id;
  if (id && id !== 'none') {
    playGuide('select_pattern');
  }
};

const onPatternScaleChange = () => {
  playGuide('scale_pattern');
};

const onUploadPattern = (file) => {
  const content = file?.content;
  if (typeof content === 'string' && content.startsWith('data:')) {
    customPatternUrl.value = content;
    playGuide('upload_pattern');
    return;
  }

  const raw = file?.file;
  if (!raw) {
    showToast({ type: 'fail', message: '读取图片失败' });
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    const url = reader.result;
    if (typeof url === 'string') {
      customPatternUrl.value = url;
      playGuide('upload_pattern');
    }
  };
  reader.readAsDataURL(raw);
};

const clearCustomPattern = () => {
  customPatternUrl.value = '';
};

const getPatternDescription = (id) => {
  const descriptions = {
    'cloud': '吉祥如意',
    'lotus': '出淤泥而不染',
    'plum': '傲雪凌霜',
    'landscape': '山水如画'
  };
  return descriptions[id] || '';
};

const saveWork = async () => {
  const umbrellaDataURL = umbrella3dRef.value?.getPngDataUrl?.();
  if (!umbrellaDataURL) {
    showToast({ type: 'fail', message: '保存失败：画面未准备好' });
    return;
  }
  
  try {
    // 合成背景和3D渲染内容
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    // 获取viewer-panel的尺寸
    const viewerPanel = viewerPanelRef.value;
    if (!viewerPanel) {
      showToast({ type: 'fail', message: '保存失败：无法获取画布尺寸' });
      return;
    }
    
    const rect = viewerPanel.getBoundingClientRect();
    const width = Math.floor(rect.width);
    const height = Math.floor(rect.height);
    canvas.width = width;
    canvas.height = height;
    
    // 加载背景图片
    const bgImage = new Image();
    bgImage.crossOrigin = 'anonymous';
    
    await new Promise((resolve, reject) => {
      bgImage.onload = () => {
        // 绘制背景
        ctx.drawImage(bgImage, 0, 0, width, height);
        
        // 加载3D渲染的图片并绘制在上面
        const umbrellaImage = new Image();
        umbrellaImage.onload = () => {
          ctx.drawImage(umbrellaImage, 0, 0, width, height);
          resolve();
        };
        umbrellaImage.onerror = () => {
          reject(new Error('加载3D渲染图片失败'));
        };
        umbrellaImage.src = umbrellaDataURL;
      };
      bgImage.onerror = () => {
        reject(new Error('加载背景图片失败'));
      };
      bgImage.src = '/images/bg.png';
    });
    
    const finalDataURL = canvas.toDataURL('image/png');
    
    const userId = localStorage.getItem('userId');
    if (!userId) {
      showToast('请先登录');
      return;
    }

    const payload = {
      userId: parseInt(userId),
      imageData: finalDataURL,
    };

    await request.post('/works', payload);
    
    showToast({ type: 'success', message: '作品保存成功' });
    showImagePreview([finalDataURL]);
    maybeShowTip('save_success');
    playGuide('save_work');
  } catch (error) {
    console.error('Save failed:', error);
    showToast({ type: 'fail', message: '保存失败: ' + (error.message || '未知错误') });
  }
};

watch(
  patternId,
  (newVal, oldVal) => {
    if (newVal && newVal !== 'none' && newVal !== oldVal) {
      maybeShowTip('pattern_change');
    }
  }
);

watch(
  enableTips,
  () => {
    saveTipPrefs();
  }
);

onMounted(() => {
  loadTipPrefs();
  maybeShowTip('first_enter');
});
</script>

<style scoped>
/* ========== 页面容器 ========== */
.page-container {
  min-height: 100vh;
  padding-bottom: 80px;
  background:
    radial-gradient(ellipse at 20% 0%, rgba(185, 222, 201, 0.25) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 100%, rgba(220, 180, 130, 0.2) 0%, transparent 50%),
    linear-gradient(180deg, #fdfbf6 0%, #f8faf8 50%, #f0f5f2 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-x: hidden;
}

/* ========== 装饰背景 ========== */
.decorative-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.cloud-decoration {
  position: absolute;
  width: 180px;
  height: 90px;
  background: radial-gradient(ellipse, rgba(74, 106, 111, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(2px);
  animation: float 12s ease-in-out infinite;
}

.cloud-1 { top: 15%; left: 5%; animation-delay: 0s; }
.cloud-2 { bottom: 25%; right: 8%; animation-delay: -6s; transform: scale(0.8); }

.ink-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
}

.blob-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  right: -50px;
  background: radial-gradient(circle, rgba(74, 106, 111, 0.2), transparent);
}

.blob-2 {
  width: 250px;
  height: 250px;
  bottom: 20%;
  left: -30px;
  background: radial-gradient(circle, rgba(220, 180, 130, 0.25), transparent);
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-15px) scale(1.02); }
}

/* ========== 顶部导航 ========== */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(247,241,227,0.9));
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(74, 106, 111, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 700;
  color: #2d3a3c;
  letter-spacing: 0.1em;
}

.header-subtitle {
  font-size: 0.75rem;
  color: rgba(74, 106, 111, 0.7);
  letter-spacing: 0.05em;
}

.save-btn {
  border-radius: 20px !important;
  padding: 0 16px !important;
  font-weight: 600 !important;
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.25);
}

/* ========== 主布局 ========== */
.main-layout {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  position: relative;
  z-index: 1;
}

.viewer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ========== 3D 视图面板 ========== */
.viewer-panel {
  position: relative;
  width: 100%;
  height: 50vh;
  min-height: 320px;
  max-height: 480px;
  border-radius: 24px;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(247,241,227,0.95), rgba(255,255,255,0.9));
  box-shadow:
    0 20px 40px rgba(0,0,0,0.08),
    0 0 0 1px rgba(74, 106, 111, 0.1),
    inset 0 1px 0 rgba(255,255,255,0.8);
}

.viewer-bg {
  position: absolute;
  inset: 0;
  background-image: url('/images/bg.png');
  background-size: cover;
  background-position: center;
  pointer-events: none;
}

.viewer {
  position: absolute;
  inset: 0;
}

/* ========== 工艺卡片条 ========== */
.craft-strip {
  padding: 14px 16px;
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(247,241,227,0.9));
  border-radius: 20px;
  border: 1px solid rgba(74, 106, 111, 0.1);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.craft-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.craft-title {
  font-size: 14px;
  font-weight: 700;
  color: #2d3a3c;
}

.craft-count {
  font-size: 12px;
  color: rgba(74, 106, 111, 0.7);
  padding: 4px 10px;
  background: rgba(74, 106, 111, 0.08);
  border-radius: 12px;
}

.craft-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
}

.craft-list::-webkit-scrollbar { display: none; }

.craft-card {
  min-width: 160px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  background: rgba(255,255,255,0.9);
  border: 1px solid rgba(74, 106, 111, 0.12);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.craft-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(74, 106, 111, 0.15);
  border-color: rgba(74, 106, 111, 0.25);
}

.craft-card-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(74, 106, 111, 0.1);
  border-radius: 10px;
  font-size: 16px;
}

.craft-card-content {
  flex: 1;
  min-width: 0;
}

.craft-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #2d3a3c;
  margin-bottom: 4px;
}

.craft-card-desc {
  font-size: 11px;
  color: rgba(45, 58, 60, 0.65);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

/* ========== 工具面板 ========== */
.tools-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-card {
  background: linear-gradient(145deg, rgba(255,255,255,0.98), rgba(250,252,250,0.95));
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(74, 106, 111, 0.1);
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
}

.panel-card.primary-card {
  background: linear-gradient(145deg, rgba(74, 106, 111, 0.08), rgba(255,255,255,0.95));
  border-color: rgba(74, 106, 111, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.card-icon {
  position: relative;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
}

.card-icon--photo img {
  position: absolute;
  width: 82%;
  height: 82%;
  top: 9%;
  left: 9%;
  right: auto;
  bottom: auto;
}

.card-icon--box {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(74, 106, 111, 0.12);
}

.card-icon--box svg {
  color: #4A6A6F;
}

.card-icon img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  object-position: center;
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #2d3a3c;
  letter-spacing: 0.02em;
}

/* 开关组 */
.toggle-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.toggle-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(250,252,250,0.8));
  border-radius: 14px;
  border: 1px solid rgba(74, 106, 111, 0.12);
  transition: all 0.2s ease;
}

.toggle-item:hover {
  border-color: rgba(74, 106, 111, 0.2);
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.08);
}

.toggle-label {
  font-size: 14px;
  font-weight: 500;
  color: #2d3a3c;
}

/* 提示横幅 */
.tip-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: rgba(74, 106, 111, 0.06);
  border-radius: 14px;
  border: 1px dashed rgba(74, 106, 111, 0.2);
  font-size: 12px;
  color: rgba(45, 58, 60, 0.8);
  transition: all 0.3s ease;
}

.tip-banner.active {
  background: rgba(74, 106, 111, 0.1);
  border-style: solid;
  border-color: rgba(74, 106, 111, 0.3);
}

.tip-icon { font-size: 16px; }

/* 选项组 */
.option-group {
  margin-bottom: 14px;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: rgba(45, 58, 60, 0.85);
  margin-bottom: 10px;
}

.option-label::before {
  content: '';
  width: 4px;
  height: 14px;
  background: linear-gradient(180deg, #4A6A6F, #6B8E23);
  border-radius: 2px;
}

.pill-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pill {
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid rgba(74, 106, 111, 0.2);
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(250,252,250,0.9));
  font-size: 13px;
  font-weight: 500;
  color: #2d3a3c;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pill:hover {
  background: rgba(74, 106, 111, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.1);
}

.pill.active {
  background: linear-gradient(135deg, #4A6A6F, #5a7a7f);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.3);
}

.option-hint {
  padding: 12px;
  background: rgba(74, 106, 111, 0.05);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hint-main {
  font-size: 12px;
  color: #2d3a3c;
  font-weight: 500;
}

.hint-sub {
  font-size: 11px;
  color: rgba(45, 58, 60, 0.65);
}

/* ========== 色彩选择 - 圆形色块 ========== */
.color-section {
  margin-bottom: 14px;
}

.color-section:last-child { margin-bottom: 0; }

.color-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: rgba(45, 58, 60, 0.85);
  margin-bottom: 12px;
}

.color-label::before {
  content: '';
  width: 4px;
  height: 14px;
  background: linear-gradient(180deg, #4A6A6F, #6B8E23);
  border-radius: 2px;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
}

.color-grid.compact {
  grid-template-columns: repeat(8, 1fr);
}

.color-swatch {
  position: relative;
  aspect-ratio: 1;
  border-radius: 50%;
  border: 3px solid transparent;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.color-swatch::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  border: 2px solid rgba(74, 106, 111, 0.2);
  transition: all 0.2s ease;
}

.color-swatch:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}

.color-swatch:hover::before {
  border-color: rgba(74, 106, 111, 0.4);
}

.color-swatch.active {
  transform: scale(1.15);
  box-shadow: 0 0 0 3px #fff, 0 0 0 5px #4A6A6F, 0 8px 20px rgba(0,0,0,0.2);
}

.color-swatch.active::before {
  display: none;
}

.swatch-name {
  display: none;
}

/* ========== 图案选择 - 优化网格 ========== */
.pattern-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.pattern-card {
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  border: 2px solid transparent;
  background: rgba(255,255,255,0.9);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.pattern-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.pattern-card.active {
  border-color: #4A6A6F;
  box-shadow: 0 0 0 3px rgba(74, 106, 111, 0.15), 0 8px 20px rgba(0,0,0,0.1);
}

.pattern-thumb {
  height: 44px;
  background-color: #f5f5f5;
  position: relative;
}

.pattern-thumb::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(0,0,0,0.03));
}

.pattern-info {
  padding: 8px 6px;
  text-align: center;
}

.pattern-name {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: #2d3a3c;
  margin-bottom: 2px;
}

.pattern-desc {
  display: block;
  font-size: 9px;
  color: rgba(74, 106, 111, 0.8);
  line-height: 1.3;
}

/* 上传行：紧凑、不占位 */
.upload-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, rgba(74, 106, 111, 0.04) 0%, rgba(74, 106, 111, 0.02) 100%);
  border: 1px solid rgba(74, 106, 111, 0.08);
  border-radius: 10px;
}

.upload-label {
  font-size: 11px;
  color: rgba(45, 58, 60, 0.75);
  font-weight: 500;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pattern-uploader :deep(.van-uploader__upload) {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: 1px dashed rgba(74, 106, 111, 0.2);
  background: rgba(255, 255, 255, 0.8);
}

.pattern-uploader :deep(.van-uploader__preview) {
  width: 30px;
  height: 30px;
}

.pattern-uploader :deep(.van-uploader__preview-image) {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  object-fit: cover;
}

.pattern-uploader :deep(.van-uploader__upload-icon) {
  font-size: 14px;
  color: rgba(74, 106, 111, 0.6);
}

.upload-clear-btn {
  min-width: 36px !important;
  height: 26px !important;
  padding: 0 8px !important;
  font-size: 11px !important;
}

/* 滑块 */
.slider-section {
  margin-bottom: 12px;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: rgba(45, 58, 60, 0.8);
}

.slider-value {
  padding: 2px 10px;
  background: rgba(74, 106, 111, 0.1);
  border-radius: 10px;
  font-weight: 600;
  color: #4A6A6F;
}

/* 桐油层数 */
.oil-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.oil-label {
  font-size: 12px;
  color: rgba(45, 58, 60, 0.8);
}

.oil-pills {
  display: flex;
  gap: 6px;
}

.oil-btn {
  padding: 6px 12px;
  border-radius: 12px;
  border: 1px solid rgba(74, 106, 111, 0.25);
  background: rgba(255,255,255,0.9);
  font-size: 12px;
  color: #2d3a3c;
  cursor: pointer;
  transition: all 0.2s ease;
}

.oil-btn:hover { background: rgba(74, 106, 111, 0.1); }
.oil-btn.active {
  background: rgba(74, 106, 111, 0.9);
  border-color: rgba(74, 106, 111, 0.9);
  color: #fff;
}

/* ========== 悬浮按钮 ========== */
.floating-btn {
  position: fixed;
  right: 16px;
  bottom: 90px;
  z-index: 50;
  box-shadow: 0 8px 24px rgba(74, 106, 111, 0.4);
  padding: 0 20px !important;
  font-weight: 600 !important;
}

/* ========== 抽屉 ========== */
.pattern-drawer {
  padding: 0 16px 24px;
  border-radius: 24px 0 0 24px !important;
  background: linear-gradient(180deg, #fafbfc 0%, #f2f5f7 100%);
}

.drawer-header {
  position: relative;
  margin: 0 -16px 20px;
  padding: 24px 20px 20px;
  overflow: hidden;
}

.drawer-header-bg {
  position: absolute;
  inset: 0;
}

.drawer-pattern-bg {
  position: absolute;
  inset: 0;
  opacity: 0.04;
  background-image: radial-gradient(circle at 20% 50%, #4A6A6F 1px, transparent 1px),
    radial-gradient(circle at 80% 30%, #4A6A6F 1px, transparent 1px);
  background-size: 24px 24px;
}

.drawer-gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(74, 106, 111, 0.06) 0%, transparent 50%);
}

.drawer-header-content {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.drawer-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(74, 106, 111, 0.12) 0%, rgba(74, 106, 111, 0.06) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.drawer-icon {
  width: 22px;
  height: 22px;
  color: #4A6A6F;
}

.drawer-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a2527;
  letter-spacing: 0.02em;
}

.drawer-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(74, 106, 111, 0.75);
}

.subtitle-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #4A6A6F;
  opacity: 0.6;
}

.catalog-list {
  max-height: calc(100vh - 260px);
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: rgba(74, 106, 111, 0.25) transparent;
}

.catalog-list::-webkit-scrollbar {
  width: 4px;
}

.catalog-list::-webkit-scrollbar-thumb {
  background: rgba(74, 106, 111, 0.25);
  border-radius: 4px;
}

.catalog-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  margin-bottom: 10px;
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
  box-shadow: 0 2px 8px rgba(74, 106, 111, 0.06);
  border: 1px solid rgba(74, 106, 111, 0.06);
  animation: catalogItemFade 0.4s ease backwards;
}

@keyframes catalogItemFade {
  from {
    opacity: 0;
    transform: translateX(12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.catalog-item:hover {
  background: #fff;
  transform: translateX(6px) translateY(-2px);
  box-shadow: 0 8px 24px rgba(74, 106, 111, 0.12);
  border-color: rgba(74, 106, 111, 0.12);
}

.catalog-item:active {
  transform: translateX(4px) scale(0.99);
}

.catalog-thumb-wrap {
  position: relative;
  flex-shrink: 0;
}

.catalog-thumb {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  background-color: #f0f2f4;
  border: 1px solid rgba(74, 106, 111, 0.1);
  overflow: hidden;
}

.catalog-thumb-overlay {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  background: linear-gradient(180deg, transparent 50%, rgba(0, 0, 0, 0.03) 100%);
  pointer-events: none;
}

.catalog-content {
  flex: 1;
  min-width: 0;
}

.catalog-name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.catalog-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a2527;
}

.heritage-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: 500;
  flex-shrink: 0;
}

.heritage-badge.badge-national {
  background: linear-gradient(135deg, rgba(180, 80, 60, 0.15) 0%, rgba(180, 80, 60, 0.08) 100%);
  color: #8B3A2E;
}

.heritage-badge.badge-local {
  background: linear-gradient(135deg, rgba(74, 106, 111, 0.15) 0%, rgba(74, 106, 111, 0.08) 100%);
  color: #4A6A6F;
}

.heritage-badge.badge-craft {
  background: linear-gradient(135deg, rgba(90, 62, 43, 0.12) 0%, rgba(90, 62, 43, 0.06) 100%);
  color: #5A3E2B;
}

.catalog-meta {
  font-size: 12px;
  color: rgba(74, 106, 111, 0.7);
  margin-bottom: 6px;
}

.catalog-meaning {
  font-size: 13px;
  color: rgba(45, 58, 60, 0.82);
  line-height: 1.55;
  margin-bottom: 8px;
}

.catalog-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #4A6A6F;
  font-weight: 500;
}

.apply-arrow {
  width: 14px;
  height: 14px;
  opacity: 0.8;
  transition: transform 0.2s ease;
}

.catalog-item:hover .apply-arrow {
  transform: translateX(2px);
}

.drawer-footer {
  margin-top: 20px;
  padding: 16px 0 0;
  border-top: 1px solid rgba(74, 106, 111, 0.08);
}

.footer-toggle-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #2d3a3c;
  font-weight: 500;
}

.footer-hint {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(74, 106, 111, 0.6);
}

/* ========== 弹窗 ========== */
.knowledge-popup {
  padding: 20px 20px 28px;
  border-radius: 24px 24px 0 0 !important;
}

.knowledge-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.knowledge-tag {
  display: inline-flex;
  padding: 4px 12px;
  background: rgba(74, 106, 111, 0.1);
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #4A6A6F;
  width: fit-content;
}

.knowledge-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #2d3a3c;
}

.knowledge-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(45, 58, 60, 0.9);
}

.knowledge-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.knowledge-hint {
  font-size: 12px;
  color: rgba(74, 106, 111, 0.7);
}

/* ========== 响应式布局 ========== */
@media (min-width: 768px) {
  .main-layout {
    flex-direction: row;
    gap: 20px;
  }

  .viewer-section {
    flex: 1.2;
    min-width: 0;
  }

  .tools-panel {
    flex: 1;
    max-width: 400px;
    max-height: calc(100vh - 100px);
    overflow-y: auto;
    scrollbar-width: thin;
  }

  .viewer-panel {
    height: auto;
    min-height: 400px;
    max-height: none;
    aspect-ratio: 4/3;
  }

  .color-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .pattern-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1024px) {
  .viewer-section {
    flex: 1.4;
  }

  .tools-panel {
    flex: 0.85;
    max-width: 380px;
  }

  .color-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
