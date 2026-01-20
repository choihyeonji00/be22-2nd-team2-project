package com.team2.storyservice.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 소설 카드 통계 실시간 업데이트 DTO
 * 
 * @author 정진호
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatsUpdateDto {
    private Long bookId;
    private Integer sentenceCount;
    private Integer participantCount;
    private Integer likeCount;
    private Integer dislikeCount;
}
