<template>
  <Teleport to="body">
    <Transition name="celebration">
      <div v-if="isVisible" class="celebration-overlay" @click="close">
        <!-- 빵빠레 / 폭죽 효과 -->
        <div class="confetti-container">
          <div v-for="i in 50" :key="i" class="confetti" :style="getConfettiStyle(i)"></div>
        </div>

        <!-- 축하 메시지 -->
        <div class="celebration-content" @click.stop>
          <div class="celebration-icon">🎉</div>
          <h2 class="celebration-title">축하합니다!</h2>
          <p class="celebration-message">{{ message }}</p>
          <div class="celebration-sub">{{ subMessage }}</div>
          <button class="btn btn-primary celebration-btn" @click="close">
            확인
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: '소설이 완결되었습니다!'
  },
  subMessage: {
    type: String,
    default: '여러분의 상상력이 하나의 이야기가 되었습니다 ✨'
  }
})

const emit = defineEmits(['close'])

const isVisible = ref(false)

watch(() => props.show, (newVal) => {
  if (newVal) {
    isVisible.value = true
    // 자동으로 5초 후 닫기
    setTimeout(() => {
      close()
    }, 8000)
  }
})

const close = () => {
  isVisible.value = false
  emit('close')
}

// 폭죽 파티클 스타일 생성
const getConfettiStyle = (index) => {
  const colors = ['#E85D75', '#845EF7', '#FFB4C8', '#FFD3E0', '#FF6B9D', '#9775FA', '#F06595']
  const randomColor = colors[index % colors.length]
  const randomX = Math.random() * 100
  const randomDelay = Math.random() * 3
  const randomDuration = 2 + Math.random() * 2
  const randomSize = 8 + Math.random() * 8
  
  return {
    '--confetti-color': randomColor,
    '--start-x': `${randomX}vw`,
    '--end-x': `${randomX + (Math.random() - 0.5) * 30}vw`,
    '--fall-delay': `${randomDelay}s`,
    '--fall-duration': `${randomDuration}s`,
    '--size': `${randomSize}px`,
    '--rotation': `${Math.random() * 360}deg`
  }
}
</script>

<style scoped>
.celebration-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  overflow: hidden;
}

.confetti-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.confetti {
  position: absolute;
  top: -20px;
  width: var(--size);
  height: var(--size);
  background: var(--confetti-color);
  opacity: 0.9;
  animation: confetti-fall var(--fall-duration) ease-out var(--fall-delay) forwards;
  transform: rotate(var(--rotation));
  left: var(--start-x);
}

.confetti:nth-child(odd) {
  border-radius: 50%;
}

.confetti:nth-child(3n) {
  border-radius: 2px;
  transform: rotate(45deg);
}

@keyframes confetti-fall {
  0% {
    top: -20px;
    left: var(--start-x);
    opacity: 1;
    transform: rotate(0deg) scale(1);
  }
  100% {
    top: 110vh;
    left: var(--end-x);
    opacity: 0;
    transform: rotate(720deg) scale(0.5);
  }
}

.celebration-content {
  background: white;
  padding: 50px 40px;
  border-radius: 30px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  animation: pop-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  z-index: 10;
}

@keyframes pop-in {
  0% {
    transform: scale(0.5);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.celebration-icon {
  font-size: 5rem;
  margin-bottom: 20px;
  animation: bounce-icon 1s ease-in-out infinite;
}

@keyframes bounce-icon {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-15px) scale(1.1);
  }
}

.celebration-title {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #E85D75, #845EF7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 15px;
}

.celebration-message {
  font-size: 1.2rem;
  color: #2D3436;
  margin-bottom: 10px;
  font-weight: 600;
}

.celebration-sub {
  font-size: 0.95rem;
  color: #636E72;
  margin-bottom: 30px;
  line-height: 1.5;
}

.celebration-btn {
  padding: 14px 40px;
  font-size: 1.1rem;
}

/* Transition */
.celebration-enter-active {
  transition: opacity 0.3s ease;
}

.celebration-leave-active {
  transition: opacity 0.5s ease;
}

.celebration-enter-from,
.celebration-leave-to {
  opacity: 0;
}

/* Mobile */
@media (max-width: 480px) {
  .celebration-content {
    padding: 40px 25px;
    border-radius: 25px;
  }

  .celebration-icon {
    font-size: 4rem;
  }

  .celebration-title {
    font-size: 1.6rem;
  }

  .celebration-message {
    font-size: 1rem;
  }

  .celebration-btn {
    width: 100%;
  }
}
</style>
