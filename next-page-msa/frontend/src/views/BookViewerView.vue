<template>
  <div class="book-viewer-container" ref="viewerContainer">
    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>책을 펼치는 중입니다...</p>
    </div>

    <!-- Book Content -->
    <template v-else>
      <!-- Toolbar (Fixed) -->
      <div class="viewer-toolbar" :class="{ 'toolbar-hidden': isToolbarHidden }">
        <button @click="goBack" class="btn-toolbar">
          ← 돌아가기
        </button>
        <span class="book-title-mini">{{ book.title }}</span>
        <div class="toolbar-controls">
          <button @click="decreaseFontSize" class="btn-toolbar" title="글자 작게">A-</button>
          <button @click="increaseFontSize" class="btn-toolbar" title="글자 크게">A+</button>
          <button @click="printBook" class="btn-toolbar" title="인쇄">🖨️</button>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="book-content-area">
        <!-- 표지 페이지 (Spread 0) -->
        <transition name="page-flip" mode="out-in">
          <div v-if="currentSpread === 0" class="book-cover-wrapper" key="cover">
            <div class="book-cover-front" @click="nextSpread">
              <div class="cover-design">
                <div class="cover-content">
                  <div class="cover-ornament-top">✦</div>
                  <h1 class="cover-title">{{ book.title }}</h1>
                  <p class="cover-author">참여 작가 {{ writerCount }}인</p>
                  <div class="cover-ornament">
                    <span class="icon">❦</span>
                  </div>
                  <button class="btn-open-book">책 펼치기</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 본문 페이지 (Spread 1+) -->
          <div v-else class="book-spread" key="spread">
            <!-- 좌측 페이지 (데스크탑에서만) -->
            <div class="book-page left-page">
              <!-- Spread 1: 타이틀 페이지 -->
              <div v-if="currentSpread === 1" class="page-content decoration-page">
                <div class="book-cover-summary">
                  <h2>{{ book.title }}</h2>
                  <p class="author-info">Made by. Team2</p>
                  <div class="ornament">❦</div>
                  <div class="mini-toc">
                    <p>이야기의 시작</p>
                  </div>
                </div>
              </div>
              <!-- 일반 컨텐츠 페이지 -->
              <div v-else class="page-content" :style="{ fontSize: fontSize + 'px' }">
                <div class="story-body">
                  <p v-for="sent in leftPageSentences" :key="sent.sentenceId" class="story-paragraph">
                    {{ sent.content }}
                  </p>
                </div>
                <!-- Fin. 왼쪽 페이지에 표시 (오른쪽이 비어있을 때) -->
                <div v-if="isLastContentSpread && leftPageSentences.length > 0 && rightPageSentences.length === 0" class="the-end">
                  <span class="fin">Fin.</span>
                </div>
              </div>
              <div class="page-number" v-if="getLeftPageNumber">- {{ getLeftPageNumber }} -</div>
            </div>

            <!-- 우측 페이지 -->
            <div class="book-page right-page" ref="contentPage">
              <div class="page-content" :style="{ fontSize: fontSize + 'px' }">
                <h2 v-if="currentSpread === 1" class="chapter-title">Content</h2>
                
                <div class="story-body">
                  <p v-for="sent in rightPageSentences" :key="sent.sentenceId" class="story-paragraph">
                    {{ sent.content }}
                  </p>
                </div>

                <!-- Fin. 오른쪽 페이지에 표시 (오른쪽에 콘텐츠가 있을 때만) -->
                <div v-if="isLastContentSpread && rightPageSentences.length > 0" class="the-end">
                  <span class="fin">Fin.</span>
                </div>
              </div>
              <div class="page-number" v-if="getRightPageNumber">- {{ getRightPageNumber }} -</div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 하단 고정 네비게이션 -->
      <div class="viewer-bottom-nav" v-if="currentSpread > 0 || totalSpreads > 0">
        <button 
          class="nav-btn prev" 
          @click="prevSpread" 
          :disabled="currentSpread === 0"
        >
          ◀
        </button>
        <div class="page-indicator">
          <div class="page-progress">
            <div class="progress-bar" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <span class="page-text">{{ currentSpread }} / {{ totalSpreads }}</span>
        </div>
        <button 
          class="nav-btn next" 
          @click="nextSpread" 
          :disabled="currentSpread === totalSpreads"
        >
          ▶
        </button>
      </div>

      <!-- 키보드 힌트 (데스크탑) -->
      <div class="keyboard-hint">
        <kbd>←</kbd> <kbd>→</kbd> 페이지 이동
      </div>
    </template>

    <!-- 인쇄용 콘텐츠 (template 밖에 배치하여 항상 DOM에 존재) -->
    <div class="print-content" v-if="!loading">
      <div class="print-header">
        <h1 class="print-title">{{ book.title }}</h1>
        <p class="print-author">참여 작가 {{ writerCount }}인</p>
        <div class="print-meta">
          <span v-if="book.status === 'COMPLETED'">📚 완결</span>
          <span v-else>✍️ 연재중</span>
          <span>총 {{ sortedSentences.length }}문장</span>
        </div>
        <div class="print-divider"></div>
      </div>
      <div class="print-body">
        <p v-for="sent in sortedSentences" :key="sent.sentenceId" class="print-paragraph">
          {{ sent.content }}
        </p>
      </div>
      <div class="print-footer">
        <div class="print-fin">~ Fin. ~</div>
        <p class="print-copyright">© 2026 Next Page - Team2</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const bookId = route.params.id

