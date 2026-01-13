package com.team2.nextpage.command.reaction.service;

import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.SentenceVoteRequest;
import com.team2.nextpage.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.VoteRequest;
import com.team2.nextpage.command.reaction.entity.BookVote;
import com.team2.nextpage.command.reaction.entity.Comment;
import com.team2.nextpage.command.reaction.repository.BookVoteRepository;
import com.team2.nextpage.command.reaction.repository.CommentRepository;
import com.team2.nextpage.command.reaction.repository.SentenceVoteRepository;
import com.team2.nextpage.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 반응(댓글/투표) Command 서비스
 *
 * @author 정병진
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReactionService {

  private final CommentRepository commentRepository;
  private final BookVoteRepository bookVoteRepository;
  private final SentenceVoteRepository sentenceVoteRepository;

  /**
   * 댓글 작성
   *
   * @param request 댓글 작성 요청 정보(bookId, content)
   * @return 생성된 댓글의 ID
   */
  public Long addComment(CreateCommentRequest request) {

    Long writerId = SecurityUtil.getCurrentUserId();

    Comment newComment = Comment.builder()
        .bookId(request.getBookId())
        .writerId(writerId)
        .content(request.getContent())
        .build();

    Comment saveComment = commentRepository.save(newComment);

    return saveComment.getCommentId();
  }

  /**
   * 댓글 수정
   *
   * @param commentId 수정할 댓글 ID
   * @param request   수정할 내용이 담긴 DTO
   */
  public void modifyComment(Long commentId, UpdateCommentRequest request) {

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

    // 권한 체크 (타인 댓글 수정/삭제 불가 예외 처리)
    validateWriter(comment, SecurityUtil.getCurrentUserId());

    comment.updateContent(request.getContent());
  }

  /**
   * 댓글 삭제
   *
   * @param commentId 삭제할 댓글 ID
   */
  public void removeComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

    // 권한 체크 (타인 댓글 수정/삭제 불가 예외 처리)
    validateWriter(comment, SecurityUtil.getCurrentUserId());

    commentRepository.delete(comment);

  }

  /**
   * 소설 좋아요/싫어요 투표 처리 (토글 방식)
   * 1. 기록 없음 -> 투표 생성 (true 반환)
   * 2. 기록 있음 + 같은 타입 -> 투표 취소 (삭제, false 반환)
   * 3. 기록 있음 + 다른 타입 -> 투표 변경 (수정, true 반환)
   *
   * @param request 투표 요청 정보 (bookId, voteType)
   * @return 최종 투표 반영 여부 (true: 반영됨 / false: 취소됨)
   */
  @Transactional
  public Boolean voteBook(VoteRequest request) {
    Long voterId = SecurityUtil.getCurrentUserId();

    // 1. 이미 투표한 기록이 있는지 확인
    Optional<BookVote> existingVote = bookVoteRepository.findByBookIdAndVoterId(request.getBookId(), voterId);

    if (existingVote.isPresent()) {
      BookVote vote = existingVote.get();
      // 2. 이미 투표 후 또 누르면 -> 취소
      if (vote.getVoteType() == request.getVoteType()) {
        bookVoteRepository.delete(vote);
        return false;
      } else {
        // 다른 걸 눌렀다면, 다른 걸로 변경(좋아요 -> 싫어요, 싫어요 -> 좋아요)
        vote.changeVoteType(request.getVoteType());

        return true; // 투표 변경
      }
    } else {
      BookVote newVote = BookVote.builder()
          .bookId(request.getBookId())
          .voterId(voterId)
          .voteType(request.getVoteType())
          .build();
      bookVoteRepository.save(newVote);
      return true;
    }
  }



  /**
   * 작성자 권한 검증(내부 헬퍼 메서드)
   * 요청한 사용자(userId)가 댓글 작성자(writerId)와 일치하는지 확인합니다.
   *
   * @param comment 검증할 댓글 엔티티
   * @param userId  요청을 보낸 사용자의 ID
   * @throws IllegalArgumentException 작성자가 아닐 경우 예외 발생
   */
  private void validateWriter(Comment comment, Long userId) {
    if (!comment.getWriterId().equals(userId)) {
      throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
    }
  }
}
