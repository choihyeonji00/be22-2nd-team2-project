package com.team2.nextpage.command.reaction.dto.request;

import com.team2.nextpage.command.reaction.entity.VoteType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteRequest {
  private Long bookId;
  private VoteType voteType;
}
