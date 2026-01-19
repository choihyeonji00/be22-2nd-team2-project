<template>
  <div :class="{ 'viewer-mode': isViewerPage }">
    <NavBar v-if="!isViewerPage" />

    <main :class="isViewerPage ? '' : 'container fade-in'" :style="isViewerPage ? '' : 'padding: 20px 20px 40px; min-height: 80vh;'">
      <RouterView />
    </main>

    <footer v-if="!isViewerPage">
      <div class="container text-center" style="padding: 30px 0; color: var(--text-muted); font-size: 0.9rem;">
        &copy; 2026 Team Next Page. All rights reserved.
      </div>
    </footer>

    <AuthModals />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import AuthModals from '@/components/AuthModals.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()

// 뷰어 페이지 여부 확인
const isViewerPage = computed(() => {
  return route.path.includes('/viewer')
})

onMounted(() => {
  authStore.checkAutoLogin()
})
</script>

<style>
/* Viewer mode - full screen without nav/footer */
.viewer-mode {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.viewer-mode main {
  flex: 1;
  padding: 0 !important;
  min-height: 100vh !important;
}
</style>
