<template>
  <div class="page-container">
    <div class="header-fixed">
      <div class="header-logo">
        <InkLogo :size="40" />
      </div>
    </div>
    
    <div class="chat-container" ref="chatContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper" :class="msg.role">
        <div class="avatar-logo" v-if="msg.role === 'ai'">
          <InkLogo :size="40" />
        </div>
        <div class="message-content">
          <div class="bubble">{{ msg.content }}</div>
        </div>
        <div class="avatar" v-if="msg.role === 'user'">
          <img src="/images/16.jpg" alt="User" />
        </div>
      </div>
      <div v-if="loading" class="message-wrapper ai">
        <div class="avatar-logo">
          <InkLogo :size="40" />
        </div>
        <div class="message-content">
          <div class="bubble loading">
            <van-loading type="spinner" size="20px" />
          </div>
        </div>
      </div>
    </div>

    <div class="input-area">
      <div
        class="voice-btn"
        :class="{ disabled: !voiceSupported }"
        @click="toggleVoice"
      >
        <van-icon
          :name="isListening ? 'stop-circle-o' : 'volume-o'"
          size="28"
          :color="isListening ? '#f00' : '#4A6A6F'"
        />
        <span class="voice-status" :class="{ listening: isListening }">
          {{ voiceLabel }}
        </span>
      </div>
      <van-field
        v-model="inputText"
        placeholder="输入您的问题..."
        class="chat-input"
        :disabled="loading"
        @keyup.enter="sendMessage"
      />
      <van-button 
        type="primary" 
        size="small" 
        color="#4A6A6F" 
        :disabled="loading || !inputText.trim()"
        @click="sendMessage"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, onUnmounted } from 'vue';
import { showToast } from 'vant';
import InkLogo from '@/components/InkLogo.vue';
import request from '@/utils/request';

const messages = ref([
  { role: 'ai', content: '您好！我是撑花AI，您的非遗文化助手。请问有什么关于油纸伞的问题我可以帮您？' }
]);
const inputText = ref('');
const loading = ref(false);
const chatContainer = ref(null);
const isListening = ref(false);
const voiceSupported = ref(false);
const voiceError = ref('');
let recognition = null;
let preferredTtsVoice = null;
const NATURAL_VOICE_HINTS = ['xiaoxiao', 'xiaoyi', 'yunxi', 'neural', 'natural', 'premium', 'female', '女'];
const ROBOTIC_VOICE_HINTS = ['robot', 'synth', 'compact', 'old', 'default', 'sampler', 'espeak'];

const voiceLabel = computed(() => {
  if (!voiceSupported.value) return '不可用';
  if (isListening.value) return '识别中';
  return '语音';
});

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
};

const stopSpeaking = () => {
  if (typeof window === 'undefined' || !window.speechSynthesis) return;
  try {
    window.speechSynthesis.cancel();
  } catch (error) {
    console.error('Speech synthesis cancel error', error);
  }
};

const pickPreferredTtsVoice = () => {
  if (typeof window === 'undefined' || !window.speechSynthesis) return;
  const voices = window.speechSynthesis.getVoices?.() ?? [];
  if (!Array.isArray(voices) || voices.length === 0) return;

  const zhVoices = voices.filter((voice) => {
    const lang = `${voice?.lang || ''}`.toLowerCase();
    return lang.startsWith('zh');
  });

  const candidates = zhVoices.length > 0 ? zhVoices : voices;
  const scoreVoice = (voice) => {
    const name = `${voice?.name ?? ''} ${voice?.voiceURI ?? ''}`.toLowerCase();
    const lang = `${voice?.lang ?? ''}`.toLowerCase();
    let score = 0;

    if (lang === 'zh-cn') score += 40;
    else if (lang.startsWith('zh')) score += 20;
    if (voice?.localService) score += 8;
    if (voice?.default) score += 6;

    if (NATURAL_VOICE_HINTS.some((hint) => name.includes(hint))) score += 30;
    if (ROBOTIC_VOICE_HINTS.some((hint) => name.includes(hint))) score -= 20;

    return score;
  };

  preferredTtsVoice = candidates.slice().sort((a, b) => scoreVoice(b) - scoreVoice(a))[0] ?? null;
};

