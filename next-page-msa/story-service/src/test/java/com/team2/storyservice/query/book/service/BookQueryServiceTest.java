package com.team2.storyservice.query.book.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.ReactionServiceClient;
import com.team2.commonmodule.feign.dto.BookReactionInfoDto;
import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.feign.dto.SentenceReactionInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.storyservice.query.book.dto.request.BookSearchRequest;
import com.team2.storyservice.query.book.dto.response.BookDetailDto;
import com.team2.storyservice.query.book.dto.response.BookDto;
import com.team2.storyservice.query.book.dto.response.BookPageResponse;
import com.team2.storyservice.query.book.dto.response.SentenceDto;
import com.team2.storyservice.query.book.dto.response.SentencePageResponse;
import com.team2.storyservice.query.book.mapper.BookMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class BookQueryServiceTest {

    @InjectMocks
    private BookQueryService bookQueryService;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private MemberServiceClient memberServiceClient;

    @Mock
    private ReactionServiceClient reactionServiceClient;

    private static MockedStatic<SecurityUtil> securityUtil;

    @BeforeAll
    public static void beforeAll() {
        securityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtil.close();
    }

    // ===== searchBooks(BookSearchRequest) 테스트 =====

    @Test
    @DisplayName("소설 검색 - 빈 목록 반환")
    void searchBooks_EmptyList() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        given(bookMapper.findBooks(request)).willReturn(Collections.emptyList());
        given(bookMapper.countBooks(request)).willReturn(0L);

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("소설 검색 - 데이터 있음")
    void searchBooks_WithData() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setTitle("Test Book");
        bookDto.setWriterId(100L);

        List<BookDto> books = List.of(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // Feign Mock - Member Service (빈 응답)
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // Feign Mock - Reaction Service (빈 응답)
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException());
        ApiResponse<Map<Long, BookReactionInfoDto>> reactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getBookReactions(anyList(), isNull())).willReturn(reactionResponse);

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Test Book");
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("소설 검색 - Feign 성공: 작가 정보 조회")
    void searchBooks_FeignSuccess_MemberInfo() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setWriterId(100L);

        List<BookDto> books = List.of(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // Feign Mock - Member Service 성공
        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(100L)
                .userNicknm("TestWriter")
                .build();
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(List.of(memberInfo))
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // Feign Mock - Reaction Service
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException());
        ApiResponse<Map<Long, BookReactionInfoDto>> reactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getBookReactions(anyList(), isNull())).willReturn(reactionResponse);

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getWriterNicknm()).isEqualTo("TestWriter");
    }

    @Test
    @DisplayName("소설 검색 - Feign 실패: 작가 정보 (닉네임 null)")
    void searchBooks_FeignFailure_MemberInfo() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setWriterId(100L);

        List<BookDto> books = List.of(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // Feign Mock - Member Service 실패
        given(memberServiceClient.getMembersBatch(anyList()))
                .willThrow(new RuntimeException("Service unavailable"));

        // Feign Mock - Reaction Service
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException());
        ApiResponse<Map<Long, BookReactionInfoDto>> reactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getBookReactions(anyList(), isNull())).willReturn(reactionResponse);

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getWriterNicknm()).isNull();
    }

    @Test
    @DisplayName("소설 검색 - Feign 성공: 반응 정보 조회")
    void searchBooks_FeignSuccess_ReactionInfo() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setWriterId(100L);

        List<BookDto> books = List.of(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // Feign Mock - Member Service
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // Feign Mock - Reaction Service 성공
        securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(999L);
        BookReactionInfoDto reactionInfo = BookReactionInfoDto.builder()
                .bookId(1L)
                .likeCount(10)
                .dislikeCount(2)
                .build();
        Map<Long, BookReactionInfoDto> reactionMap = Map.of(1L, reactionInfo);
        ApiResponse<Map<Long, BookReactionInfoDto>> reactionResponse = ApiResponse.success(reactionMap);
        given(reactionServiceClient.getBookReactions(anyList(), eq(999L))).willReturn(reactionResponse);

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getLikeCount()).isEqualTo(10);
        assertThat(response.getContent().get(0).getDislikeCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("소설 검색 - Feign 실패: 반응 정보 (기본값 0)")
    void searchBooks_FeignFailure_ReactionInfo() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setWriterId(100L);

        List<BookDto> books = List.of(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // Feign Mock - Member Service
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // Feign Mock - Reaction Service 실패
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException());
        given(reactionServiceClient.getBookReactions(anyList(), isNull()))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getLikeCount()).isEqualTo(0);
        assertThat(response.getContent().get(0).getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("소설 검색 - SecurityUtil.getCurrentUserId() 실패 케이스")
    void searchBooks_SecurityUtilFailure() {
        // Given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setWriterId(100L);

        List<BookDto> books = List.of(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // Feign Mock - Member Service
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // SecurityUtil Mock - 예외 발생 (비로그인 상태)
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException("No authentication"));

        // Feign Mock - Reaction Service (userId = null)
        ApiResponse<Map<Long, BookReactionInfoDto>> reactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getBookReactions(anyList(), isNull())).willReturn(reactionResponse);

        // When
        BookPageResponse response = bookQueryService.searchBooks(request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        // 예외가 발생해도 정상 진행되어야 함
    }

    // ===== searchBooks() - Deprecated 메서드 =====

    @Test
    @DisplayName("소설 목록 조회 - Deprecated 메서드")
    void searchBooks_Deprecated() {
        // Given
        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setTitle("Old Method");

        given(bookMapper.findAllBooks()).willReturn(List.of(bookDto));

        // When
        List<BookDto> result = bookQueryService.searchBooks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Old Method");
    }

    // ===== getBook(Long) 테스트 =====

    @Test
    @DisplayName("소설 상세 보기 - 정상 조회 + Feign 성공")
    void getBook_Success_WithFeignSuccess() {
        // Given
        Long bookId = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setBookId(bookId);
        bookDto.setTitle("Test Book");
        bookDto.setWriterId(100L);

        given(bookMapper.findBookDetail(bookId)).willReturn(bookDto);

        // Feign Mock - Member Service 성공
        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(100L)
                .userNicknm("Author")
                .build();
        ApiResponse<MemberInfoDto> memberResponse = ApiResponse.success(memberInfo);
        given(memberServiceClient.getMemberInfo(100L)).willReturn(memberResponse);

        // When
        BookDto result = bookQueryService.getBook(bookId);

        // Then
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getWriterNicknm()).isEqualTo("Author");
    }

    @Test
    @DisplayName("소설 상세 보기 - 존재하지 않음 → BusinessException")
    void getBook_NotFound_ThrowsException() {
        // Given
        Long bookId = 999L;
        given(bookMapper.findBookDetail(bookId)).willReturn(null);

        // When & Then
        assertThatThrownBy(() -> bookQueryService.getBook(bookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("소설 상세 보기 - Feign 실패: 닉네임 null")
    void getBook_FeignFailure_NicknameNull() {
        // Given
        Long bookId = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setBookId(bookId);
        bookDto.setWriterId(100L);

        given(bookMapper.findBookDetail(bookId)).willReturn(bookDto);

        // Feign Mock - Member Service 실패
        given(memberServiceClient.getMemberInfo(100L))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        BookDto result = bookQueryService.getBook(bookId);

        // Then
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getWriterNicknm()).isNull();
    }

    // ===== getBookForViewer(Long) 테스트 =====

    @Test
    @DisplayName("뷰어 모드 조회 - 로그인 사용자")
    void getBookForViewer_AuthenticatedUser() {
        // Given
        Long bookId = 1L;
        Long userId = 100L;

        BookDetailDto bookDetailDto = new BookDetailDto();
        bookDetailDto.setBookId(bookId);
        bookDetailDto.setTitle("Viewer Book");
        bookDetailDto.setWriterId(200L);

        SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setSentenceId(10L);
        sentenceDto.setWriterId(200L);

        securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        given(bookMapper.findBookForViewer(bookId, userId)).willReturn(bookDetailDto);
        given(bookMapper.findSentencesByBookId(bookId, userId)).willReturn(List.of(sentenceDto));

        // Feign Mocks
        BookReactionInfoDto bookReaction = BookReactionInfoDto.builder()
                .bookId(bookId)
                .likeCount(5)
                .dislikeCount(1)
                .myVote("LIKE")
                .build();
        ApiResponse<BookReactionInfoDto> reactionResponse = ApiResponse.success(bookReaction);
        given(reactionServiceClient.getBookReactionStats(bookId, userId)).willReturn(reactionResponse);

        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(200L)
                .userNicknm("Writer")
                .build();
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(List.of(memberInfo))
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        ApiResponse<Map<Long, SentenceReactionInfoDto>> sentenceReactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getSentenceReactions(anyList(), eq(userId))).willReturn(sentenceReactionResponse);

        // When
        BookDetailDto result = bookQueryService.getBookForViewer(bookId);

        // Then
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getTitle()).isEqualTo("Viewer Book");
        assertThat(result.getLikeCount()).isEqualTo(5);
        assertThat(result.getDislikeCount()).isEqualTo(1);
        assertThat(result.getMyVote()).isEqualTo("LIKE");
        assertThat(result.getWriterNicknm()).isEqualTo("Writer");
    }

    @Test
    @DisplayName("뷰어 모드 조회 - 비로그인 사용자")
    void getBookForViewer_UnauthenticatedUser() {
        // Given
        Long bookId = 1L;

        BookDetailDto bookDetailDto = new BookDetailDto();
        bookDetailDto.setBookId(bookId);
        bookDetailDto.setWriterId(200L);

        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException("No auth"));
        given(bookMapper.findBookForViewer(bookId, null)).willReturn(bookDetailDto);
        given(bookMapper.findSentencesByBookId(bookId, null)).willReturn(Collections.emptyList());

        // Feign Mocks
        ApiResponse<BookReactionInfoDto> reactionResponse = ApiResponse.success(
                BookReactionInfoDto.builder().bookId(bookId).likeCount(0).dislikeCount(0).build()
        );
        given(reactionServiceClient.getBookReactionStats(bookId, null)).willReturn(reactionResponse);

        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        BookDetailDto result = bookQueryService.getBookForViewer(bookId);

        // Then
        assertThat(result.getBookId()).isEqualTo(bookId);
    }

    @Test
    @DisplayName("뷰어 모드 조회 - 존재하지 않는 소설 → BusinessException")
    void getBookForViewer_NotFound_ThrowsException() {
        // Given
        Long bookId = 999L;

        securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(100L);
        given(bookMapper.findBookForViewer(bookId, 100L)).willReturn(null);

        // When & Then
        assertThatThrownBy(() -> bookQueryService.getBookForViewer(bookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("뷰어 모드 조회 - Feign 성공: 반응 정보")
    void getBookForViewer_FeignSuccess_ReactionInfo() {
        // Given
        Long bookId = 1L;
        Long userId = 100L;

        BookDetailDto bookDetailDto = new BookDetailDto();
        bookDetailDto.setBookId(bookId);
        bookDetailDto.setWriterId(200L);

        securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        given(bookMapper.findBookForViewer(bookId, userId)).willReturn(bookDetailDto);
        given(bookMapper.findSentencesByBookId(bookId, userId)).willReturn(Collections.emptyList());

        // Feign Mock - Reaction Service 성공
        BookReactionInfoDto reactionInfo = BookReactionInfoDto.builder()
                .bookId(bookId)
                .likeCount(20)
                .dislikeCount(5)
                .myVote("DISLIKE")
                .build();
        ApiResponse<BookReactionInfoDto> reactionResponse = ApiResponse.success(reactionInfo);
        given(reactionServiceClient.getBookReactionStats(bookId, userId)).willReturn(reactionResponse);

        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        BookDetailDto result = bookQueryService.getBookForViewer(bookId);

        // Then
        assertThat(result.getLikeCount()).isEqualTo(20);
        assertThat(result.getDislikeCount()).isEqualTo(5);
        assertThat(result.getMyVote()).isEqualTo("DISLIKE");
    }

    @Test
    @DisplayName("뷰어 모드 조회 - Feign 실패: 반응 정보 (기본값 0)")
    void getBookForViewer_FeignFailure_ReactionInfo() {
        // Given
        Long bookId = 1L;
        Long userId = 100L;

        BookDetailDto bookDetailDto = new BookDetailDto();
        bookDetailDto.setBookId(bookId);
        bookDetailDto.setWriterId(200L);

        securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        given(bookMapper.findBookForViewer(bookId, userId)).willReturn(bookDetailDto);
        given(bookMapper.findSentencesByBookId(bookId, userId)).willReturn(Collections.emptyList());

        // Feign Mock - Reaction Service 실패
        given(reactionServiceClient.getBookReactionStats(bookId, userId))
                .willThrow(new RuntimeException("Service unavailable"));

        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(Collections.emptyList())
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        // When
        BookDetailDto result = bookQueryService.getBookForViewer(bookId);

        // Then
        assertThat(result.getLikeCount()).isEqualTo(0);
        assertThat(result.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("뷰어 모드 조회 - Feign 성공: 회원 정보 (작가 + 문장 작가들)")
    void getBookForViewer_FeignSuccess_MemberInfo() {
        // Given
        Long bookId = 1L;
        Long userId = 100L;

        BookDetailDto bookDetailDto = new BookDetailDto();
        bookDetailDto.setBookId(bookId);
        bookDetailDto.setWriterId(200L);

        SentenceDto sentence1 = new SentenceDto();
        sentence1.setSentenceId(10L);
        sentence1.setWriterId(300L);

        SentenceDto sentence2 = new SentenceDto();
        sentence2.setSentenceId(11L);
        sentence2.setWriterId(400L);

        securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        given(bookMapper.findBookForViewer(bookId, userId)).willReturn(bookDetailDto);
        given(bookMapper.findSentencesByBookId(bookId, userId)).willReturn(List.of(sentence1, sentence2));

        // Feign Mock - Member Service 성공
        MemberInfoDto member1 = MemberInfoDto.builder().userId(200L).userNicknm("BookWriter").build();
        MemberInfoDto member2 = MemberInfoDto.builder().userId(300L).userNicknm("SentenceWriter1").build();
        MemberInfoDto member3 = MemberInfoDto.builder().userId(400L).userNicknm("SentenceWriter2").build();
        ApiResponse<MemberBatchInfoDto> memberResponse = ApiResponse.success(
                new MemberBatchInfoDto(List.of(member1, member2, member3))
        );
        given(memberServiceClient.getMembersBatch(anyList())).willReturn(memberResponse);

        ApiResponse<BookReactionInfoDto> reactionResponse = ApiResponse.success(
                BookReactionInfoDto.builder().bookId(bookId).likeCount(0).dislikeCount(0).build()
        );
        given(reactionServiceClient.getBookReactionStats(bookId, userId)).willReturn(reactionResponse);

        ApiResponse<Map<Long, SentenceReactionInfoDto>> sentenceReactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getSentenceReactions(anyList(), eq(userId))).willReturn(sentenceReactionResponse);

        // When
        BookDetailDto result = bookQueryService.getBookForViewer(bookId);

        // Then
        assertThat(result.getWriterNicknm()).isEqualTo("BookWriter");
        assertThat(result.getSentences()).hasSize(2);
        assertThat(result.getSentences().get(0).getWriterNicknm()).isEqualTo("SentenceWriter1");
        assertThat(result.getSentences().get(1).getWriterNicknm()).isEqualTo("SentenceWriter2");
    }

    // ===== getSentencesByUser() 테스트 =====

    @Test
    @DisplayName("사용자 문장 조회 - 정상 조회")
    void getSentencesByUser_Success() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setSentenceId(100L);
        sentenceDto.setWriterId(userId);

        given(bookMapper.findSentencesByWriterId(userId, 0, 10)).willReturn(List.of(sentenceDto));
        given(bookMapper.countSentencesByWriterId(userId)).willReturn(1L);

        // Feign Mock - Member Service
        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(userId)
                .userNicknm("SentenceWriter")
                .build();
        ApiResponse<MemberInfoDto> memberResponse = ApiResponse.success(memberInfo);
        given(memberServiceClient.getMemberInfo(userId)).willReturn(memberResponse);

        // Feign Mock - Reaction Service
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException());
        ApiResponse<Map<Long, SentenceReactionInfoDto>> reactionResponse = ApiResponse.success(new HashMap<>());
        given(reactionServiceClient.getSentenceReactions(anyList(), isNull())).willReturn(reactionResponse);

        // When
        SentencePageResponse response = bookQueryService.getSentencesByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getWriterNicknm()).isEqualTo("SentenceWriter");
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 문장 조회 - 빈 목록")
    void getSentencesByUser_EmptyList() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        given(bookMapper.findSentencesByWriterId(userId, 0, 10)).willReturn(Collections.emptyList());
        given(bookMapper.countSentencesByWriterId(userId)).willReturn(0L);

        // When
        SentencePageResponse response = bookQueryService.getSentencesByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("사용자 문장 조회 - Feign 실패")
    void getSentencesByUser_FeignFailure() {
        // Given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setSentenceId(100L);

        given(bookMapper.findSentencesByWriterId(userId, 0, 10)).willReturn(List.of(sentenceDto));
        given(bookMapper.countSentencesByWriterId(userId)).willReturn(1L);

        // Feign Mock - Member Service 실패
        given(memberServiceClient.getMemberInfo(userId))
                .willThrow(new RuntimeException("Service unavailable"));

        // Feign Mock - Reaction Service 실패
        securityUtil.when(SecurityUtil::getCurrentUserId).thenThrow(new RuntimeException());
        given(reactionServiceClient.getSentenceReactions(anyList(), isNull()))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        SentencePageResponse response = bookQueryService.getSentencesByUser(userId, page, size);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getWriterNicknm()).isNull();
    }
}
