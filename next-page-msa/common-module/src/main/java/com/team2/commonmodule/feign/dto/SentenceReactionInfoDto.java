package com.team2.commonmodule.feign.dto;

import lombok.*;


/**
 * SentenceReactionInfoDto
 *
 * @author Next-Page Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceReactionInfoDto {
    private Long sentenceId;
    private long likeCount;
    private long dislikeCount;
    private String myVote; // "LIKE", "DISLIKE", or null
}
