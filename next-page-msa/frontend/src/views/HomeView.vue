<template>
  <div>
    <!-- Floating Atmosphere -->
    <div class="shape shape-1"></div>
    <div class="shape shape-2"></div>

    <div class="text-center mb-4" style="padding: 60px 0;">
      <h1 class="hero-title floating text-glow">
        우리가 함께 만드는 이야기
      </h1>
      <p style="font-size: 1.2rem; color: var(--text-muted); max-width: 600px; margin: 0 auto; line-height: 1.8;">
        당신의 한 문장이 베스트셀러의 시작이 됩니다.<br>
        지금 바로 릴레이 소설에 참여해보세요.
      </p>
      <div class="mt-4" id="hero-cta">
        <div v-if="!authStore.isAuthenticated" class="guest-only" style="display: flex; justify-content: center; gap: 15px;">
          <button @click="authStore.openLogin" class="btn btn-outline"
            style="font-size: 1.1rem; padding: 12px 30px;">로그인</button>
          <button @click="authStore.openSignup" class="btn btn-primary"
            style="font-size: 1.1rem; padding: 12px 30px;">회원가입</button>
        </div>
        <div v-else class="user-only" style="display: flex; justify-content: center;">
          <router-link to="/books/new" class="btn btn-primary" style="font-size: 1.1rem; padding: 12px 30px;">
            이야기 시작하기
          </router-link>
        </div>
      </div>
    </div>

    <!-- Filter & Search -->
    <div class="card search-card" ref="searchContainer">
      <div class="filter-group">
        <select v-model="filters.categoryId" class="form-control filter-select">
          <option value="">전체 카테고리</option>
          <option v-for="cat in categories" :key="cat.categoryId" :value="cat.categoryId">
            {{ cat.categoryName }}
          </option>
        </select>
        <select v-model="filters.status" class="form-control filter-select">
          <option value="">모든 상태</option>
          <option value="WRITING">연재중</option>
          <option value="COMPLETED">완결</option>
        </select>
      </div>
      <div class="search-group">
        <input type="text" v-model="filters.keyword" class="form-control search-input" placeholder="제목/작가 검색..." 
          @input="debouncedSearch" @keyup.enter="searchBooks">
        <button class="btn btn-primary search-btn" @click="searchBooks">
          검색
        </button>
      </div>
    </div>

    <!-- Book Grid -->
    <div class="grid" id="book-list" ref="resultsContainer">
      <div v-for="book in books" :key="book.bookId" class="card" @click="goDetail(book.bookId)"
        style="cursor: pointer; animation: slideInFromBottom 0.5s ease-out;">
        <div class="book-cover-placeholder">
          <span class="book-icon">{{ getIcon(book.categoryId) }}</span>
        </div>
        <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
          <span class="badge" :class="book.status === 'WRITING' ? 'badge-writing' : 'badge-completed'">
            {{ book.status }}
          </span>
          <span style="font-size: 0.8rem; color: var(--text-muted); text-transform:uppercase; letter-spacing:1px;">
            {{ getCategoryName(book.categoryId) }}
          </span>
        </div>
        <h3 style="margin-bottom: 10px; font-size: 1.4rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
          {{ book.title }}
        </h3>
        <p style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 20px;">
          <span style="color: var(--primary-color);">Make by.</span> {{ book.writerNicknm || '작가님' }}
        </p>
        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid rgba(255,255,255,0.1); padding-top: 15px;">
          <span style="font-size: 0.85rem; color: var(--text-muted);">
            👥 {{ book.participantCount || 1 }}명 참여
          </span>
          <span style="font-size: 0.85rem; color: var(--text-muted);">
            📝 {{ book.currentSequence }} 문장
          </span>
        </div>
      </div>

      <!-- Sentinel -->
      <div ref="sentinel" style="height: 20px; grid-column: 1/-1;">
        <div v-if="loading" class="text-center" style="padding: 20px; color: var(--text-muted);">
          Loading...
        </div>
      </div>

      <div v-if="books.length === 0 && !loading" class="card"
        style="grid-column: 1/-1; padding: 60px 20px; text-align: center;">
        <div style="font-size: 4rem; margin-bottom: 20px;">📚</div>
        <h3 style="color: var(--text-muted); margin-bottom: 10px;">등록된 소설이 없습니다.</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem;">다른 검색어를 시도하거나 새로운 이야기를 시작해보세요!</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()

