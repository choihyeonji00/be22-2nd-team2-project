<template>
  <div class="card fade-in" style="max-width: 900px; margin: 0 auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="margin:0;">내 서재</h2>
      <button class="btn btn-outline" style="border-color: #ef4444; color: #ef4444; font-size: 0.85rem;"
        @click="withdraw">회원 탈퇴</button>
    </div>

    <div id="profile-section"
      style="margin-bottom: 30px; padding: 15px; background: rgba(255,255,255,0.03); border-radius: 12px;">
      <div v-if="user" id="profile-info" style="color: var(--text-muted); font-size: 0.95rem;">
        <span style="margin-right: 15px;"><strong>{{ user.userNicknm }}</strong>님</span>
        <span style="margin-right: 15px;">{{ user.userEmail }}</span>
        <span class="badge badge-writing">{{ user.userRole }}</span>
      </div>
      <div v-else>Loading...</div>
    </div>

    <!-- Stats Section -->
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-icon">📚</div>
        <div class="stat-content">
          <span class="stat-value">{{ user?.createdBookCount || 0 }}</span>
          <span class="stat-label">만든 소설</span>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">✍️</div>
        <div class="stat-content">
          <span class="stat-value">{{ user?.writtenSentenceCount || 0 }}</span>
          <span class="stat-label">작성한 문장</span>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">💬</div>
        <div class="stat-content">
          <span class="stat-value">{{ user?.writtenCommentCount || 0 }}</span>
          <span class="stat-label">작성한 댓글</span>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs"
      style="display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px;">
      <button class="btn btn-ghost" :class="{ active: currentTab === 'books' }" @click="switchTab('books')">내가 만든 소설</button>
      <button class="btn btn-ghost" :class="{ active: currentTab === 'sentences' }" @click="switchTab('sentences')">내가 쓴
        문장</button>
      <button class="btn btn-ghost" :class="{ active: currentTab === 'comments' }" @click="switchTab('comments')">내가 쓴
        댓글</button>
    </div>

    <!-- List Container -->
    <div id="list-container" style="min-height: 200px;">
      <div v-if="loadingList" class="text-center p-4">Loading...</div>
      
      <div v-else-if="items.length === 0" class="text-center p-4">
          데이터가 없습니다.
      </div>

      <div v-else>
          <!-- Books -->
          <template v-if="currentTab === 'books'">
             <div v-for="book in items" :key="book.bookId" class="card" @click="goBook(book.bookId)" 
                style="cursor: pointer; padding: 15px; margin-bottom: 10px; display: flex; align-items: center; justify-content: space-between;">
                <div>
                   <h4 style="margin: 0 0 5px 0;">{{ book.title }}</h4>
                   <span style="font-size: 0.8rem; color: var(--text-muted);">{{ new Date(book.createdAt).toLocaleDateString() }}</span>
                   <span class="badge" :class="book.status === 'WRITING' ? 'badge-writing' : 'badge-completed'" style="margin-left: 10px; font-size: 0.7rem;">{{ book.status }}</span>
                </div>
                <div style="text-align: right;">
                   <div style="font-size: 0.8rem; color: var(--text-muted);">문장 {{ book.currentSequence }} / {{ book.maxSequence }}</div>
                </div>
             </div>
          </template>

          <!-- Sentences -->
          <template v-if="currentTab === 'sentences'">
              <div v-for="sent in items" :key="sent.sentenceId" class="card" @click="goBook(sent.bookId)"
                style="padding: 15px; margin-bottom: 10px; border-left: 3px solid var(--secondary-color); cursor: pointer;">
                  <div v-if="sent.bookTitle" style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 5px;">
                      <span style="color: var(--primary-color);">[{{ sent.bookTitle }}]</span> 에 쓴 문장
                  </div>
                  <p style="margin: 0 0 5px 0; font-size: 1rem;">{{ sent.content }}</p>
                  <div style="font-size: 0.8rem; color: var(--text-muted); display: flex; justify-content: space-between;">
                      <span>{{ new Date(sent.createdAt).toLocaleString() }}</span>
                      <div>
                         <span style="margin-right: 10px;">👍 {{ sent.likeCount }}</span>
                         <span>👎 {{ sent.dislikeCount }}</span>
                      </div>
                  </div>
              </div>
          </template>

          <!-- Comments -->
          <template v-if="currentTab === 'comments'">
               <div v-for="c in items" :key="c.commentId" class="card" @click="goBook(c.bookId)"
                 style="cursor: pointer; padding: 15px; margin-bottom: 10px; border-left: 3px solid var(--accent-color);">
                  <div style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 5px;">
                      <span style="color: var(--primary-color);">[{{ c.bookTitle || '소설' }}]</span> 에 남긴 댓글
                  </div>
                  <p style="margin: 0 0 5px 0;">{{ c.content }}</p>
                  <div style="font-size: 0.8rem; color: var(--text-muted);">
                      {{ new Date(c.createdAt).toLocaleString() }}
                  </div>
               </div>
          </template>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" id="pagination" style="display: flex; justify-content: center; gap: 10px; margin-top: 20px;">
        <button class="btn btn-outline" :disabled="page === 0" @click="changePage(page - 1)" style="padding: 5px 15px;">이전</button>
        <span style="align-self: center; color: var(--text-muted); font-size: 0.9rem;">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="btn btn-outline" :disabled="page >= totalPages - 1" @click="changePage(page + 1)" style="padding: 5px 15px;">다음</button>
    </div>

    <!-- 확인 모달 -->
    <ConfirmModal 
      :show="showConfirmModal"
      :title="confirmModalConfig.title"
      :message="confirmModalConfig.message"
      :type="confirmModalConfig.type"
      :confirmText="confirmModalConfig.confirmText"
      :cancelText="confirmModalConfig.cancelText"
      @confirm="handleConfirmModalConfirm"
      @close="showConfirmModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'
