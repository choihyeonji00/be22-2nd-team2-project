package com.team2.commonmodule.feign.dto;

import lombok.*;


/**
 * MemberReactionStatsDto
 *
 * @author Next-Page Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberReactionStatsDto {
    private int writtenCommentCount;
}
