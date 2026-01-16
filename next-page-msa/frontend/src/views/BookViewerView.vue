<template>
  <div class="book-viewer-container">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>책을 펼치는 중입니다...</p>
    </div>

    <div v-else class="book-stage">
      <!-- Toolbar -->
      <div class="viewer-toolbar" :class="{ 'toolbar-hidden': isToolbarHidden }">
        <button @click="goBack" class="btn btn-ghost">
          ← 돌아가기
        </button>
        <span class="book-title-mini">{{ book.title }}</span>
        <div class="font-controls">
          <button @click="decreaseFontSize" class="icon-btn-text">A-</button>
          <button @click="increaseFontSize" class="icon-btn-text">A+</button>
        </div>
      </div>

      <!-- Cover Page (Page 0) -->
      <transition name="book-open" mode="out-in">
        <div v-if="currentSpread === 0" class="book-cover-wrapper" key="cover">
            <div class="book-cover-front" @click="nextSpread">
                <div class="cover-design">
                    <div class="spine-line"></div>
                    <div class="cover-content">
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

        <!-- Book Spread (Pages 1+) -->
      <div v-else class="book-spread" key="spread">
        <!-- Left Page -->
        <div class="book-page left-page desktop-only">
            <!-- Spread 1: Title Page -->
            <div v-if="currentSpread === 1" class="page-content decoration-page">
                <div class="book-cover-summary">
                    <h2>{{ book.title }}</h2>
                    <p class="author-info">Make by. Team2</p>
                    <div class="ornament">❦</div>
                    <div class="mini-toc">
                        <p>이야기의 시작</p>
                    </div>
                </div>
            </div>
            <!-- Other Spreads: Content -->
            <div v-else class="page-content" :style="{ fontSize: fontSize + 'px' }">
                <div class="story-body">
                    <p v-for="sent in leftPageSentences" :key="sent.sentenceId" class="story-paragraph">
                        {{ sent.content }}
                    </p>
                </div>
            </div>
            <div class="page-number">- {{ getLeftPageNumber }} -</div>
        </div>

        <!-- Right Page -->
        <div class="book-page right-page" ref="contentPage">
          <div class="page-content" :style="{ fontSize: fontSize + 'px' }">
            <h2 v-if="currentSpread === 1" class="chapter-title">Chapter 1</h2>
            <h2 v-else-if="(currentSpread - 1) % 5 === 0" class="chapter-title">Chapter {{ Math.ceil((currentSpread - 1) / 2) + 1 }}</h2>
            
            <div class="story-body">
              <p v-for="sent in rightPageSentences" :key="sent.sentenceId" class="story-paragraph">
                {{ sent.content }}
              </p>
            </div>

            <div v-if="currentSpread === totalSpreads" class="the-end">
                <span class="fin">Fin.</span>
            </div>
          </div>

          <!-- Pagination Controls (Absolute Positioned Bottom) -->
          <div class="viewer-pagination">
                <button class="btn-nav" @click="prevSpread">
                    이전
                </button>
                <div class="page-info">{{ currentSpread }} / {{ totalSpreads }}</div>
                <button class="btn-nav" :disabled="currentSpread === totalSpreads" @click="nextSpread">
                    다음
                </button>
          </div>

          <div class="page-number" v-if="getRightPageNumber">- {{ getRightPageNumber }} -</div>
          <!-- Page curl effect -->
          <div class="page-curl"></div>
        </div>
      </div>
    </transition>
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
const fontSize = ref(18)

// Pagination (Spread based)
const currentSpread = ref(0)
const itemsPerPage = 8 

const sortedSentences = computed(() => {
  return sentences.value ? [...sentences.value].sort((a, b) => a.sequenceNo - b.sequenceNo) : []
})

const totalContentPages = computed(() => Math.ceil(sortedSentences.value.length / itemsPerPage))

const totalSpreads = computed(() => {
    if (sortedSentences.value.length === 0) return 0
    const pages = Math.ceil(sortedSentences.value.length / itemsPerPage)
    if (pages === 0) return 0
    return 1 + Math.ceil((pages - 1) / 2)
})

const leftPageSentences = computed(() => {
    if (currentSpread.value <= 1) return []
    const pageIndex = 1 + (currentSpread.value - 2) * 2
    const start = pageIndex * itemsPerPage
    const end = start + itemsPerPage
    return sortedSentences.value.slice(start, end)
})

const rightPageSentences = computed(() => {
    if (currentSpread.value === 0) return []
    let pageIndex;
    if (currentSpread.value === 1) {
        pageIndex = 0 
    } else {
        pageIndex = 2 + (currentSpread.value - 2) * 2
    }
    const start = pageIndex * itemsPerPage
    const end = start + itemsPerPage
    return sortedSentences.value.slice(start, end)
})

