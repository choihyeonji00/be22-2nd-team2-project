// Pull to Refresh & Real-time Updates Script
(function () {
    let startY = 0;
    let pullDistance = 0;
    const threshold = 80;
    let isPulling = false;
    let stompClient = null;

    // Pull-to-Refresh 초기화
    function initPullToRefresh() {
        const container = document.querySelector('main.container');
        if (!container) return;

        container.addEventListener('touchstart', function (e) {
            if (window.scrollY === 0) {
                startY = e.touches[0].clientY;
                isPulling = true;
            }
        });

        container.addEventListener('touchmove', function (e) {
            if (!isPulling) return;

            pullDistance = e.touches[0].clientY - startY;

            if (pullDistance > 0 && pullDistance < threshold * 2) {
                e.preventDefault();
                // Visual feedback (optional)
                container.style.transform = `translateY(${pullDistance * 0.5}px)`;
                container.style.opacity = 1 - (pullDistance / (threshold * 4));
            }
        });

        container.addEventListener('touchend', function () {
            if (pullDistance > threshold) {
                // Trigger refresh
                showToast('새로고침 중...', 'info');
                location.reload();
            }

            // Reset
            container.style.transform = '';
            container.style.opacity = '';
            isPulling = false;
            pullDistance = 0;
        });
    }

    // WebSocket 연결 (새 소설 실시간 수신)
    function connectWebSocket() {
        const currentPath = window.location.pathname;
        if (currentPath !== '/' && currentPath !== '/index') {
            return;
        }

        try {
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null;

            stompClient.connect({}, function (frame) {
                stompClient.subscribe('/topic/books/new', function (message) {
                    const bookEvent = JSON.parse(message.body);
                    addNewBookToList(bookEvent);
                    if (typeof showToast === 'function') {
                        showToast(`새로운 소설 "${bookEvent.title}"이 등록되었습니다!`, 'success');
                    }
                });
            }, function (error) {
                // Connection error handling
            });
        } catch (e) {
            // Setup error handling
        }
    }

    // 새 소설을 목록 상단에 추가
    function addNewBookToList(bookEvent) {
        const $bookList = $('#book-list');
        if (!$bookList.length) return;

        // Remove "no results" message if exists
        $bookList.find('.card:has(h3:contains("등록된 소설이 없습니다"))').remove();

        // Determine icon based on category
        let icon = '📖';
        const catName = bookEvent.categoryName;
        if (catName.includes('로맨스') || catName === 'ROMANCE') icon = '💖';
        else if (catName.includes('스릴러') || catName === 'THRILLER') icon = '🔪';
        else if (catName.includes('판타지') || catName === 'FANTASY') icon = '🏰';
        else if (catName.includes('SF')) icon = '👽';
        else if (catName.includes('미스터리') || catName === 'MYSTERY') icon = '🕵️';
        else if (catName.includes('일상') || catName === 'DAILY') icon = '☕';

        const html = `
            <div class="card new-item" onclick="location.href='/books/${bookEvent.bookId}'" style="cursor: pointer; animation: slideInFromTop 0.6s ease-out;">
                <div class="book-cover-placeholder">
                    <span class="book-icon">${icon}</span>
                </div>
                <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
                    <span class="badge badge-writing">연재중</span>
                    <span style="font-size: 0.8rem; color: var(--text-muted); text-transform:uppercase; letter-spacing:1px;">${bookEvent.categoryName}</span>
                </div>
                <h3 style="margin-bottom: 10px; font-size: 1.4rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${bookEvent.title}</h3>
                <p style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 20px;">
                    <span style="color: var(--primary-color);">Make by.</span> ${bookEvent.writerNickname}
                </p>
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid rgba(255,255,255,0.1); padding-top: 15px;">
                    <span style="font-size: 0.85rem; color: var(--text-muted);">
                        👥 1명 참여
                    </span>
                    <span style="font-size: 0.85rem; color: var(--text-muted);">
                        📝 1 문장
                    </span>
                </div>
            </div>
        `;

        $bookList.prepend(html);

        // 새 항목 강조 효과
        setTimeout(function () {
            $('.new-item').removeClass('new-item');
        }, 3000);
    }

    // 페이지 로드 시 초기화
    $(document).ready(function () {
        initPullToRefresh();
        connectWebSocket();
    });
})();
