package com.team2.reactionservice.command.reaction.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.VoteRequest;
import com.team2.reactionservice.command.reaction.entity.BookVote;
import com.team2.reactionservice.command.reaction.entity.Comment;
import com.team2.reactionservice.command.reaction.entity.SentenceVote;
import com.team2.reactionservice.command.reaction.entity.VoteType;
import com.team2.reactionservice.command.reaction.repository.BookVoteRepository;
import com.team2.reactionservice.command.reaction.repository.CommentRepository;
import com.team2.reactionservice.command.reaction.repository.SentenceVoteRepository;
import com.team2.reactionservice.feign.StoryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import com.team2.commonmodule.util.SecurityUtil;
import java.lang.reflect.Field;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

/**
 * ReactionService 단위 테스트
 * 댓글 및 투표 기능 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReactionService 단위 테스트")
class ReactionServiceTest {

        @InjectMocks
        private ReactionService reactionService;

        @Mock
        private CommentRepository commentRepository;

        @Mock
        private BookVoteRepository bookVoteRepository;

        @Mock
        private SentenceVoteRepository sentenceVoteRepository;

        @Mock
        private SimpMessagingTemplate messagingTemplate;

        @Mock
        private StoryServiceClient storyServiceClient;

        private Comment testComment;
        private Comment parentComment;
        private CreateCommentRequest createCommentRequest;
        private UpdateCommentRequest updateCommentRequest;
        private VoteRequest voteRequest;

        @BeforeEach
        void setUp() throws Exception {
                // 부모 댓글
                parentComment = Comment.builder()
                                .bookId(1L)
                                .writerId(1L)
                                .content("부모 댓글입니다.")
                                .parent(null)
                                .build();
                // Reflection을 사용하여 commentId 설정
                Field parentCommentIdField = Comment.class.getDeclaredField("commentId");
                parentCommentIdField.setAccessible(true);
                parentCommentIdField.set(parentComment, 1L);

                // 테스트 댓글
                testComment = Comment.builder()
                                .bookId(1L)
                                .writerId(2L)
                                .content("테스트 댓글입니다.")
                                .parent(null)
                                .build();
                // Reflection을 사용하여 commentId 설정
                Field testCommentIdField = Comment.class.getDeclaredField("commentId");
                testCommentIdField.setAccessible(true);
                testCommentIdField.set(testComment, 2L);

                // 댓글 생성 요청
                createCommentRequest = CreateCommentRequest.builder()
                                .bookId(1L)
                                .content("새 댓글입니다.")
                                .parentId(null)
                                .build();

                // 댓글 수정 요청
                updateCommentRequest = UpdateCommentRequest.builder()
                                .content("수정된 댓글입니다.")
                                .build();

                // 투표 요청
                voteRequest = VoteRequest.builder()
                                .bookId(1L)
                                .voteType(VoteType.LIKE)
                                .build();
        }

        @Test
        @DisplayName("댓글 작성 성공 - 일반 댓글")
        void addCommentSuccess() throws Exception {
                // Given
                Long currentUserId = 3L;

                Comment savedComment = Comment.builder()
                                .bookId(createCommentRequest.getBookId())
                                .writerId(currentUserId)
                                .content(createCommentRequest.getContent())
                                .parent(null)
                                .build();
                // Reflection을 사용하여 commentId 설정
                Field savedCommentIdField = Comment.class.getDeclaredField("commentId");
                savedCommentIdField.setAccessible(true);
                savedCommentIdField.set(savedComment, 3L);

                given(commentRepository.save(any(Comment.class)))
                                .willReturn(savedComment);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(currentUserId);

                        // When
                        Long commentId = reactionService.addComment(createCommentRequest);

                        // Then
                        assertThat(commentId).isEqualTo(3L);
                        then(commentRepository).should(times(1)).save(any(Comment.class));
                }
        }

        @Test
        @DisplayName("댓글 작성 성공 - 대댓글 (parentId 존재)")
        void addReplyCommentSuccess() {
                // Given
                Long currentUserId = 3L;
                CreateCommentRequest replyRequest = CreateCommentRequest.builder()
                                .bookId(1L)
                                .content("대댓글입니다.")
                                .parentId(1L)
                                .build();

                given(commentRepository.findById(1L))
                                .willReturn(Optional.of(parentComment));
                given(commentRepository.save(any(Comment.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(currentUserId);

                        // When
                        reactionService.addComment(replyRequest);

                        // Then
                        then(commentRepository).should(times(1)).findById(1L);
                        then(commentRepository).should(times(1)).save(argThat(comment -> comment.getParent() != null &&
                                        comment.getParent().getCommentId().equals(1L)));
                }
        }

        @Test
        @DisplayName("댓글 작성 실패 - 부모 댓글이 존재하지 않음")
        void addCommentFail_ParentNotFound() {
                // Given
                Long currentUserId = 3L;
                CreateCommentRequest replyRequest = CreateCommentRequest.builder()
                                .bookId(1L)
                                .content("대댓글입니다.")
                                .parentId(999L)
                                .build();

                given(commentRepository.findById(999L))
                                .willReturn(Optional.empty());

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(currentUserId);

                        // When & Then
                        assertThatThrownBy(() -> reactionService.addComment(replyRequest))
                                        .isInstanceOf(BusinessException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
                }
        }

        @Test
        @DisplayName("댓글 작성 실패 - 부모 댓글과 다른 소설")
        void addCommentFail_DifferentBook() {
                // Given
                Long currentUserId = 3L;
                CreateCommentRequest replyRequest = CreateCommentRequest.builder()
                                .bookId(2L) // 부모 댓글의 bookId는 1L
                                .content("대댓글입니다.")
                                .parentId(1L)
                                .build();

                given(commentRepository.findById(1L))
                                .willReturn(Optional.of(parentComment));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(currentUserId);

                        // When & Then
                        assertThatThrownBy(() -> reactionService.addComment(replyRequest))
                                        .isInstanceOf(BusinessException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
                }
        }

        @Test
        @DisplayName("댓글 수정 성공 - 작성자가 수정")
        void modifyCommentSuccess() {
                // Given
                Long commentId = 2L;
                Long currentUserId = 2L; // testComment의 작성자

                given(commentRepository.findById(commentId))
                                .willReturn(Optional.of(testComment));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(currentUserId);

                        // When
                        reactionService.modifyComment(commentId, updateCommentRequest);

                        // Then
                        assertThat(testComment.getContent()).isEqualTo(updateCommentRequest.getContent());
                }
        }

        @Test
        @DisplayName("댓글 수정 실패 - 작성자가 아님")
        void modifyCommentFail_NotOwner() {
                // Given
                Long commentId = 2L;
                Long notOwnerId = 999L;

                given(commentRepository.findById(commentId))
                                .willReturn(Optional.of(testComment));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(notOwnerId);

                        // When & Then
                        assertThatThrownBy(() -> reactionService.modifyComment(commentId, updateCommentRequest))
                                        .isInstanceOf(BusinessException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_OWNER);
                }
        }

        @Test
        @DisplayName("댓글 삭제 성공 - 작성자가 삭제")
        void removeCommentSuccess_Owner() {
                // Given
                Long commentId = 2L;
                Long currentUserId = 2L;

                given(commentRepository.findById(commentId))
                                .willReturn(Optional.of(testComment));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(currentUserId);
                        securityUtil.when(SecurityUtil::isAdmin)
                                        .thenReturn(false);

                        // When
                        reactionService.removeComment(commentId);

                        // Then
                        then(commentRepository).should(times(1)).delete(testComment);
                }
        }

        @Test
        @DisplayName("댓글 삭제 성공 - 관리자가 삭제")
        void removeCommentSuccess_Admin() {
                // Given
                Long commentId = 2L;
                Long adminId = 999L;

                given(commentRepository.findById(commentId))
                                .willReturn(Optional.of(testComment));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(adminId);
                        securityUtil.when(SecurityUtil::isAdmin)
                                        .thenReturn(true);

                        // When
                        reactionService.removeComment(commentId);

                        // Then
                        then(commentRepository).should(times(1)).delete(testComment);
                }
        }

        @Test
        @DisplayName("댓글 삭제 실패 - 작성자도 관리자도 아님")
        void removeCommentFail_NotOwnerNorAdmin() {
                // Given
                Long commentId = 2L;
                Long notOwnerId = 999L;

                given(commentRepository.findById(commentId))
                                .willReturn(Optional.of(testComment));

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(notOwnerId);
                        securityUtil.when(SecurityUtil::isAdmin)
                                        .thenReturn(false);

                        // When & Then
                        assertThatThrownBy(() -> reactionService.removeComment(commentId))
                                        .isInstanceOf(BusinessException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_OWNER);

                        // Verify
                        then(commentRepository).should(never()).delete(any(Comment.class));
                }
        }

        @Test
        @DisplayName("소설 투표 성공 - 새로운 좋아요 투표")
        void voteBookSuccess_NewLike() {
                // Given
                Long voterId = 3L;

                given(bookVoteRepository.findByBookIdAndVoterId(voteRequest.getBookId(), voterId))
                                .willReturn(Optional.empty());
                given(bookVoteRepository.countByBookIdAndVoteType(voteRequest.getBookId(), VoteType.LIKE))
                                .willReturn(1L);
                given(bookVoteRepository.countByBookIdAndVoteType(voteRequest.getBookId(), VoteType.DISLIKE))
                                .willReturn(0L);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(voterId);

                        // When
                        Boolean result = reactionService.voteBook(voteRequest);

                        // Then
                        assertThat(result).isTrue(); // 투표 추가됨
                        then(bookVoteRepository).should(times(1)).save(any(BookVote.class));
                }
        }

        @Test
        @DisplayName("소설 투표 성공 - 같은 투표 토글 (취소)")
        void voteBookSuccess_ToggleSameVote() {
                // Given
                Long voterId = 3L;
                BookVote existingVote = BookVote.builder()
                                .bookId(voteRequest.getBookId())
                                .voterId(voterId)
                                .voteType(VoteType.LIKE)
                                .build();

                given(bookVoteRepository.findByBookIdAndVoterId(voteRequest.getBookId(), voterId))
                                .willReturn(Optional.of(existingVote));
                given(bookVoteRepository.countByBookIdAndVoteType(voteRequest.getBookId(), VoteType.LIKE))
                                .willReturn(0L);
                given(bookVoteRepository.countByBookIdAndVoteType(voteRequest.getBookId(), VoteType.DISLIKE))
                                .willReturn(0L);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(voterId);

                        // When
                        Boolean result = reactionService.voteBook(voteRequest);

                        // Then
                        assertThat(result).isFalse(); // 투표 취소됨
                        then(bookVoteRepository).should(times(1)).delete(existingVote);
                }
        }

        @Test
        @DisplayName("소설 투표 성공 - 다른 투표로 변경")
        void voteBookSuccess_ChangeVote() {
                // Given
                Long voterId = 3L;
                BookVote existingVote = BookVote.builder()
                                .bookId(voteRequest.getBookId())
                                .voterId(voterId)
                                .voteType(VoteType.DISLIKE) // 기존: DISLIKE
                                .build();

                VoteRequest likeRequest = VoteRequest.builder()
                                .bookId(1L)
                                .voteType(VoteType.LIKE) // 새로운: LIKE
                                .build();

                given(bookVoteRepository.findByBookIdAndVoterId(likeRequest.getBookId(), voterId))
                                .willReturn(Optional.of(existingVote));
                given(bookVoteRepository.countByBookIdAndVoteType(likeRequest.getBookId(), VoteType.LIKE))
                                .willReturn(1L);
                given(bookVoteRepository.countByBookIdAndVoteType(likeRequest.getBookId(), VoteType.DISLIKE))
                                .willReturn(0L);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(voterId);

                        // When
                        Boolean result = reactionService.voteBook(likeRequest);

                        // Then
                        assertThat(result).isTrue(); // 투표 변경됨
                        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.LIKE);
                        then(bookVoteRepository).should(never()).delete(any(BookVote.class));
                }
        }

        @Test
        @DisplayName("문장 투표 성공 - 새로운 좋아요 투표")
        void voteSentenceSuccess_NewLike() {
                // Given
                Long sentenceId = 1L;
                Long voterId = 3L;
                Long bookId = 1L;

                VoteRequest sentenceVoteRequest = VoteRequest.builder()
                                .voteType(VoteType.LIKE)
                                .build();

                given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, voterId))
                                .willReturn(Optional.empty());
                given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.LIKE))
                                .willReturn(1L);
                given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.DISLIKE))
                                .willReturn(0L);
                given(storyServiceClient.getBookIdBySentenceId(sentenceId))
                                .willReturn(bookId);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(voterId);

                        // When
                        Boolean result = reactionService.voteSentence(sentenceId, sentenceVoteRequest);

                        // Then
                        assertThat(result).isTrue();
                        then(sentenceVoteRepository).should(times(1)).save(any(SentenceVote.class));
                }
        }

        @Test
        @DisplayName("문장 투표 성공 - 같은 투표 토글 (취소)")
        void voteSentenceSuccess_ToggleSameVote() {
                // Given
                Long sentenceId = 1L;
                Long voterId = 3L;
                Long bookId = 1L;

                VoteRequest sentenceVoteRequest = VoteRequest.builder()
                                .voteType(VoteType.LIKE)
                                .build();

                SentenceVote existingVote = SentenceVote.builder()
                                .sentenceId(sentenceId)
                                .voterId(voterId)
                                .voteType(VoteType.LIKE)
                                .build();

                given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, voterId))
                                .willReturn(Optional.of(existingVote));
                given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.LIKE))
                                .willReturn(0L);
                given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.DISLIKE))
                                .willReturn(0L);
                given(storyServiceClient.getBookIdBySentenceId(sentenceId))
                                .willReturn(bookId);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(voterId);

                        // When
                        Boolean result = reactionService.voteSentence(sentenceId, sentenceVoteRequest);

                        // Then
                        assertThat(result).isFalse(); // 투표 취소됨
                        then(sentenceVoteRepository).should(times(1)).delete(existingVote);
                }
        }

        @Test
        @DisplayName("문장 투표 성공 - 다른 투표로 변경")
        void voteSentenceSuccess_ChangeVote() {
                // Given
                Long sentenceId = 1L;
                Long voterId = 3L;
                Long bookId = 1L;

                VoteRequest likeRequest = VoteRequest.builder()
                                .voteType(VoteType.LIKE)
                                .build();

                SentenceVote existingVote = SentenceVote.builder()
                                .sentenceId(sentenceId)
                                .voterId(voterId)
                                .voteType(VoteType.DISLIKE) // 기존: DISLIKE
                                .build();

                given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, voterId))
                                .willReturn(Optional.of(existingVote));
                given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.LIKE))
                                .willReturn(1L);
                given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.DISLIKE))
                                .willReturn(0L);
                given(storyServiceClient.getBookIdBySentenceId(sentenceId))
                                .willReturn(bookId);

                try (MockedStatic<SecurityUtil> securityUtil = mockStatic(
                                SecurityUtil.class)) {

                        securityUtil.when(SecurityUtil::getCurrentUserId)
                                        .thenReturn(voterId);

                        // When
                        Boolean result = reactionService.voteSentence(sentenceId, likeRequest);

                        // Then
                        assertThat(result).isTrue(); // 투표 변경됨
                        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.LIKE);
                }
        }
}
