package com.team2.storyservice.query.book.dto.request;

import lombok.*;

/**
 * 소설 검색 요청 DTO (페이지, 정렬, 필터링)
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchRequest {

    // 페이지
    @Builder.Default
    private Integer page = 0; // 페이지 번호 (0부터 시작)
    @Builder.Default
    private Integer size = 10; // 페이지 크기

    // 정렬
    @Builder.Default
    private String sortBy = "createdAt"; // 정렬 기준 (createdAt, title)
    @Builder.Default
    private String sortOrder = "DESC"; // 정렬 방향 (ASC, DESC)

    // 필터링
    private String status; // 상태 필터 (WRITING, COMPLETED)
    private String categoryId; // 카테고리 필터
    private String keyword; // 제목 검색 키워드
    private Long writerId; // 작성자 ID 필터

    public Integer getOffset() {
        return page * size;
    }

}