const getLeftPageNumber = computed(() => {
    if (currentSpread.value <= 1) return null
    const pageNum = 1 + (currentSpread.value - 2) * 2 + 1
    return pageNum > totalContentPages.value ? null : pageNum
})

const getRightPageNumber = computed(() => {
    if (currentSpread.value === 0) return null
    const pageNum = (currentSpread.value === 1) ? 1 : (2 + (currentSpread.value - 2) * 2 + 1)
    return pageNum > totalContentPages.value ? null : pageNum
})

const writerCount = computed(() => {
    return sentences.value ? new Set(sentences.value.map(s => s.writerId)).size : 0
})

const goBack = () => router.back()
const increaseFontSize = () => { if (fontSize.value < 28) fontSize.value += 2 }
const decreaseFontSize = () => { if (fontSize.value > 14) fontSize.value -= 2 }

const scrollToTop = () => window.scrollTo(0, 0)

const nextPage = () => {
    if (currentSpread.value === 0) {
        currentSpread.value = 1
        return
    }
    if (currentSpread.value < totalSpreads.value) {
        currentSpread.value++
        scrollToTop()
    }
}
const nextSpread = nextPage 

const prevSpread = () => {
    if (currentSpread.value > 0) {
        currentSpread.value--
        scrollToTop()
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
  
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
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
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@700&display=swap');

.book-viewer-container {
  min-height: 100vh;
  background-color: #2c3e50;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  font-family: 'Nanum Myeongjo', serif;
  overflow-x: hidden;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: #ecf0f1;
}

.viewer-toolbar {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #ecf0f1;
  margin-bottom: 20px;
  padding: 0 10px;
  z-index: 100;
}

.book-title-mini {
    font-weight: 700;
    font-size: 1.2rem;
}

.icon-btn-text {
    background: none;
    border: 1px solid rgba(255,255,255,0.3);
    color: white;
    padding: 5px 10px;
    border-radius: 4px;
    margin-left: 5px;
    cursor: pointer;
}

.book-stage {
  width: 100%;
  max-width: 1200px;
  flex: 1;
  display: flex;
  justify-content: center; 
  align-items: center;
  position: relative;
  perspective: 1500px;
}

/* --- COVER STYLES --- */
.book-cover-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

.book-cover-front {
    width: 400px;
    height: 600px;
    background: linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%);
    border-radius: 10px 20px 20px 10px;
    box-shadow: 
        -15px 0 30px rgba(0,0,0,0.5),
        inset 5px 0 10px rgba(255,255,255,0.1),
        inset -5px 0 10px rgba(0,0,0,0.3);
    position: relative;
    cursor: pointer;
    transition: transform 0.3s ease;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px;
    color: white;
    border-left: 15px solid rgba(0,0,0,0.2);
}

.book-cover-front:hover {
    transform: rotateY(-10deg) scale(1.02);
}

.cover-design {
    border: 3px double rgba(255,255,255,0.3);
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 20px;
    text-align: center;
}

.cover-title {
    font-family: 'Cinzel', serif;
    font-size: 2.5rem;
    margin-bottom: 20px;
    text-shadow: 0 2px 4px rgba(0,0,0,0.5);
    line-height: 1.2;
}

.cover-author {
    font-size: 1.1rem;
    opacity: 0.8;
    margin-bottom: 40px;
}

.cover-ornament .icon {
    font-size: 4rem;
    color: rgba(255,255,255,0.8);
}

.btn-open-book {
    margin-top: 50px;
    background: transparent;
    border: 1px solid rgba(255,255,255,0.5);
    color: white;
    padding: 10px 30px;
    border-radius: 30px;
    font-family: 'Nanum Myeongjo', serif;
    cursor: pointer;
    transition: all 0.3s;
}

.btn-open-book:hover {
    background: white;
    color: #2c3e50;
}

/* --- BOOK SPREAD STYLES --- */
.book-spread {
  display: flex;
  background-color: #fdfbf7;
  box-shadow: 
    0 20px 50px rgba(0,0,0,0.5), 
    inset 0 0 100px rgba(0,0,0,0.05);
  border-radius: 4px;
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
  min-height: 700px; /* Fixed Height for consistency */
  height: 80vh;
  position: relative;
  margin: 0 auto; /* Ensure centering */
}

.book-page {
  flex: 1;
  padding: 60px 40px;
  position: relative;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.left-page {
    padding-right: 50px;
    border-right: 1px solid rgba(0,0,0,0.03);
}

.right-page {
    padding-left: 50px;
}

/* Transition Animations */
.book-open-enter-active {
  transition: all 0.8s ease-out;
  transform-origin: center left;
}

.book-open-leave-active {
  transition: all 0.5s ease-in;
}

.book-open-enter-from {
  opacity: 0;
  transform: rotateY(-90deg);
}

.book-open-leave-to {
  opacity: 0;
  transform: rotateY(90deg);
}

.book-spread::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 0;
    bottom: 0;
    width: 2px;
    background: rgba(0,0,0,0.05);
    z-index: 10;
}

.book-spread::after {
    content: '';
    position: absolute;
    left: 50%;
    top: 0;
    bottom: 0;
    width: 40px;
    margin-left: -20px;
    background: linear-gradient(to right, 
        rgba(0,0,0,0.05) 0%, 
        rgba(0,0,0,0.15) 35%, 
        rgba(0,0,0,0.2) 50%, 
        rgba(0,0,0,0.15) 65%, 
        rgba(0,0,0,0.05) 100%);
    pointer-events: none;
    z-index: 5;
}

.page-content {
  flex: 1;
  line-height: 2.0; 
  color: #2c3e50;
  overflow-y: auto;
  text-align: justify;
  padding-bottom: 50px; /* Space for pagination */
}

.story-paragraph {
    margin-bottom: 1.5em;
    text-indent: 1em; 
}

.chapter-title {
    text-align: center;
    margin-bottom: 40px;
    font-size: 2rem;
    color: #1a1a1a;
    border-bottom: 2px solid #ddd;
    padding-bottom: 20px;
}

.page-number {
    text-align: center;
    margin-top: 20px;
    font-size: 0.9rem;
    color: #95a5a6;
    font-family: 'Nunito', sans-serif;
    position: absolute;
    bottom: 20px;
    left: 0; right: 0;
}

.the-end {
    text-align: center;
    margin-top: 50px;
    margin-bottom: 30px;
}

.fin {
    font-style: italic;
    font-family: serif;
    font-size: 1.5rem;
    color: #7f8c8d;
    position: relative;
    padding: 0 20px;
}
.fin::before, .fin::after {
    content: '~';
    margin: 0 10px;
}

.decoration-page {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
    border: 4px double #eee;
    padding: 20px;
    margin: 10px;
    height: 100%;
}

.book-cover-summary h2 {
    font-size: 2.0rem;
    margin-bottom: 20px;
    word-break: keep-all;
}

.mini-toc {
    margin-top: 40px;
    font-family: 'Cinzel', serif;
    border-top: 1px solid #eee;
    padding-top: 20px;
}

/* Pagination Styles */
.viewer-pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
    position: absolute;
    bottom: 50px;
    width: 100%;
    left: 0;
    pointer-events: auto; /* Ensure clickable */
    z-index: 20;
}

