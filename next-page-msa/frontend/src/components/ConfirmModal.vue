<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="isVisible" class="confirm-overlay" @click.self="handleCancel">
        <div class="confirm-modal">
          <!-- 아이콘 -->
          <div class="confirm-icon" :class="type">
            <span v-if="type === 'warning'">⚠️</span>
            <span v-else-if="type === 'danger'">🗑️</span>
            <span v-else-if="type === 'info'">ℹ️</span>
            <span v-else>❓</span>
          </div>

          <!-- 타이틀 -->
          <h3 class="confirm-title">{{ title }}</h3>

          <!-- 메시지 -->
          <p class="confirm-message">{{ message }}</p>

          <!-- 버튼 -->
          <div class="confirm-buttons">
            <button 
              class="btn btn-ghost confirm-cancel" 
              @click="handleCancel"
            >
              {{ cancelText }}
            </button>
            <button 
              class="btn confirm-ok" 
              :class="confirmButtonClass"
              @click="handleConfirm"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '확인'
  },
  message: {
    type: String,
    default: '정말 진행하시겠습니까?'
  },
  type: {
    type: String,
    default: 'warning', // warning, danger, info, question
    validator: (value) => ['warning', 'danger', 'info', 'question'].includes(value)
  },
  confirmText: {
    type: String,
    default: '확인'
  },
  cancelText: {
    type: String,
    default: '취소'
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

const isVisible = ref(false)

watch(() => props.show, (newVal) => {
  isVisible.value = newVal
})

const confirmButtonClass = computed(() => {
  switch (props.type) {
    case 'danger':
      return 'btn-danger'
    case 'warning':
      return 'btn-warning'
    default:
      return 'btn-primary'
  }
})

const handleConfirm = () => {
  isVisible.value = false
  emit('confirm')
  emit('close')
}

const handleCancel = () => {
  isVisible.value = false
  emit('cancel')
  emit('close')
}
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 20px;
}

.confirm-modal {
  background: white;
  border-radius: 25px;
  padding: 35px 30px 30px;
  max-width: 400px;
  width: 100%;
  text-align: center;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.2);
  animation: modal-pop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes modal-pop {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.confirm-icon {
  font-size: 3.5rem;
  margin-bottom: 15px;
  line-height: 1;
}

.confirm-icon.warning span {
  filter: drop-shadow(0 4px 15px rgba(255, 193, 7, 0.4));
}

.confirm-icon.danger span {
  filter: drop-shadow(0 4px 15px rgba(220, 53, 69, 0.4));
}

.confirm-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #2D3436;
  margin-bottom: 12px;
}

.confirm-message {
  font-size: 1rem;
  color: #636E72;
  line-height: 1.6;
  margin-bottom: 25px;
}

.confirm-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-buttons .btn {
  flex: 1;
  max-width: 140px;
  padding: 12px 20px;
  font-weight: 600;
  border-radius: 15px;
}

.confirm-cancel {
  background: #f1f3f5 !important;
  color: #636E72 !important;
  border: none !important;
}

.confirm-cancel:hover {
  background: #e9ecef !important;
}

.btn-warning {
  background: linear-gradient(135deg, #E85D75, #FF6B9D) !important;
  color: white !important;
}

.btn-danger {
  background: linear-gradient(135deg, #dc3545, #ff6b6b) !important;
  color: white !important;
}

/* Transition */
.modal-enter-active {
  transition: opacity 0.2s ease;
}

.modal-leave-active {
  transition: opacity 0.15s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .confirm-modal,
.modal-leave-to .confirm-modal {
  transform: scale(0.9);
}

/* Mobile */
@media (max-width: 480px) {
  .confirm-modal {
    padding: 30px 25px 25px;
    border-radius: 20px;
  }

  .confirm-icon {
    font-size: 3rem;
  }

  .confirm-title {
    font-size: 1.2rem;
  }

  .confirm-message {
    font-size: 0.95rem;
  }

  .confirm-buttons {
    flex-direction: column-reverse;
    gap: 10px;
  }

  .confirm-buttons .btn {
    max-width: none;
    width: 100%;
  }
}
</style>
