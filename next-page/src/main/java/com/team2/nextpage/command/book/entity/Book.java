package com.team2.nextpage.command.book.entity;

import com.team2.nextpage.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;

/**
 * 소설(Book) 애그리거트 루트
 *
 * @author 정진호
 */
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(name = "writer_id")
    private Long writerId;

    @Column(name = "category_id")
    private String categoryId;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'WRITING'")
    private BookStatus status; // WRITING, COMPLETED

    @Column(name = "current_sequence")
    private Integer currentSequence;

    @Column(name = "max_sequence")
    private Integer maxSequence;

    @Column(name = "last_writer_user_id")
    private Long lastWriterUserId;

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Sentence> sentences = new ArrayList<>();

    // =================================================================
    // 🏛️ Domain Logic (Story Aggregate State Management)
    // Assigned to: 정진호 (Core & Architecture)
    // =================================================================

    /**
     * 문장을 작성하기 전에 도메인 규칙을 검증합니다.
     * 1. 소설이 WRITING 상태인지
     * 2. 직전 작성자가 본인이 아닌지 (연속 작성 금지)
     *
     * @param writerId 작성 시도하는 사용자 ID
     * @throws BusinessException 규칙 위반 시
     */
    public void validateWritingPossible(Long writerId) {
        validateWritingPossible(writerId, false);
    }

    /**
     * 문장을 작성하기 전에 도메인 규칙을 검증합니다. (관리자 모드 지원)
     * 1. 소설이 WRITING 상태인지
     * 2. 직전 작성자가 본인이 아닌지 (연속 작성 금지) - 관리자는 예외
     *
     * @param writerId 작성 시도하는 사용자 ID
     * @param isAdmin  관리자 여부 (true: 연속 작성 제한 우회)
     * @throws BusinessException 규칙 위반 시
     */
    public void validateWritingPossible(Long writerId, boolean isAdmin) {
        if (this.status != BookStatus.WRITING) {
            throw new BusinessException(ErrorCode.ALREADY_COMPLETED);
        }
        // 관리자가 아닌 경우에만 연속 작성 제한 적용
        if (!isAdmin && writerId.equals(this.lastWriterUserId)) {
            throw new BusinessException(ErrorCode.CONSECUTIVE_WRITING_NOT_ALLOWED);
        }
    }

    /**
     * 문장 작성이 완료된 후 소설의 상태(순서, 마지막 작성자)를 업데이트합니다.
     * 또한 최대 시퀀스에 도달하면 소설을 완결 처리합니다.
     *
     * @param writerId 작성 완료한 사용자 ID
     */
    public void updateStateAfterWriting(Long writerId) {
        this.lastWriterUserId = writerId;
        this.currentSequence++;

        // 완결 조건 체크
        if (this.currentSequence > this.maxSequence) {
            completeStory();
        }
    }

    /**
     * 소설을 완결 상태로 변경합니다.
     */
    private void completeStory() {
        this.status = BookStatus.COMPLETED;
    }

    /**
     * 소설 생성 시 초기화 (팩토리 메서드나 생성자에서 호출)
     */
    public void init(Long writerId, String categoryId, String title, Integer maxSequence) {
        this.writerId = writerId;
        this.categoryId = categoryId;
        this.title = title;
        this.maxSequence = maxSequence;
        this.status = BookStatus.WRITING;
        this.currentSequence = 1;
        this.lastWriterUserId = null; // 아직 작성자 없음 (첫 문장은 별도 로직 따름 or 첫 문장 작성시 갱신)
    }
}
