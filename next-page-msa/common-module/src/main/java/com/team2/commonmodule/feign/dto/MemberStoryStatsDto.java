package com.team2.commonmodule.feign.dto;

import lombok.*;


/**
 * MemberStoryStatsDto
 *
 * @author Next-Page Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStoryStatsDto {
    private int createdBookCount;
    private int writtenSentenceCount;
}
