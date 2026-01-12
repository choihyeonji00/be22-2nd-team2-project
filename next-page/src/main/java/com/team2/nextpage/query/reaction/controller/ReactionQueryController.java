package com.team2.nextpage.query.reaction.controller;

import com.team2.nextpage.common.response.ApiResponse;
import com.team2.nextpage.query.reaction.dto.response.CommentDto;
import com.team2.nextpage.query.reaction.service.ReactionQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 반응 Query 컨트롤러
 *
 * @author 정병진
 */
@RestController
@RequestMapping("/api/reactions")
public class ReactionQueryController {

    private final ReactionQueryService reactionQueryService;

    public ReactionQueryController(ReactionQueryService reactionQueryService) {
        this.reactionQueryService = reactionQueryService;
    }

  /**
   * 댓글 목록 조회 API
   * GET /api/reactions/comments/{bookId}
   *
   * @param bookId 조회할 소설의 ID
   * @return       댓글 목록 (ApiResponse로 래핑)
   */
  @GetMapping("/comments/{bookId}")
  public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(@PathVariable Long bookId) {
    List<CommentDto> comments = reactionQueryService.getComments(bookId);
    return ResponseEntity.ok(ApiResponse.success(comments));
  }
}
