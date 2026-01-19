package com.team2.reactionservice.feign.controller;

import com.team2.commonmodule.feign.dto.*;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.reactionservice.feign.service.ReactionInternalService;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ReactionInternalController
 *
 * @author 정병진
 */
@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class ReactionInternalController {

    private final ReactionInternalService reactionInternalService;

    @GetMapping("/members/{userId}/stats")
    public ResponseEntity<ApiResponse<MemberReactionStatsDto>> getMemberReactionStats(@PathVariable Long userId) {
        MemberReactionStatsDto stats = reactionInternalService.getMemberReactionStats(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/reactions/sentences/stats")
    public ResponseEntity<ApiResponse<Map<Long, SentenceReactionInfoDto>>> getSentenceReactions(
            @RequestBody List<Long> sentenceIds,
            @RequestParam(required = false) Long userId) {

        Map<Long, SentenceReactionInfoDto> stats = reactionInternalService
                .getSentenceReactions(sentenceIds, userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/reactions/books/{bookId}/stats")
    public ResponseEntity<ApiResponse<BookReactionInfoDto>> getBookReactionStats(
            @PathVariable Long bookId,
            @RequestParam(required = false) Long userId) {

        BookReactionInfoDto stats = reactionInternalService.getBookReactionStats(bookId, userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/reactions/books/stats")
    public ResponseEntity<ApiResponse<Map<Long, BookReactionInfoDto>>> getBookReactions(
            @RequestBody List<Long> bookIds,
            @RequestParam(required = false) Long userId) {

        Map<Long, BookReactionInfoDto> stats = reactionInternalService
                .getBookReactions(bookIds, userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
