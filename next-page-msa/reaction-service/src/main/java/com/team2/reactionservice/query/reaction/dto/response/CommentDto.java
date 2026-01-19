package com.team2.reactionservice.query.reaction.dto.response;

import java.time.LocalDateTime;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 조회용 DTO
 *
 * @author 정병진
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto extends RepresentationModel<CommentDto> {
    private Long commentId;
    private String content;
    private Long writerId; // MSA: Feign Client로 조회하기 위한 ID
    private String writerNicknm;
    private LocalDateTime createdAt;

    // 대댓글 지원
    private Long parentId;
    @Builder.Default
    private List<CommentDto> children = new ArrayList<>();

    // 마이페이지용 추가 정보
    private Long bookId;
    private String bookTitle;
}