const books = ref([])
const categories = ref([])
const categoryMap = reactive({})
const filters = reactive({
  categoryId: '',
  status: '',
  keyword: ''
})
const page = ref(0)
const size = 20
const loading = ref(false)
const hasNext = ref(true)
const sentinel = ref(null)
const searchContainer = ref(null)

// Category Fetch
const fetchCategories = async () => {
  try {
    const res = await axios.get('/categories')
    categories.value = res.data.data
    categories.value.forEach(cat => {
      categoryMap[cat.categoryId] = cat.categoryName
    })
  } catch (e) {
    console.error(e)
  }
}

// Book Load
const loadBooks = async (isAppend = false) => {
  if (loading.value || (!hasNext.value && isAppend)) return
  loading.value = true
  
  try {
    const res = await axios.get('/books', {
      params: {
        categoryId: filters.categoryId,
        status: filters.status,
        keyword: filters.keyword,
        page: page.value,
        size: size
      }
    })
    
    const data = res.data.data
    if (isAppend) {
      books.value = [...books.value, ...data.content]
    } else {
      books.value = data.content
    }
    hasNext.value = !data.last
    if (hasNext.value) page.value++
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetAndLoad = () => {
  page.value = 0
  hasNext.value = true
  books.value = []
  loadBooks()
}

// Watchers
watch(() => filters.categoryId, resetAndLoad)
watch(() => filters.status, resetAndLoad)

let searchTimeout = null
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(resetAndLoad, 500)
}

const searchBooks = () => {
  clearTimeout(searchTimeout)
  resetAndLoad().then(() => {
    // Scroll to Search Container
    if (searchContainer.value) {
      searchContainer.value.scrollIntoView({ block: 'start', behavior: 'smooth' })
    }
  })
}

// Navigation
const goDetail = (bookId) => {
  router.push(`/books/${bookId}`)
}

// Helpers
const getCategoryName = (id) => categoryMap[id] || id || '미분류'
const getIcon = (catId) => {
  const name = categoryMap[catId] || ''
  if (catId === 'ROMANCE' || name.includes('로맨스')) return '💖'
  if (catId === 'THRILLER' || name.includes('스릴러')) return '🔪'
  if (catId === 'FANTASY' || name.includes('판타지')) return '🏰'
  if (catId === 'SF' || name.includes('SF')) return '👽'
  if (catId === 'MYSTERY' || name.includes('미스터리')) return '🕵️'
  if (catId === 'DAILY' || name.includes('일상')) return '☕'
  return '📖'
}

// Observer
let observer = null
onMounted(() => {
  fetchCategories()
  loadBooks()
  
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      loadBooks(true)
    }
  }, { threshold: 0.1 })
  
  if (sentinel.value) observer.observe(sentinel.value)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* Search Card Layout */
.search-card {
  display: flex;
  gap: 15px;
  align-items: center;
  z-index: 10;
  position: relative;
  margin-bottom: 30px;
  scroll-margin-top: 20px; /* Ensures scroll stops with padding at top */
}

.filter-group {
  display: flex;
  gap: 15px;
}

.filter-select {
  width: auto;
  min-width: 120px;
}

.search-group {
  flex: 1;
  display: flex;
  gap: 10px;
  min-width: 250px;
}

.search-btn {
  padding: 12px 20px;
  border-radius: 15px;
  white-space: nowrap;
}

/* Mobile Layout Adjustment */
@media (max-width: 768px) {
  .search-card {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .filter-group {
    display: grid;
    grid-template-columns: 1fr 1fr; /* 50% 50% Split */
    gap: 10px;
    width: 100%;
  }

  .filter-select {
    width: 100%;
    min-width: 0;
  }

  .search-group {
    width: 100%;
    min-width: 0;
  }
}
</style>
