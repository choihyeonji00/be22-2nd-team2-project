package com.team2.storyservice.query.book.dto.response;

import java.time.LocalDateTime;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;

/**
 * 소설 조회용 DTO
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
public class BookDto extends RepresentationModel<BookDto> {
    private Long bookId;
    private Long writerId;
    private String writerNicknm; // 작가 닉네임
    private String categoryId;
    private String title;
    private String status;
    private Integer currentSequence;
    private Integer maxSequence;
    private Integer participantCount; // 참여 작가 수
    private Integer likeCount; // 좋아요 수
    private Integer dislikeCount; // 싫어요 수
    private LocalDateTime createdAt;
}
