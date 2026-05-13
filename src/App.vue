<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  
  <TabBar v-if="$route.meta.showTab" />
</template>

<script setup>
import TabBar from './components/TabBar.vue'
</script>

<style>
/* Global styles are in style.css */
</style>
