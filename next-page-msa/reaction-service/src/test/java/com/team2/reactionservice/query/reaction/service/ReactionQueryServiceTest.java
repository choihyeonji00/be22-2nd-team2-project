package com.team2.reactionservice.query.reaction.service;

import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.StoryServiceClient;
import com.team2.commonmodule.feign.dto.BookBatchInfoDto;
import com.team2.commonmodule.feign.dto.BookInfoDto;
import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.reactionservice.query.reaction.dto.response.CommentDto;
import com.team2.reactionservice.query.reaction.dto.response.CommentPageResponse;
import com.team2.reactionservice.query.reaction.mapper.ReactionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReactionQueryServiceTest {

    @InjectMocks
    private ReactionQueryService reactionQueryService;

    @Mock
    private ReactionMapper reactionMapper;

    @Mock
    private MemberServiceClient memberServiceClient;

    @Mock
    private StoryServiceClient storyServiceClient;

    // ===== getComments(Long) 테스트 =====

    @Test
    @DisplayName("댓글 목록 조회 - 빈 목록")
    void getComments_EmptyList() {
        // Given
        Long bookId = 1L;
        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(Collections.emptyList());

        // When
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("댓글 목록 조회 - 최상위 댓글만")
    void getComments_RootCommentsOnly() {
        // Given
        Long bookId = 1L;

        CommentDto root1 = createComment(1L, null, 100L, bookId, "First Comment");
        CommentDto root2 = createComment(2L, null, 200L, bookId, "Second Comment");

        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(List.of(root1, root2));

        // Feign Mock
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCommentId()).isEqualTo(1L);
        assertThat(result.get(1).getCommentId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("댓글 목록 조회 - 부모-자식 관계 검증 (트리 구조)")
    void getComments_TreeStructure() {
        // Given
        Long bookId = 1L;

        CommentDto root = createComment(1L, null, 100L, bookId, "Root");
        CommentDto child1 = createComment(2L, 1L, 200L, bookId, "Child 1");
        CommentDto child2 = createComment(3L, 1L, 300L, bookId, "Child 2");
        CommentDto grandchild = createComment(4L, 2L, 400L, bookId, "Grandchild");

        given(reactionMapper.findCommentsByBookId(bookId))
                .willReturn(List.of(root, child1, child2, grandchild));

        // Feign Mock
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // Then
        assertThat(result).hasSize(1); // 최상위만 반환
        assertThat(result.get(0).getCommentId()).isEqualTo(1L);
        assertThat(result.get(0).getChildren()).hasSize(2); // child1, child2

        CommentDto child1Result = result.get(0).getChildren().get(0);
        assertThat(child1Result.getCommentId()).isEqualTo(2L);
        assertThat(child1Result.getChildren()).hasSize(1); // grandchild

        assertThat(child1Result.getChildren().get(0).getCommentId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("댓글 목록 조회 - Orphan 처리 (부모 삭제된 댓글은 최상위로)")
    void getComments_OrphanHandling() {
        // Given
        Long bookId = 1L;

        CommentDto root = createComment(1L, null, 100L, bookId, "Root");
        CommentDto orphan = createComment(2L, 999L, 200L, bookId, "Orphan (parent deleted)");

        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(List.of(root, orphan));

        // Feign Mock
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // Then
        assertThat(result).hasSize(2); // root + orphan (최상위로 올라옴)
        assertThat(result).extracting("commentId").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("댓글 목록 조회 - Feign 성공: 회원 정보")
    void getComments_FeignSuccess_MemberInfo() {
        // Given
        Long bookId = 1L;

        CommentDto comment1 = createComment(1L, null, 100L, bookId, "Comment 1");
        CommentDto comment2 = createComment(2L, null, 200L, bookId, "Comment 2");

        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(List.of(comment1, comment2));

        // Feign Mock - Member Service 성공
        MemberInfoDto member1 = MemberInfoDto.builder()
                .userId(100L)
                .userNicknm("Writer1")
                .build();
        MemberInfoDto member2 = MemberInfoDto.builder()
                .userId(200L)
                .userNicknm("Writer2")
                .build();
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(List.of(member1, member2))
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWriterNicknm()).isEqualTo("Writer1");
        assertThat(result.get(1).getWriterNicknm()).isEqualTo("Writer2");
    }

    @Test
    @DisplayName("댓글 목록 조회 - Feign 실패: 닉네임 null")
    void getComments_FeignFailure_NicknameNull() {
        // Given
        Long bookId = 1L;

        CommentDto comment = createComment(1L, null, 100L, bookId, "Comment");

        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(List.of(comment));

        // Feign Mock - Member Service 실패
        given(memberServiceClient.getMembersBatch(anyList()))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWriterNicknm()).isNull();
    }

    // ===== getCommentsByUser() 테스트 =====

    @Test
    @DisplayName("사용자 댓글 조회 - 정상 조회")
    void getCommentsByUser_Success() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        CommentDto comment = createComment(1L, null, userId, 100L, "My Comment");

        given(reactionMapper.findCommentsByWriterId(userId, 0, 10)).willReturn(List.of(comment));
        given(reactionMapper.countCommentsByWriterId(userId)).willReturn(1L);

        // Feign Mock - Member Service
        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(userId)
                .userNicknm("CommentWriter")
                .build();
        ApiResponse<MemberInfoDto> memberResponse = ApiResponse.success(memberInfo);
        given(memberServiceClient.getMemberInfo(userId)).willReturn(memberResponse);

        // Feign Mock - Story Service
        BookInfoDto bookInfo = new BookInfoDto(100L, "Test Book", null, null);
        ApiResponse<BookBatchInfoDto> bookResponse = ApiResponse.success(
                new BookBatchInfoDto(List.of(bookInfo))
        );
        given(storyServiceClient.getBooksBatch(anyList())).willReturn(bookResponse);

        // When
        CommentPageResponse response = reactionQueryService.getCommentsByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getWriterNicknm()).isEqualTo("CommentWriter");
        assertThat(response.getContent().get(0).getBookTitle()).isEqualTo("Test Book");
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 댓글 조회 - 빈 목록")
    void getCommentsByUser_EmptyList() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        given(reactionMapper.findCommentsByWriterId(userId, 0, 10)).willReturn(Collections.emptyList());
        given(reactionMapper.countCommentsByWriterId(userId)).willReturn(0L);

        // When
        CommentPageResponse response = reactionQueryService.getCommentsByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("사용자 댓글 조회 - Feign 성공: 회원 + 소설 정보")
    void getCommentsByUser_FeignSuccess_AllInfo() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        CommentDto comment1 = createComment(1L, null, userId, 100L, "Comment 1");
        CommentDto comment2 = createComment(2L, null, userId, 200L, "Comment 2");

        given(reactionMapper.findCommentsByWriterId(userId, 0, 10)).willReturn(List.of(comment1, comment2));
        given(reactionMapper.countCommentsByWriterId(userId)).willReturn(2L);

        // Feign Mock - Member Service
        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(userId)
                .userNicknm("TestWriter")
                .build();
        ApiResponse<MemberInfoDto> memberResponse = ApiResponse.success(memberInfo);
        given(memberServiceClient.getMemberInfo(userId)).willReturn(memberResponse);

        // Feign Mock - Story Service
        BookInfoDto book1 = new BookInfoDto(100L, "Book 1", null, null);
        BookInfoDto book2 = new BookInfoDto(200L, "Book 2", null, null);
        ApiResponse<BookBatchInfoDto> bookResponse = ApiResponse.success(
                new BookBatchInfoDto(List.of(book1, book2))
        );
        given(storyServiceClient.getBooksBatch(anyList())).willReturn(bookResponse);

        // When
        CommentPageResponse response = reactionQueryService.getCommentsByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getWriterNicknm()).isEqualTo("TestWriter");
        assertThat(response.getContent().get(0).getBookTitle()).isEqualTo("Book 1");
        assertThat(response.getContent().get(1).getBookTitle()).isEqualTo("Book 2");
    }

    @Test
    @DisplayName("사용자 댓글 조회 - Feign 실패: 모든 정보 null")
    void getCommentsByUser_FeignFailure_AllInfoNull() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        CommentDto comment = createComment(1L, null, userId, 100L, "Comment");

        given(reactionMapper.findCommentsByWriterId(userId, 0, 10)).willReturn(List.of(comment));
        given(reactionMapper.countCommentsByWriterId(userId)).willReturn(1L);

        // Feign Mock - Member Service 실패
        given(memberServiceClient.getMemberInfo(userId))
                .willThrow(new RuntimeException("Service unavailable"));

        // Feign Mock - Story Service 실패
        given(storyServiceClient.getBooksBatch(anyList()))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        CommentPageResponse response = reactionQueryService.getCommentsByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getWriterNicknm()).isNull();
        assertThat(response.getContent().get(0).getBookTitle()).isNull();
    }

    // ===== Helper Methods =====

    private CommentDto createComment(Long commentId, Long parentId, Long writerId, Long bookId, String content) {
        CommentDto comment = new CommentDto();
        comment.setCommentId(commentId);
        comment.setParentId(parentId);
        comment.setWriterId(writerId);
        comment.setBookId(bookId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }
}