const speakAiReply = (content) => {
  if (!content || typeof window === 'undefined' || !window.speechSynthesis || !window.SpeechSynthesisUtterance) return;

  stopSpeaking();
  const utterance = new window.SpeechSynthesisUtterance(content);
  utterance.lang = 'zh-CN';
  utterance.rate = 0.92;
  utterance.pitch = 1.02;
  utterance.volume = 1;
  if (preferredTtsVoice) {
    utterance.voice = preferredTtsVoice;
  }
  window.speechSynthesis.speak(utterance);
};

const loadHistory = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) return; // Don't load history if not logged in (or maybe show empty)
  
  try {
    const res = await request.get(`/ai/history?userId=${userId}`);
    if (res && res.length > 0) {
      messages.value = res.map(msg => ({
        role: msg.role,
        content: msg.content
      }));
    }
  } catch (error) {
    console.error('Failed to load history:', error);
  }
};

const sendMessage = async () => {
  if (!inputText.value.trim()) return;
  
  const userMsg = inputText.value;
  messages.value.push({ role: 'user', content: userMsg });
  inputText.value = '';
  loading.value = true;
  scrollToBottom();

  try {
    const userId = localStorage.getItem('userId');
    const response = await request.post('/ai/chat', {
      message: userMsg,
      userId: userId ? parseInt(userId) : 1
    }, { timeout: 30000 });
    
    const reply = response?.reply ?? response?.data?.reply;
    if (reply) {
      messages.value.push({ role: 'ai', content: reply });
      speakAiReply(reply);
    } else {
       messages.value.push({ role: 'ai', content: '抱歉，我暂时无法回答这个问题。' });
    }
    if (!reply) {
      const latestAi = messages.value[messages.value.length - 1];
      if (latestAi?.role === 'ai') {
        speakAiReply(latestAi.content);
      }
    }
  } catch (error) {
    console.error(error);
    let errorMsg = error?.response?.data?.reply || '网络连接异常，请稍后再试。';
    if (error?.code === 'ECONNABORTED' || error?.message?.includes('timeout')) {
      errorMsg = 'AI 响应超时，请稍后再试。';
    }
    messages.value.push({ role: 'ai', content: errorMsg });
    speakAiReply(errorMsg);
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

const toggleVoice = () => {
  if (!voiceSupported.value) {
    showToast('当前环境不支持语音输入，请使用 Chrome / Edge 并确保 HTTPS');
    return;
  }
  if (loading.value) return;
  if (isListening.value) {
    stopListening();
  } else {
    startListening();
  }
};

const normalizeVoiceError = (errorCode) => {
  switch (errorCode) {
    case 'not-allowed':
    case 'service-not-allowed':
      return '麦克风权限被拒绝，请在浏览器设置中允许麦克风访问';
    case 'audio-capture':
      return '未检测到可用麦克风设备';
    case 'no-speech':
      return '未识别到语音，请稍后重试';
    case 'network':
      return '语音识别网络异常，请检查网络后重试';
    case 'aborted':
      return '';
    default:
      return '语音识别失败，请稍后重试';
  }
};

const createRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) return null;

  const instance = new SpeechRecognition();
  instance.lang = 'zh-CN';
  instance.continuous = false;
  instance.interimResults = true;
  instance.maxAlternatives = 1;

  instance.onstart = () => {
    voiceError.value = '';
    isListening.value = true;
    showToast('正在聆听，请开始说话');
  };

  instance.onend = () => {
    isListening.value = false;
  };

  instance.onresult = (event) => {
    let finalText = '';
    for (let i = event.resultIndex; i < event.results.length; i += 1) {
      if (event.results[i].isFinal) {
        finalText += event.results[i][0].transcript;
      }
    }
    if (!finalText) return;
    inputText.value = inputText.value ? `${inputText.value}${finalText}` : finalText;
  };

  instance.onerror = (event) => {
    console.error('Speech recognition error', event.error);
    const msg = normalizeVoiceError(event.error);
    voiceError.value = msg;
    isListening.value = false;
    if (msg) showToast(msg);
  };

  return instance;
};