.btn-nav {
    background: white;
    border: 1px solid #bdc3c7;
    padding: 5px 15px;
    border-radius: 20px;
    color: #7f8c8d;
    font-family: 'Nunito', sans-serif;
    font-size: 0.8rem;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.btn-nav:hover:not(:disabled) {
    border-color: #2c3e50;
    color: #2c3e50;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.btn-nav:disabled {
    opacity: 0.4;
    cursor: not-allowed;
    background: #fcfcfc;
}

/* Mobile Responsive */
@media (max-width: 768px) {
    .left-page, .book-cover-front .cover-ornament, .spine-line {
        display: none; 
    }
    
    .book-cover-front {
        width: 100%;
        height: 70vh;
        border-radius: 5px 15px 15px 5px;
    }
    
    .book-spread::before, .book-spread::after {
        display: none; 
    }

    .book-spread {
        flex-direction: column;
        box-shadow: none;
        background: transparent;
        min-height: auto;
        height: auto;
    }

    .book-page {
        background: #fdfbf7;
        box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        padding: 60px 20px 80px 20px; /* More bottom padding for nav */
        min-height: 80vh;
    }

    .mobile-only { display: block; }
    .desktop-only { display: none; }

    .viewer-toolbar {
        position: fixed;
        top: 0; left: 0; right: 0;
        background: rgba(44, 62, 80, 0.95);
        backdrop-filter: blur(5px);
        margin-bottom: 0;
        transition: transform 0.3s ease;
    }
    .toolbar-hidden { transform: translateY(-100%); }
    .book-stage { padding-top: 60px; }
    
    .viewer-pagination {
        position: absolute;
        bottom: 20px;
    }
}

</style>