const loading = ref(true)
const book = ref({})
const sentences = ref([])
const fontSize = ref(17)
const viewerContainer = ref(null)

// Pagination (Spread based)
const currentSpread = ref(0)
const itemsPerPage = 10 // 한 페이지에 보여줄 문장 수

const sortedSentences = computed(() => {
  return sentences.value ? [...sentences.value].sort((a, b) => a.sequenceNo - b.sequenceNo) : []
})

const totalContentPages = computed(() => {
  if (sortedSentences.value.length === 0) return 0
  return Math.ceil(sortedSentences.value.length / itemsPerPage)
})

// 총 스프레드 수 계산 - 빈 페이지 방지
const totalSpreads = computed(() => {
  const total = sortedSentences.value.length
  if (total === 0) return 1 // 표지만
  
  const totalPages = totalContentPages.value
  if (totalPages === 0) return 1
  
  // Spread 0: 표지
  // Spread 1: 우측에 1페이지 (pageIndex 0)
  // Spread 2: 좌측에 2페이지(index 1), 우측에 3페이지(index 2)
  // Spread 3: 좌측에 4페이지(index 3), 우측에 5페이지(index 4)
  // ...
  
  // 마지막 페이지 인덱스 (0부터 시작)
  const lastPageIndex = totalPages - 1
  
  if (lastPageIndex === 0) {
    // 1페이지만 있으면 Spread 1까지
    return 1
  }
  
  // 마지막 페이지가 어느 스프레드에 있는지 계산
  // pageIndex 0 -> Spread 1
  // pageIndex 1, 2 -> Spread 2
  // pageIndex 3, 4 -> Spread 3
  // 공식: spread = 1 + ceil(lastPageIndex / 2)
  const lastSpread = 1 + Math.ceil(lastPageIndex / 2)
  
  return lastSpread
})

const progressPercent = computed(() => {
  if (totalSpreads.value <= 1) return 100
  return Math.round((currentSpread.value / totalSpreads.value) * 100)
})

// 좌측 페이지 문장 (Spread 2부터)
const leftPageSentences = computed(() => {
  if (currentSpread.value <= 1) return []
  
  // Spread 2: 좌측 = pageIndex 1 (2번째 페이지)
  // Spread 3: 좌측 = pageIndex 3 (4번째 페이지)
  // Spread 4: 좌측 = pageIndex 5 (6번째 페이지)
  // 공식: pageIndex = (currentSpread - 1) * 2 - 1
  const pageIndex = (currentSpread.value - 1) * 2 - 1
  
  // 페이지가 총 페이지 수를 초과하면 빈 배열
  if (pageIndex >= totalContentPages.value || pageIndex < 0) return []
  
  const start = pageIndex * itemsPerPage
  const end = start + itemsPerPage
  return sortedSentences.value.slice(start, end)
})