const startListening = () => {
  if (!recognition) {
    recognition = createRecognition();
  }
  if (!recognition) {
    showToast('您的浏览器不支持语音输入');
    return;
  }
  if (isListening.value) return;

  try {
    stopSpeaking();
    recognition.start();
  } catch (error) {
    console.error('Speech start error', error);
    showToast('语音输入启动失败，请稍后重试');
  }
};

const stopListening = () => {
  if (!recognition || !isListening.value) return;

  try {
    recognition.stop();
  } catch (error) {
    console.error('Speech stop error', error);
  }
};

const setupVoice = () => {
  voiceSupported.value = !!(window.SpeechRecognition || window.webkitSpeechRecognition);
  if (!voiceSupported.value) return;

  recognition = createRecognition();
};

onMounted(async () => {
  setupVoice();
  pickPreferredTtsVoice();
  if (window?.speechSynthesis && typeof window.speechSynthesis.addEventListener === 'function') {
    window.speechSynthesis.addEventListener('voiceschanged', pickPreferredTtsVoice);
  }
  await loadHistory();
  scrollToBottom();
});

onUnmounted(() => {
  stopSpeaking();
  stopListening();
  if (window?.speechSynthesis && typeof window.speechSynthesis.removeEventListener === 'function') {
    window.speechSynthesis.removeEventListener('voiceschanged', pickPreferredTtsVoice);
  }
  recognition = null;
});
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: linear-gradient(rgba(255,255,255,0.85), rgba(221, 255, 238, 0.8));
  position: relative;
  overflow: hidden;
}

/* Background Decoration */
.page-container::before {
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

.page-container::after {
  content: "撑花";
  position: absolute;
  top: 30%;
  left: 30%;
  font-size: 8rem;
  color: rgba(74, 106, 111, 0.03);
  writing-mode: vertical-rl;
  font-weight: bold;
  pointer-events: none;
  z-index: 0;
  font-family: var(--font-family-ancient);
}

.header-fixed {
  background: rgba(185, 222, 201, 0.4);
  padding: 10px 0;
  box-shadow: 0 2px 4px rgba(74, 106, 111, 0.1);
  z-index: 10;
  backdrop-filter: blur(5px);
}

.header-logo {
  display: flex;
  justify-content: center;
  align-items: center;
}

.quick-questions {
  padding: 12px 15px;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.6);
  border-bottom: 1px solid rgba(74, 106, 111, 0.08);
}
.quick-label {
  font-size: 13px;
  color: #4A6A6F;
  margin-right: 8px;
  vertical-align: middle;
}
.chips {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
}
.chips .van-tag {
  cursor: pointer;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  padding-bottom: 160px; /* Increased space to prevent content from being hidden behind the input area */
  position: relative;
  z-index: 1;
  scroll-behavior: smooth; /* Add smooth scrolling */
}

.message-wrapper {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message-wrapper.user {
  flex-direction: row;
  justify-content: flex-end;
}

.avatar, .avatar-logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #EAEAE0;
}

.avatar-logo {
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid rgba(74, 106, 111, 0.2);
}

.avatar img {
  width: 100%;
  height: 100%;
}

.message-content {
  max-width: 70%;
  margin: 0 10px;
}

.bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.5;
  color: #333;
  box-shadow: 0 1px 2px rgba(74, 106, 111, 0.1);
  word-wrap: break-word;
  border: 1px solid rgba(74, 106, 111, 0.1);
}

.user .bubble {
  background: #4A6A6F;
  color: #fff;
  border: none;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
}

.input-area {
  position: fixed;
  bottom: 80px; /* Raised to avoid blocking the protruding AI icon */
  left: 15px;
  right: 15px;
  width: auto;
  background: rgba(255, 255, 255, 0.95);
  padding: 10px 15px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  border-radius: 24px;
  box-shadow: 0 4px 15px rgba(74, 106, 111, 0.15);
  backdrop-filter: blur(10px);
  z-index: 10;
}

.voice-btn {
  margin-right: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.voice-btn.disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.voice-status {
  font-size: 12px;
  color: #5b6f73;
  user-select: none;
}

.voice-status.listening {
  color: #e65454;
}

.chat-input {
  flex: 1;
  background: #f0f0eb;
  border-radius: 20px;
  padding: 5px 15px;
  margin-right: 10px;
}

:deep(.van-field__control) {
  background: transparent;
}
</style>
