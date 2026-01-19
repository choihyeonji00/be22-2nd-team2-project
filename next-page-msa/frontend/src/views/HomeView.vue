<template>
  <div :class="{ 'search-mode': isSearchMode }">
    <!-- Floating Atmosphere -->
    <div class="shape shape-1"></div>
    <div class="shape shape-2"></div>

    <!-- 히어로 섹션 - 검색 모드일 때 축소 -->
    <div class="text-center hero-section" :class="{ 'hero-collapsed': isSearchMode }">
      <h1 class="hero-title floating text-glow">
        우리가 함께 만드는 이야기
      </h1>
      <p class="hero-subtitle" v-show="!isSearchMode">
        당신의 한 문장이 베스트셀러의 시작이 됩니다.<br>
        지금 바로 릴레이 소설에 참여해보세요.
      </p>
      <div class="hero-cta" id="hero-cta" v-show="!isSearchMode">
        <div v-if="!authStore.isAuthenticated" class="cta-buttons">
          <button @click="authStore.openLogin" class="btn btn-outline">로그인</button>
          <button @click="authStore.openSignup" class="btn btn-primary">회원가입</button>
        </div>
        <div v-else class="cta-buttons">
          <router-link to="/books/new" class="btn btn-primary">
            이야기 시작하기
          </router-link>
        </div>
      </div>
    </div>

    <!-- Filter & Search - 검색 모드일 때 상단 sticky -->
    <div class="card search-card" :class="{ 'search-card-sticky': isSearchMode }" ref="searchContainer">
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
          @input="debouncedSearch" @keyup.enter="searchBooks" @focus="onSearchFocus">
        <button class="btn btn-primary search-btn" @click="searchBooks">
          검색
        </button>
        <button v-if="isSearchMode" class="btn btn-ghost clear-btn" @click="clearSearch" title="검색 초기화">
          ✕
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
          <div style="display: flex; gap: 10px;">
            <span style="font-size: 0.85rem; color: var(--text-muted);">
              👥 {{ book.participantCount || 1 }}명
            </span>
            <span style="font-size: 0.85rem; color: var(--text-muted);">
              📝 {{ book.currentSequence }}
            </span>
          </div>
          <div style="display: flex; gap: 8px;">
            <span style="font-size: 0.85rem; color: var(--primary-color);">
              👍 {{ book.likeCount || 0 }}
            </span>
            <span style="font-size: 0.85rem; color: var(--text-muted);">
              👎 {{ book.dislikeCount || 0 }}
            </span>
          </div>
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
const resultsContainer = ref(null)

// 검색 모드 상태
const isSearchMode = ref(false)

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
  return loadBooks()
}

// 검색 모드 상태 업데이트 함수
const updateSearchMode = () => {
  isSearchMode.value = filters.keyword.trim().length > 0 || filters.categoryId !== '' || filters.status !== ''
}

// 필터 변경 시 검색 모드 적용 및 스크롤
// 스크롤 함수 (통일된 동작)
const scrollToResults = () => {
  if (isSearchMode.value && searchContainer.value) {
    setTimeout(() => {
      window.scrollTo({
        top: searchContainer.value.offsetTop - 100,
        behavior: 'smooth'
      })
    }, 100)
  }
}

// 필터 변경 시 검색 모드 적용 및 스크롤
const handleFilterChange = async () => {
  updateSearchMode()
  await resetAndLoad()
  scrollToResults()
}

// Watchers - 드롭다운 변경 시에도 동일하게 적용
watch(() => filters.categoryId, handleFilterChange)
watch(() => filters.status, handleFilterChange)

let searchTimeout = null
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    isSearchMode.value = filters.keyword.trim().length > 0 || filters.categoryId !== '' || filters.status !== ''
    await resetAndLoad()
    scrollToResults()
  }, 500)
}

const onSearchFocus = () => {
  // 검색 입력창에 포커스하면 검색 모드 활성화 준비
}

const searchBooks = async () => {
  clearTimeout(searchTimeout)
  isSearchMode.value = filters.keyword.trim().length > 0 || filters.categoryId !== '' || filters.status !== ''
  await resetAndLoad()
  scrollToResults()
}

const clearSearch = () => {
  filters.keyword = ''
  filters.categoryId = ''
  filters.status = ''
  isSearchMode.value = false
  resetAndLoad()
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
/* Hero Section */
.hero-section {
  padding: 40px 0 30px;
  transition: all 0.3s ease;
}

/* 검색 모드일 때 히어로 축소 */
.hero-collapsed {
  padding: 15px 0 10px;
}

.hero-collapsed .hero-title {
  font-size: 1.8rem;
  animation: none;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: var(--text-muted);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.7;
}

.hero-cta {
  margin-top: 25px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.cta-buttons .btn {
  font-size: 1rem;
  padding: 10px 25px;
}

/* Search Card Layout */
.search-card {
  display: flex;
  gap: 15px;
  align-items: center;
  z-index: 50;
  position: relative;
  margin-bottom: 25px;
  scroll-margin-top: 80px;
  transition: all 0.3s ease;
}

/* 검색 모드일 때 sticky */
.search-card-sticky {
  position: sticky;
  top: 70px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(232, 93, 117, 0.15);
  border-color: var(--primary-color);
  z-index: 100;
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
  align-items: center;
}

.search-btn {
  padding: 12px 20px;
  border-radius: 15px;
  white-space: nowrap;
}

.clear-btn {
  padding: 8px 12px !important;
  min-width: auto !important;
  width: auto !important;
  color: var(--text-muted);
  font-size: 1rem;
}

.clear-btn:hover {
  color: var(--primary-color);
  background: rgba(232, 93, 117, 0.1);
}

/* 검색 결과 카운트 표시 */
.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 0;
}

.results-count {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.results-count strong {
  color: var(--primary-color);
}

/* Mobile Layout */
@media (max-width: 768px) {
  .hero-section {
    padding: 25px 0 20px;
  }

  .hero-collapsed {
    padding: 10px 0 8px;
  }

  .hero-collapsed .hero-title {
    font-size: 1.4rem;
  }

  .hero-subtitle {
    font-size: 0.95rem;
    padding: 0 10px;
  }

  .hero-cta {
    margin-top: 20px;
  }

  .cta-buttons .btn {
    font-size: 0.9rem;
    padding: 10px 20px;
  }

  .search-card {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
    align-items: stretch;
    padding: 15px;
    margin-bottom: 20px;
  }

  .search-card-sticky {
    top: 60px;
    margin: 0 -12px;
    border-radius: 0;
    padding: 12px;
  }

  .filter-group {
    display: contents;
  }

  .filter-select {
    width: 100%;
    min-width: 0;
  }

  .search-group {
    grid-column: 1 / -1;
    display: flex;
    gap: 8px;
    width: 100%;
  }

  .search-input {
    flex: 1;
    min-width: 0;
  }
  
  .search-btn {
    flex-shrink: 0;
    width: auto;
    padding: 0 20px;
  }

  .clear-btn {
    padding: 8px !important;
  }
}

/* Small Mobile */
@media (max-width: 480px) {
  .hero-section {
    padding: 20px 0 15px;
  }

  .hero-collapsed {
    padding: 8px 0 5px;
  }

  .hero-collapsed .hero-title {
    font-size: 1.2rem;
  }

  .hero-subtitle {
    font-size: 0.9rem;
  }

  .cta-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .cta-buttons .btn {
    width: 100%;
  }

  .search-card-sticky {
    top: 55px;
  }
}
</style>

