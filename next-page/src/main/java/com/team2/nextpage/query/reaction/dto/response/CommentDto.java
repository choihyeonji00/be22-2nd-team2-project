package com.team2.nextpage.query.reaction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 댓글 조회용 DTO
 *
 * @author 정병진
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentDto {
    private Long commentId;
    private String content;
    private String writerNicknm;
    private LocalDateTime createdAt;

    // 대댓글 지원
    private Long parentId;
    private java.util.List<CommentDto> children = new java.util.ArrayList<>();
}
