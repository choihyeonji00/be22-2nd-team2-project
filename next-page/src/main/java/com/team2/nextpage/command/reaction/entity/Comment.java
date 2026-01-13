package com.team2.nextpage.command.reaction.entity;

import com.team2.nextpage.common.entity.BaseEntity;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 엔티티
 *
 * @author 정병진
 */
@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long commentId;

  @Column(nullable = false)
  private Long bookId;

  @Column(nullable = false)
  private Long writerId;

  @Column(nullable = false)
  private String content;

  @Builder
  public Comment(Long bookId, Long writerId, String content) {
    this.bookId = bookId;
    this.writerId = writerId;
    this.content = content;
  }

  /**
   * 댓글 내용 수정
   * 엔티티 스스로 데이터 무결성 검증하고 상태를 변경
   *
   * @param newContent 수정할 새로운 댓글 내용(Null 또는 빈 문자열 불가)
   * @throws BusinessException 내용이 비어있는 경우
   */
  public void updateContent(String newContent) {
    if (newContent == null || newContent.isBlank()) {
      throw new BusinessException(ErrorCode.EMPTY_CONTENT);
    }
    this.content = newContent;
  }
}
