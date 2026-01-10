package com.team2.nextpage.command.reaction;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 반응(댓글/투표) Command 컨트롤러
 *
 * @author 정병진
 */
@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    /**
     * 댓글 등록 API
     */
    @PostMapping("/comments")
    public void createComment() {
        // impl
    }

    /**
     * 투표 API
     */
    @PostMapping("/votes")
    public void vote() {
        // impl
    }
}