// 우측 페이지 문장
const rightPageSentences = computed(() => {
  if (currentSpread.value === 0) return []
  
  let pageIndex
  if (currentSpread.value === 1) {
    // Spread 1: 우측 = pageIndex 0 (1번째 페이지)
    pageIndex = 0
  } else {
    // Spread 2: 우측 = pageIndex 2 (3번째 페이지)
    // Spread 3: 우측 = pageIndex 4 (5번째 페이지)
    // 공식: pageIndex = (currentSpread - 1) * 2
    pageIndex = (currentSpread.value - 1) * 2
  }
  
  // 페이지가 총 페이지 수를 초과하면 빈 배열
  if (pageIndex >= totalContentPages.value) return []
  
  const start = pageIndex * itemsPerPage
  const end = start + itemsPerPage
  return sortedSentences.value.slice(start, end)
})

// 현재 스프레드에 콘텐츠가 있는지 확인
const hasContent = computed(() => {
  if (currentSpread.value === 0) return true // 표지
  return leftPageSentences.value.length > 0 || rightPageSentences.value.length > 0
})

// 마지막 콘텐츠 스프레드인지 확인 (Fin. 표시용)
const isLastContentSpread = computed(() => {
  if (currentSpread.value === 0) return false
  
  // 현재 스프레드에서 보여주는 마지막 문장의 인덱스 계산
  const rightPageIndex = currentSpread.value === 1 ? 0 : 2 + (currentSpread.value - 2) * 2
  const lastSentenceIndexInSpread = (rightPageIndex + 1) * itemsPerPage - 1
  
  // 마지막 문장이 이 스프레드에 있으면 true
  return lastSentenceIndexInSpread >= sortedSentences.value.length - 1
})

const getLeftPageNumber = computed(() => {
  if (currentSpread.value <= 1) return null
  if (leftPageSentences.value.length === 0) return null
  const pageNum = 1 + (currentSpread.value - 2) * 2 + 1
  return pageNum <= totalContentPages.value ? pageNum : null
})

const getRightPageNumber = computed(() => {
  if (currentSpread.value === 0) return null
  if (rightPageSentences.value.length === 0) return null
  const pageNum = currentSpread.value === 1 ? 1 : 2 + (currentSpread.value - 2) * 2 + 1
  return pageNum <= totalContentPages.value ? pageNum : null
})

const isNewChapter = computed(() => {
  return currentSpread.value > 1 && (currentSpread.value - 1) % 5 === 0
})

const currentChapter = computed(() => {
  return Math.ceil((currentSpread.value - 1) / 5) + 1
})

const writerCount = computed(() => {
  return sentences.value ? new Set(sentences.value.map(s => s.writerId)).size : 0
})

const goBack = () => router.back()
const increaseFontSize = () => { if (fontSize.value < 24) fontSize.value += 2 }
const decreaseFontSize = () => { if (fontSize.value > 14) fontSize.value -= 2 }

const nextSpread = () => {
  if (currentSpread.value === 0) {
    currentSpread.value = 1
    return
  }
  if (currentSpread.value < totalSpreads.value) {
    currentSpread.value++
  }
}

const prevSpread = () => {
  if (currentSpread.value > 0) {
    currentSpread.value--
  }
}

// 인쇄 기능
const printBook = () => {
  window.print()
}

// 키보드 네비게이션
const handleKeydown = (e) => {
  if (e.key === 'ArrowRight' || e.key === ' ') {
    e.preventDefault()
    nextSpread()
  } else if (e.key === 'ArrowLeft') {
    e.preventDefault()
    prevSpread()
  }
}

// 스와이프 제스처
let touchStartX = 0
let touchEndX = 0

