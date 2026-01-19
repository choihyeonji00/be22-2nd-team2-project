package com.team2.commonmodule.feign;

import com.team2.commonmodule.feign.dto.*;
import com.team2.commonmodule.response.ApiResponse;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ReactionServiceClient
 *
 * @author Next-Page Team
 */
@FeignClient(name = "reaction-service")
public interface ReactionServiceClient {

        @PostMapping("/internal/reactions/sentences/stats")
        ApiResponse<Map<Long, SentenceReactionInfoDto>> getSentenceReactions(
                        @RequestBody List<Long> sentenceIds,
                        @RequestParam(value = "userId", required = false) Long userId);

        @GetMapping("/internal/reactions/books/{bookId}/stats")
        ApiResponse<BookReactionInfoDto> getBookReactionStats(
                        @PathVariable("bookId") Long bookId,
                        @RequestParam(value = "userId", required = false) Long userId);

        @GetMapping("/internal/members/{userId}/stats")
        ApiResponse<MemberReactionStatsDto> getMemberReactionStats(
                        @PathVariable("userId") Long userId);

        @PostMapping("/internal/reactions/books/stats")
        ApiResponse<Map<Long, BookReactionInfoDto>> getBookReactions(
                        @RequestBody List<Long> bookIds,
                        @RequestParam(value = "userId", required = false) Long userId);
}
