package com.team2.commonmodule.feign.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentNotificationDto {
    private Long commentId;
    private Long bookId;
    private String content;
    private Long parentId;
    private String nickname;
    private LocalDateTime createdAt;
}
