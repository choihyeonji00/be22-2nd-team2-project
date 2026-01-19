package com.team2.reactionservice.websocket.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * WebSocket 댓글 생성 이벤트 DTO
 *
 * @author Next-Page Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent {
    private Long commentId;
    private Long bookId;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;
}
