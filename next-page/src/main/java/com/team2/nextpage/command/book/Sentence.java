package com.team2.nextpage.command.book;

import com.team2.nextpage.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 문장(Sentence) 엔티티
 *
 * @author 최현지
 */
@Entity
@Table(name = "sentences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sentence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sentenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String content;
    private Integer sequenceNo;
}
