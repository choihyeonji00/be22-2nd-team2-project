package com.team2.nextpage.query.book;

import lombok.Getter;

/**
 * 소설 조회용 DTO
 *
 * @author 최현지
 */
@Getter
public class BookDto {
    private Long bookId;
    private String title;
    private String status;
    private Integer currentSequence;
    // ...
}
