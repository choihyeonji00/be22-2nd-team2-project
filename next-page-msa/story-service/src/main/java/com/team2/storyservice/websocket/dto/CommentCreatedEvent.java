package com.team2.storyservice.websocket.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * 실시간 댓글 생성 이벤트 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent {
    private Long commentId;
    private Long bookId;
    private Long writerId;
    private String writerNicknm;
    private String content;
    private LocalDateTime createdAt;
}
