package com.team2.nextpage.command.reaction;

import com.team2.nextpage.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 댓글 엔티티
 *
 * @author 정병진
 */
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private Long bookId;
    private Long writerId;
    private String content;

    // modification methods
}
