package com.team2.storyservice.websocket.dto;

import lombok.*;


/**
 * VoteUpdateDto
 *
 * @author 정진호
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteUpdateDto {
    private String targetType; // "BOOK" or "SENTENCE"
    private Long targetId;
    private long likeCount;
    private long dislikeCount;
}
