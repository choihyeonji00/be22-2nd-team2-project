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

      <!-- Book Spread -->
      <div class="book-spread">
        <!-- Left Page (Decoration for Desktop) -->
        <div class="book-page left-page desktop-only">
            <div class="page-content decoration-page">
                <div class="book-cover-summary">
                    <h2>{{ book.title }}</h2>
                    <p class="author-info">참여 작가 {{ writerCount }}명</p>
                    <div class="ornament">❦</div>
                </div>
            </div>
            <div class="page-number">- 1 -</div>
        </div>

        <!-- Right Page (Content) -->
        <div class="book-page right-page" ref="contentPage">
          <div class="page-content" :style="{ fontSize: fontSize + 'px' }">
            <h2 class="chapter-title">{{ book.title }}</h2>
            <div class="story-body">
              <p v-for="sent in sortedSentences" :key="sent.sentenceId" class="story-paragraph">
                {{ sent.content }}
              </p>
            </div>
            <div class="the-end">
                <span class="fin">Fin.</span>
            </div>
          </div>
          <div class="page-number">- 2 -</div>
          <!-- Page curl effect -->
          <div class="page-curl"></div>
        </div>
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
const fontSize = ref(18)

const sortedSentences = computed(() => {
  return sentences.value ? [...sentences.value].sort((a, b) => a.sequenceNo - b.sequenceNo) : []
})

const writerCount = computed(() => {
    return sentences.value ? new Set(sentences.value.map(s => s.writerId)).size : 0
})

const goBack = () => router.back()

const increaseFontSize = () => { if (fontSize.value < 28) fontSize.value += 2 }
const decreaseFontSize = () => { if (fontSize.value > 14) fontSize.value -= 2 }

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

.book-viewer-container {
  min-height: 100vh;
  background-color: #2c3e50; /* Dark background for focus */
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  font-family: 'Nanum Myeongjo', serif;
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

.icon-btn-text:hover {
    background: rgba(255,255,255,0.1);
}

.book-stage {
  width: 100%;
  max-width: 1200px;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.book-spread {
  display: flex;
  background-color: #fdfbf7; /* Paper color */
  box-shadow: 
    0 20px 50px rgba(0,0,0,0.5), 
    inset 0 0 100px rgba(0,0,0,0.05);
  border-radius: 4px;
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
  min-height: 80vh;
  position: relative;
}

.book-page {
  flex: 1;
  padding: 60px 50px;
  position: relative;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* Vertical center line effect */
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
}

.page-content {
  flex: 1;
  line-height: 2.0; /* Comfortable reading */
  color: #2c3e50;
  overflow-y: auto;
  text-align: justify;
}

.story-body {
    white-space: pre-wrap; /* Preserve line breaks */
}

.story-paragraph {
    margin-bottom: 1.5em;
    text-indent: 1em; /* Indent start of paragraphs */
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

/* Cover Summary Page */
.decoration-page {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
    border: 4px double #eee;
    padding: 40px;
    margin: 20px;
}

.book-cover-summary h2 {
    font-size: 2.5rem;
    margin-bottom: 20px;
    word-break: keep-all;
}

.author-info {
    font-size: 1.2rem;
    color: #7f8c8d;
}

.ornament {
    font-size: 3rem;
    color: #E85D75;
    margin-top: 40px;
}

/* Mobile Responsive */
@media (max-width: 768px) {
    .left-page {
        display: none; /* Hide left page on mobile */
    }
    
    .book-spread::before, .book-spread::after {
        display: none; /* Remove spine effect */
    }

    .book-spread {
        flex-direction: column;
        box-shadow: none;
        background: transparent;
    }

    .book-page {
        background: #fdfbf7;
        box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        padding: 30px 20px;
        min-height: 80vh;
    }

    .chapter-title {
        font-size: 1.5rem;
    }
    
    .page-content {
        -webkit-overflow-scrolling: touch;
    }
    
    .book-viewer-container {
        padding: 0;
    }
    
    .viewer-toolbar {
        padding: 10px 20px;
        background: rgba(44, 62, 80, 0.95);
        backdrop-filter: blur(5px);
        margin-bottom: 0;
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 100;
        transition: transform 0.3s ease;
    }
    
    .toolbar-hidden {
        transform: translateY(-100%);
    }
    
    .book-stage {
        padding-top: 60px; /* Space for fixed toolbar */
    }
}
</style>
