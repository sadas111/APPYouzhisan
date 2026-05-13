<template>
  <div class="particle-container" :class="theme">
    <div class="texture-overlay"></div>
    <div class="umbrella-bg" v-if="showUmbrella"></div>
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  theme: {
    type: String,
    default: 'night', // 'night', 'day', 'ink', 'guide-droplet', 'warm-droplet', 'tech', 'fresh'
    validator: (value) => ['night', 'day', 'ink', 'guide-droplet', 'warm-droplet', 'tech', 'fresh'].includes(value)
  },
  particleCount: {
    type: Number,
    default: 60
  },
  showUmbrella: {
    type: Boolean,
    default: true
  },
  variant: {
    type: String,
    default: 'normal' // 'normal', 'violation'
  }
})

const container = ref(null)
const canvas = ref(null)
let ctx = null
let animationFrameId = null
let particles = []

// Theme Config
const themes = {
  // 1. 夜间模式 (Night Mode)
  night: {
    colors: ['#FFD700', '#FFA500', '#FFFFFF'], // Gold, Orange, White
    bg: 'linear-gradient(to bottom, #0f2027, #203a43, #2c5364)', // Deep Blue/Black
    particleSpeed: 0.5,
    glow: true
  },
  // 2. 日间模式 (Day Mode)
  day: {
    colors: ['#FFB7C5', '#FF69B4', '#FFC0CB'], // Sakura colors
    bg: 'linear-gradient(to bottom, #FFF1EB, #ACE0F9)', // Warm Light
    particleSpeed: 1,
    glow: false
  },
  // 3. 水墨风格 (Ink Style)
  ink: {
    colors: ['#98FB98', '#90EE90', '#8FBC8F', '#A2D9CE'], // Pale Green, Light Green, Dark Sea Green, Pale Turquoise
    bg: '#F5F5DC', // Beige Paper
    particleSpeed: 0.8,
    glow: false
  },
  // 4. 科技风格 (Tech Style)
  tech: {
    // colors: ['#4A6A6F', '#2F4F4F', '#5F9EA0'], // Dark Cyan, Dark Slate Gray, Cadet Blue
    bg: '#F5F5DC', // Beige Paper (keeping consistent with app style)
    particleSpeed: 1.2,
    glow: false
  },
  // 5. 清新主题 (Fresh Theme - 当前登录页使用)
  fresh: {
	colors: ['#f09ee9', '#da9ef0', '#b9dec9', '#4eef7e', '#4eefec'],     //适配淡黄色
    // colors: ['#bde2cd', '#d5fbe5', '#b9dec9', '#cdf3dd', '#C1E6C1'],    //适配清新竹黄绿
    bg: 'linear-gradient(to bottom, #fff, #dfe)', 
	// bg: '#f9f4dc',
    particleSpeed: 0.7,
    glow: true
  },
  // 6. 引导页水滴 (Guide Droplet)
  'guide-droplet': {
    colors: ['#FFDAB9', '#98FB98', '#E0FFFF'],  // Light Orange, PaleGreen, LightCyan
    bg: '#FFFFFF', 
    particleSpeed: 1.2,
    glow: true
  },
  // 7. 温暖水滴 (Warm Droplet)
  'warm-droplet': {
    colors: ['#FFDAB9', '#FFE4B5', '#FFEFD5'], // PeachPuff, Moccasin, PapayaWhip
    bg: 'linear-gradient(to bottom, #FFF5E6, #FFE4E1)', // Warm White to MistyRose
    particleSpeed: 1.0,
    glow: true
  }
}

class Particle {
  constructor(w, h, theme, variant) {
    this.w = w
    this.h = h
    this.theme = theme
    this.variant = variant
    this.reset()
  }

  reset() {
    this.x = Math.random() * this.w
    this.y = Math.random() * this.h
    this.size = Math.random() * 3 + 1
    
    const config = themes[this.theme] || themes.night
    
    if (this.theme === 'ink' && this.variant === 'violation') {
      const violationColors = ['#000000', '#1a1a1a', '#8B0000', '#FF0000']
      this.color = violationColors[Math.floor(Math.random() * violationColors.length)]
    } else {
      this.color = config.colors[Math.floor(Math.random() * config.colors.length)]
    }
    
    if (this.theme === 'day') {
      this.vx = (Math.random() - 0.5) * 1
      this.vy = Math.random() * 1.5 + 0.5
    } else if (this.theme.includes('droplet')) {
      // Droplet physics
      this.vx = (Math.random() - 0.5) * 0.5
      this.vy = Math.random() * 2 + 1
      this.size = Math.random() * 4 + 2
    } else if (this.theme === 'ink') {
      this.vx = (Math.random() - 0.5) * themes[this.theme].particleSpeed
      this.vy = (Math.random() - 0.5) * themes[this.theme].particleSpeed
      this.size = Math.random() * 4 + 2 // Larger particles for ink theme to be obvious
    } else if (this.theme === 'tech') {
      this.vx = (Math.random() - 0.5) * themes[this.theme].particleSpeed
      this.vy = (Math.random() - 0.5) * themes[this.theme].particleSpeed
      this.size = Math.random() * 2 + 1.5
    } else {
      this.vx = (Math.random() - 0.5) * themes[this.theme].particleSpeed
      this.vy = (Math.random() - 0.5) * themes[this.theme].particleSpeed
    }
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    if (this.y > this.h) {
      this.y = 0
      this.x = Math.random() * this.w
    }
    if (this.y < 0) {
      this.y = this.h
      this.x = Math.random() * this.w
    }
    if (this.x > this.w) this.x = 0
    if (this.x < 0) this.x = this.w
  }

