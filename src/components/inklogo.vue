<template>
  <div class="q-logo" :style="{ width: size + 'px', height: size + 'px' }">
    <svg viewBox="0 0 120 120" class="logo-svg" xmlns="http://www.w3.org/2000/svg">
      <!-- Q-style Background Blob -->
      <path d="M60,10 Q110,10 110,60 Q110,110 60,110 Q10,110 10,60 Q10,10 60,10" fill="#FFFAF0" stroke="#FFD700" stroke-width="3" />
      
      <!-- Cute Umbrella Icon (Small) -->
      <g transform="translate(80, 10) rotate(15) scale(0.4)">
         <path d="M10,60 Q50,10 90,60" fill="#FF7F50" stroke="#8B4513" stroke-width="2" />
         <!-- <path d="M50,10 L50,60" fill="none" stroke="#8B4513" stroke-width="2" /> -->
         <path d="M50,60 L50,85" fill="none" stroke="#8B4513" stroke-width="3" stroke-linecap="round" />
      </g>

      <!-- Q-style Text "撑花" -->
      <!-- Using SVG text with rounded font stack -->
      <text x="60" y="75" text-anchor="middle" font-size="40" fill="#8B4513" font-family="'YouYuan', 'Varela Round', 'Rounded Mplus 1c', sans-serif" font-weight="bold" letter-spacing="2">
        撑花
      </text>
      
      <!-- Cute Decor dots -->
      <circle cx="20" cy="30" r="4" fill="#FFB6C1" />
      <circle cx="100" cy="90" r="5" fill="#87CEFA" />
      <circle cx="30" cy="95" r="3" fill="#98FB98" />
    </svg>
  </div>
</template>

<script setup>
defineProps({
  size: {
    type: Number,
    default: 100
  }
})
</script>

<style scoped>
.q-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  animation: float 3s ease-in-out infinite;
}

.logo-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 0px rgba(74, 106, 111, 0.1));
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
</style>