const handleTouchStart = (e) => {
  touchStartX = e.changedTouches[0].screenX
}

const handleTouchEnd = (e) => {
  touchEndX = e.changedTouches[0].screenX
  handleSwipe()
}

const handleSwipe = () => {
  const diff = touchStartX - touchEndX
  if (Math.abs(diff) > 50) {
    if (diff > 0) {
      nextSpread() // 왼쪽으로 스와이프 = 다음
    } else {
      prevSpread() // 오른쪽으로 스와이프 = 이전
    }
  }
}

onMounted(async () => {
  try {
    const res = await axios.get(`/books/${bookId}/view`)
    book.value = res.data.data
    sentences.value = book.value.sentences || []
  } catch (e) {
    toast.error('소설 내용을 불러올 수 없습니다.')
    loading.value = false
  } finally {
    loading.value = false
  }
  
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('scroll', handleScroll)
  
  // 스와이프 이벤트
  if (viewerContainer.value) {
    viewerContainer.value.addEventListener('touchstart', handleTouchStart, { passive: true })
    viewerContainer.value.addEventListener('touchend', handleTouchEnd, { passive: true })
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('scroll', handleScroll)
  
  if (viewerContainer.value) {
    viewerContainer.value.removeEventListener('touchstart', handleTouchStart)
    viewerContainer.value.removeEventListener('touchend', handleTouchEnd)
  }
})

const lastScrollY = ref(0)
const isToolbarHidden = ref(false)

const handleScroll = () => {
  if (window.innerWidth > 768) return
  const currentScrollY = window.scrollY
  if (currentScrollY > lastScrollY.value && currentScrollY > 50) {
    isToolbarHidden.value = true
  } else {
    isToolbarHidden.value = false
  }
  lastScrollY.value = currentScrollY
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@400;700;800&display=swap');

/* ========== Container - 풀스크린 활용 ========== */
.book-viewer-container {
  min-height: 100vh;
  height: 100vh;
  background: linear-gradient(135deg, #FFF5F8 0%, #FFF0F5 50%, #FFE5EC 100%);
  display: flex;
  flex-direction: column;
  font-family: 'Nanum Myeongjo', serif;
  overflow: hidden;
  position: relative;
}

/* Background Pattern */
.book-viewer-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='40' height='40' viewBox='0 0 40 40' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M20 20.5V18h2v2.5h2.5v2H22v2.5h-2V22.5h-2.5v-2H20z' fill='%23E85D75' fill-opacity='0.03' fill-rule='evenodd'/%3E%3C/svg%3E");
  z-index: 0;
  pointer-events: none;
}

/* ========== Loading ========== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: #2D3436;
  z-index: 1;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #FFD3E0;
  border-top-color: #E85D75;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== Toolbar (고정) ========== */
.viewer-toolbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0 15px;
  z-index: 100;
  border-bottom: 1px solid rgba(232, 93, 117, 0.1);
  transition: transform 0.3s ease;
}

.toolbar-hidden {
  transform: translateY(-100%);
}

.book-title-mini {
  font-weight: 700;
  font-size: 0.95rem;
  color: #2D3436;
  max-width: 40%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.toolbar-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-toolbar {
  background: transparent;
  border: 1px solid rgba(232, 93, 117, 0.2);
  color: #E85D75;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.85rem;
  transition: all 0.2s ease;
}

.btn-toolbar:hover {
  background: #E85D75;
  color: white;
}

/* ========== 메인 콘텐츠 영역 ========== */
.book-content-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 15px 80px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* ========== Cover Styles ========== */
.book-cover-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.book-cover-front {
  width: 100%;
  max-width: 320px;
  height: 80%;
  max-height: 480px;
  background: linear-gradient(135deg, #E85D75 0%, #845EF7 100%);
  border-radius: 8px 20px 20px 8px;
  box-shadow: 
    -8px 0 20px rgba(232, 93, 117, 0.3),
    0 15px 35px rgba(0, 0, 0, 0.2);
  position: relative;
  cursor: pointer;
  transition: transform 0.4s ease, box-shadow 0.4s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 25px;
  color: white;
  border-left: 10px solid rgba(0, 0, 0, 0.15);
}

.book-cover-front:hover {
  transform: rotateY(-5deg) scale(1.02);
}

.cover-design {
  border: 2px double rgba(255, 255, 255, 0.4);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 15px;
  text-align: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 5px;
}

.cover-ornament-top {
  font-size: 1.2rem;
  margin-bottom: 15px;
  opacity: 0.8;
}

.cover-title {
  font-size: clamp(1.2rem, 4vw, 1.8rem);
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  line-height: 1.3;
  word-break: keep-all;
}

.cover-author {
  font-size: 0.9rem;
  opacity: 0.9;
  margin-bottom: 20px;
}

.cover-ornament .icon {
  font-size: 2rem;
  color: rgba(255, 255, 255, 0.9);
}

.btn-open-book {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.5);
  color: white;
  padding: 10px 25px;
  border-radius: 50px;
  font-family: 'Nanum Myeongjo', serif;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-open-book:hover {
  background: white;
  color: #E85D75;
}

/* ========== Book Spread - 화면에 꽉 차게 ========== */
.book-spread {
  display: flex;
  background: linear-gradient(to right, #FAF8F5 0%, #FDFBF7 50%, #FAF8F5 100%);
  box-shadow: 
    0 15px 40px rgba(0, 0, 0, 0.12),
    0 5px 15px rgba(232, 93, 117, 0.08);
  border-radius: 6px;
  overflow: hidden;
  width: 100%;
  max-width: 900px;
  height: 100%;
  max-height: calc(100vh - 150px);
  position: relative;
}

/* Book Spine Effect */
.book-spread::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: rgba(0, 0, 0, 0.06);
  z-index: 10;
  transform: translateX(-50%);
}

.book-spread::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 30px;
  margin-left: -15px;
  background: linear-gradient(to right, 
    rgba(0, 0, 0, 0.03) 0%, 
    rgba(0, 0, 0, 0.06) 50%, 
    rgba(0, 0, 0, 0.03) 100%);
  pointer-events: none;
  z-index: 5;
}

/* ========== Book Pages ========== */
.book-page {
  flex: 1;
  padding: 30px 25px 50px;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-page {
  padding-right: 35px;
  border-right: 1px solid rgba(0, 0, 0, 0.02);
  background: linear-gradient(to right, #F8F6F3 0%, #FDFBF7 100%);
}

.right-page {
  padding-left: 35px;
  background: linear-gradient(to left, #F8F6F3 0%, #FDFBF7 100%);
}

/* ========== Page Content ========== */
.page-content {
  flex: 1;
  line-height: 1.7;
  color: #2D3436;
  overflow-y: auto;
  text-align: justify;
  padding-right: 5px;
  font-size: 15px;
}

.page-content::-webkit-scrollbar {
  width: 4px;
}

.page-content::-webkit-scrollbar-thumb {
  background: rgba(232, 93, 117, 0.2);
  border-radius: 2px;
}

.story-body {
  padding-bottom: 5px;
}

.story-paragraph {
  margin-bottom: 0.6em;
  text-indent: 1em;
  word-break: keep-all;
}

.chapter-title {
  text-align: center;
  margin-bottom: 25px;
  font-size: 1.3rem;
  color: #E85D75;
  border-bottom: 1px solid #FFD3E0;
  padding-bottom: 15px;
}

/* ========== Page Number ========== */
.page-number {
  text-align: center;
  font-size: 0.75rem;
  color: #95a5a6;
  font-family: 'Nunito', sans-serif;
  position: absolute;
  bottom: 15px;
  left: 0;
  right: 0;
}

/* ========== The End ========== */
.the-end {
  text-align: center;
  margin-top: 30px;
}

.fin {
  font-style: italic;
  font-size: 1.3rem;
  color: #E85D75;
}

.fin::before, .fin::after {
  content: '~';
  margin: 0 10px;
  color: #FFD3E0;
}

/* ========== Decoration Page ========== */
.decoration-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  border: 2px double #FFD3E0;
  padding: 20px;
  margin: 5px;
  height: calc(100% - 40px);
  background: linear-gradient(135deg, rgba(255, 245, 248, 0.5), rgba(255, 229, 236, 0.5));
  border-radius: 6px;
}

.book-cover-summary h2 {
  font-size: 1.4rem;
  margin-bottom: 15px;
  word-break: keep-all;
  color: #E85D75;
}

.author-info {
  color: #636E72;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.ornament {
  font-size: 2rem;
  color: #E85D75;
  margin: 15px 0;
}

.mini-toc {
  margin-top: 20px;
  border-top: 1px solid #FFD3E0;
  padding-top: 15px;
  color: #636E72;
  font-size: 0.9rem;
}

/* ========== 하단 네비게이션 (고정) ========== */
.viewer-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(232, 93, 117, 0.1);
  padding: 0 20px;
  z-index: 100;
}

.nav-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid #FFD3E0;
  background: white;
  color: #E85D75;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.nav-btn:hover:not(:disabled) {
  background: #E85D75;
  color: white;
  border-color: #E85D75;
  transform: scale(1.05);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  min-width: 150px;
}

.page-progress {
  width: 100%;
  height: 4px;
  background: #FFD3E0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #E85D75, #845EF7);
  transition: width 0.3s ease;
}

.page-text {
  font-size: 0.8rem;
  color: #636E72;
  font-weight: 600;
}

/* ========== Keyboard Hint ========== */
.keyboard-hint {
  position: fixed;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  padding: 5px 15px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  font-size: 0.75rem;
  color: #95a5a6;
  display: none;
  z-index: 50;
}

.keyboard-hint kbd {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 3px;
  margin: 0 2px;
}

/* ========== Transition Animations ========== */
.page-flip-enter-active {
  transition: all 0.4s ease-out;
}

.page-flip-leave-active {
  transition: all 0.3s ease-in;
}

.page-flip-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-flip-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* ========== Print Styles ========== */
@media print {
  .viewer-toolbar,
  .viewer-bottom-nav,
  .keyboard-hint {
    display: none !important;
  }

  .book-viewer-container {
    background: white !important;
    height: auto !important;
    min-height: auto !important;
  }

  .book-content-area {
    padding: 20px !important;
  }

  .book-spread {
    box-shadow: none !important;
    max-height: none !important;
    height: auto !important;
  }

  .page-content {
    overflow: visible !important;
  }
}

/* ========== Desktop ========== */
@media (min-width: 769px) {
  .keyboard-hint {
    display: block;
  }

  .viewer-toolbar {
    padding: 0 30px;
  }

  .book-content-area {
    padding: 60px 30px 80px;
  }
}

/* ========== Tablet & Mobile ========== */
@media (max-width: 768px) {
  .book-viewer-container {
    height: 100vh;
    height: 100dvh; /* Dynamic viewport height */
  }

  .book-content-area {
    padding: 55px 10px 65px;
  }

  .left-page {
    display: none;
  }

  .book-spread {
    flex-direction: column;
    max-height: none;
    height: 100%;
    border-radius: 10px;
  }

  .book-spread::before,
  .book-spread::after {
    display: none;
  }

  .right-page {
    padding: 20px 15px 40px;
    flex: 1;
    background: #FDFBF7;
  }

  .page-content {
    font-size: 16px;
    line-height: 1.9;
  }

  .story-paragraph {
    margin-bottom: 1em;
  }

  .chapter-title {
    font-size: 1.2rem;
    margin-bottom: 20px;
  }

  .book-cover-front {
    max-width: 280px;
    height: 70%;
  }

  .cover-title {
    font-size: 1.4rem;
  }

  .viewer-bottom-nav {
    height: 55px;
    gap: 15px;
  }

  .nav-btn {
    width: 40px;
    height: 40px;
  }

  .page-indicator {
    min-width: 120px;
  }

  .btn-toolbar {
    padding: 5px 10px;
    font-size: 0.8rem;
  }

  .book-title-mini {
    font-size: 0.85rem;
    max-width: 35%;
  }
}

/* ========== Small Mobile ========== */
@media (max-width: 480px) {
  .viewer-toolbar {
    padding: 0 10px;
    height: 45px;
  }

  .toolbar-controls {
    gap: 3px;
  }

  .btn-toolbar {
    padding: 4px 8px;
    font-size: 0.75rem;
  }

  .book-title-mini {
    max-width: 30%;
    font-size: 0.8rem;
  }

  .book-content-area {
    padding: 50px 8px 60px;
  }

  .right-page {
    padding: 15px 12px 35px;
  }

  .viewer-bottom-nav {
    height: 50px;
    gap: 10px;
    padding: 0 15px;
  }

  .nav-btn {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;
  }

  .page-indicator {
    min-width: 100px;
  }
}

/* 화면에서는 인쇄 콘텐츠 숨김 */
.print-content {
  display: none !important;
}
</style>

<!-- 인쇄 전용 스타일 (scoped 아님) -->
<style>
@media print {
  /* 전체 화면 스타일 초기화 */
  html, body {
    background: white !important;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }

  /* 뷰어의 화면 요소 숨김 */
  .book-viewer-container .viewer-toolbar,
  .book-viewer-container .viewer-bottom-nav,
  .book-viewer-container .keyboard-hint,
  .book-viewer-container .book-cover-wrapper,
  .book-viewer-container .book-spread,
  .book-viewer-container .book-content-area,
  .book-viewer-container .loading-state {
    display: none !important;
  }

  .book-viewer-container {
    background: white !important;
    padding: 0 !important;
    min-height: auto !important;
  }

  /* 인쇄 콘텐츠 표시 */
  .book-viewer-container .print-content {
    display: block !important;
    padding: 40px !important;
    max-width: 100% !important;
    margin: 0 auto !important;
    font-family: 'Nanum Myeongjo', 'Batang', 'Georgia', serif !important;
    color: #2D3436 !important;
    line-height: 1.8 !important;
    background: white !important;
  }

  /* 헤더 */
  .print-header {
    text-align: center !important;
    margin-bottom: 40px !important;
    padding-bottom: 30px !important;
  }

  .print-title {
    font-size: 28pt !important;
    font-weight: 700 !important;
    color: #E85D75 !important;
    margin-bottom: 15px !important;
    letter-spacing: 2px !important;
  }

  .print-author {
    font-size: 12pt !important;
    color: #636E72 !important;
    margin-bottom: 20px !important;
  }

  .print-meta {
    display: flex !important;
    justify-content: center !important;
    gap: 20px !important;
    font-size: 10pt !important;
    color: #858C8F !important;
    margin-bottom: 25px !important;
  }

  .print-divider {
    width: 100px !important;
    height: 2px !important;
    background: #E85D75 !important;
    margin: 0 auto !important;
  }

  /* 본문 */
  .print-body {
    text-align: justify !important;
    word-break: keep-all !important;
  }

  .print-paragraph {
    text-indent: 2em !important;
    margin-bottom: 0.8em !important;
    font-size: 11pt !important;
    line-height: 2 !important;
  }

  /* 푸터 */
  .print-footer {
    margin-top: 60px !important;
    padding-top: 30px !important;
    text-align: center !important;
    border-top: 1px solid #eee !important;
  }

  .print-fin {
    font-size: 16pt !important;
    font-style: italic !important;
    color: #E85D75 !important;
    margin-bottom: 20px !important;
    font-family: 'Georgia', serif !important;
  }

  .print-copyright {
    font-size: 9pt !important;
    color: #aaa !important;
  }

  /* 페이지 설정 */
  @page {
    margin: 2cm;
    size: A4;
  }

  @page :first {
    margin-top: 3cm;
  }
}
</style>