  draw() {
    if (!ctx) return

    if (this.theme === 'ink') {
      ctx.beginPath()
      const len = this.size * 3
      // Use movement direction for line angle
      const angle = Math.atan2(this.vy, this.vx)
      
      ctx.moveTo(this.x, this.y)
      ctx.lineTo(this.x - Math.cos(angle) * len, this.y - Math.sin(angle) * len)
      
      ctx.strokeStyle = this.color
      ctx.lineWidth = 1.5
      ctx.globalAlpha = 0.8
      ctx.stroke()
      ctx.globalAlpha = 1.0
    } else {
      ctx.beginPath()
      if (this.theme === 'tech') {
        ctx.rect(this.x, this.y, this.size, this.size)
      } else {
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
      }
      ctx.fillStyle = this.color
      
      if (themes[this.theme].glow) {
        ctx.shadowBlur = 10
        ctx.shadowColor = this.color
      } else {
        ctx.shadowBlur = 0
      }
      
      if (this.theme.includes('droplet')) {
         ctx.globalAlpha = 0.6
      } else if (this.theme === 'fresh') {
         ctx.globalAlpha = Math.random() * 0.5 + 0.3 // Random transparency
      } else {
         ctx.globalAlpha = 1.0
      }
  
      ctx.fill()
      ctx.globalAlpha = 1.0
    }
  }
}

const init = () => {
  const cvs = canvas.value
  if (!cvs) return
  ctx = cvs.getContext('2d')
  
  const resize = () => {
    if (!canvas.value) return
    cvs.width = window.innerWidth
    cvs.height = window.innerHeight
    initParticles()
  }
  
  window.addEventListener('resize', resize)
  resize()
  animate()
}

const initParticles = () => {
  if (!canvas.value) return
  particles = []
  const { width, height } = canvas.value
  for (let i = 0; i < props.particleCount; i++) {
    particles.push(new Particle(width, height, props.theme, props.variant))
  }
}

const animate = () => {
  if (!ctx || !canvas.value) return
  const { width, height } = canvas.value
  const config = themes[props.theme] || themes.night
  
  // Clear canvas with background
  if (config.bg.startsWith('linear')) {
    const grad = ctx.createLinearGradient(0, 0, 0, height)
    // Simplified gradient parsing for demo - specific to our config
    if (props.theme === 'warm-droplet') {
        grad.addColorStop(0, '#FFF5E6')
        grad.addColorStop(1, '#FFE4E1')
    } else if (props.theme === 'day') {
        grad.addColorStop(0, '#FFF1EB')
        grad.addColorStop(1, '#ACE0F9')
    } else if (props.theme === 'fresh') {
        grad.addColorStop(0, '#fff')
        grad.addColorStop(1, '#dfe')
    } else {
        grad.addColorStop(0, '#0f2027')
        grad.addColorStop(1, '#2c5364')
    }
    ctx.fillStyle = grad
  } else {
    ctx.fillStyle = config.bg
  }
  
  ctx.fillRect(0, 0, width, height)
  
  particles.forEach(p => {
    p.update()
    p.draw()
  })
  
  // Tech theme connections
  if (props.theme === 'tech') {
    ctx.lineWidth = 0.5
    const connectionDistance = 100
    
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const p1 = particles[i]
        const p2 = particles[j]
        const dx = p1.x - p2.x
        const dy = p1.y - p2.y
        const dist = Math.sqrt(dx * dx + dy * dy)
        
        if (dist < connectionDistance) {
          ctx.beginPath()
          ctx.strokeStyle = `rgba(74, 106, 111, ${1 - dist / connectionDistance})` // #4A6A6F fade out
          ctx.moveTo(p1.x, p1.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.stroke()
        }
      }
    }
  }

  animationFrameId = requestAnimationFrame(animate)
}

onMounted(() => {
  init()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
  window.removeEventListener('resize', init)
})

watch(() => props.theme, () => {
  initParticles()
})
</script>

<style scoped>
.particle-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

/* Texture overlay removed as requested */
.texture-overlay {
  display: none;
}
</style>