import { toast } from '@/utils/toast'
import ConfirmModal from '@/components/ConfirmModal.vue'

const router = useRouter()
const authStore = useAuthStore()

const user = ref(null)
const currentTab = ref('books')
const items = ref([])
const page = ref(0)
const totalPages = ref(0)
const loadingList = ref(false)

// 확인 모달 상태
const showConfirmModal = ref(false)
const confirmModalConfig = ref({
  title: '',
  message: '',
  type: 'warning',
  confirmText: '확인',
  cancelText: '취소',
  onConfirm: null
})

const handleConfirmModalConfirm = () => {
  if (confirmModalConfig.value.onConfirm) {
    confirmModalConfig.value.onConfirm()
  }
  showConfirmModal.value = false
}

onMounted(async () => {
    if (!authStore.isAuthenticated) {
        toast.warning('로그인이 필요합니다.')
        router.push('/')
        return
    }
    await loadUserProfile()
    loadList()
})

const loadUserProfile = async () => {
    try {
        const res = await axios.get('/members/me')
        user.value = res.data.data
    } catch(e) { console.error(e) }
}

const switchTab = (tab) => {
    currentTab.value = tab
    page.value = 0
    loadList()
}

const changePage = (newPage) => {
    page.value = newPage
    loadList()
}

const loadList = async () => {
    loadingList.value = true
    try {
        let url = ''
        let params = { page: page.value, size: 10 }
        
        if (currentTab.value === 'books') {
            url = '/books'
            params.writerId = user.value?.userId 
        } else if (currentTab.value === 'sentences') {
            url = '/books/mysentences'
        } else if (currentTab.value === 'comments') {
            url = '/reactions/mycomments'
        }

        const res = await axios.get(url, { params })
        items.value = res.data.data.content
        totalPages.value = res.data.data.totalPages
    } catch(e) {
        console.error(e)
    } finally {
        loadingList.value = false
    }
}

const goBook = (id) => {
    if (id) router.push(`/books/${id}`)
}

const withdraw = () => {
    confirmModalConfig.value = {
        title: '회원 탈퇴',
        message: '정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.',
        type: 'danger',
        confirmText: '탈퇴하기',
        cancelText: '취소',
        onConfirm: async () => {
            try {
                await axios.delete('/auth/withdraw')
                authStore.logout()
                toast.success('탈퇴되었습니다. 이용해 주셔서 감사합니다.')
                router.push('/')
            } catch(e) { toast.error('탈퇴 처리에 실패했습니다.') }
        }
    }
    showConfirmModal.value = true
}
</script>

<style scoped>
.tabs .btn.active {
    border-bottom: 2px solid var(--primary-color);
    color: var(--primary-color);
    font-weight: 700;
}

/* Stats Grid */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 40px;
}

.stat-item {
    background: #fff;
    border: 2px solid rgba(0,0,0,0.05);
    border-radius: 20px;
    padding: 25px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    transition: all 0.3s ease;
    box-shadow: 0 5px 15px rgba(0,0,0,0.03);
}

.stat-item:hover {
    transform: translateY(-5px);
    border-color: var(--primary-color);
    box-shadow: 0 10px 25px rgba(232, 93, 117, 0.15);
}

.stat-icon {
    font-size: 2.5rem;
    margin-bottom: 5px;
    background: #FFF5F8;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.stat-content {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.stat-value {
    font-size: 1.8rem;
    font-weight: 800;
    color: var(--text-color);
    line-height: 1;
    margin-bottom: 5px;
}

.stat-label {
    font-size: 0.9rem;
    color: var(--text-muted);
    font-weight: 600;
}

@media (max-width: 600px) {
    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    /* Make the last item span full width on small screens if 3 items */
    .stat-item:last-child {
        grid-column: span 2; 
    }
    .stat-item:last-child:nth-child(3n) {
         grid-column: span 1; /* Reset if grid logic changes */
    }
    /* Actually for 3 items on 2 col grid: 1, 2 on first row, 3 spans full on second row */
    .stats-grid > .stat-item:nth-child(3) {
         grid-column: 1 / -1;
    }
}
</style>
