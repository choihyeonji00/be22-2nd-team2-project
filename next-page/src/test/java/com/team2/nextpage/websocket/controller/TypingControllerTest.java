package com.team2.nextpage.websocket.controller;

import com.team2.nextpage.websocket.dto.TypingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypingControllerTest {

    @InjectMocks
    private TypingController typingController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private TypingStatus typingStatus;

    @BeforeEach
    void setUp() {
        typingStatus = new TypingStatus();
        typingStatus.setBookId(1L);
        typingStatus.setUserNickname("테스터");
        typingStatus.setTyping(false);
    }

    @Test
    @DisplayName("입력 시작 이벤트 처리")
    void handleTypingStart_Success() {
        // given
        assertThat(typingStatus.isTyping()).isFalse();

        // when
        typingController.handleTypingStart(typingStatus);

        // then
        assertThat(typingStatus.isTyping()).isTrue();
        verify(messagingTemplate, times(1))
                .convertAndSend("/topic/typing/1", typingStatus);
    }

    @Test
    @DisplayName("입력 종료 이벤트 처리")
    void handleTypingStop_Success() {
        // given
        typingStatus.setTyping(true);
        assertThat(typingStatus.isTyping()).isTrue();

        // when
        typingController.handleTypingStop(typingStatus);

        // then
        assertThat(typingStatus.isTyping()).isFalse();
        verify(messagingTemplate, times(1))
                .convertAndSend("/topic/typing/1", typingStatus);
    }

    @Test
    @DisplayName("댓글 입력 시작 이벤트 처리")
    void handleCommentTypingStart_Success() {
        // given
        assertThat(typingStatus.isTyping()).isFalse();

        // when
        typingController.handleCommentTypingStart(typingStatus);

        // then
        assertThat(typingStatus.isTyping()).isTrue();
        verify(messagingTemplate, times(1))
                .convertAndSend("/topic/comment-typing/1", typingStatus);
    }

    @Test
    @DisplayName("댓글 입력 종료 이벤트 처리")
    void handleCommentTypingStop_Success() {
        // given
        typingStatus.setTyping(true);
        assertThat(typingStatus.isTyping()).isTrue();

        // when
        typingController.handleCommentTypingStop(typingStatus);

        // then
        assertThat(typingStatus.isTyping()).isFalse();
        verify(messagingTemplate, times(1))
                .convertAndSend("/topic/comment-typing/1", typingStatus);
    }

    @Test
    @DisplayName("TypingStatus static 메서드 테스트 - startTyping")
    void typingStatus_StartTyping() {
        // when
        TypingStatus status = TypingStatus.startTyping(1L, "닉네임");

        // then
        assertThat(status.getBookId()).isEqualTo(1L);
        assertThat(status.getUserNickname()).isEqualTo("닉네임");
        assertThat(status.isTyping()).isTrue();
    }

    @Test
    @DisplayName("TypingStatus static 메서드 테스트 - stopTyping")
    void typingStatus_StopTyping() {
        // when
        TypingStatus status = TypingStatus.stopTyping(1L, "닉네임");

        // then
        assertThat(status.getBookId()).isEqualTo(1L);
        assertThat(status.getUserNickname()).isEqualTo("닉네임");
        assertThat(status.isTyping()).isFalse();
    }
}