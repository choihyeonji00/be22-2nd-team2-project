package com.team2.nextpage.command.reaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 반응(댓글/투표) Command 서비스
 *
 * @author 정병진
 */
@Service
@Transactional
public class ReactionService {

    private final CommentRepository commentRepository;

    public ReactionService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * 댓글 작성
     */
    public void addComment() {
        // impl
    }

    /**
     * 소설 좋아요 투표
     */
    public void voteBook() {
        // impl
    }
}
