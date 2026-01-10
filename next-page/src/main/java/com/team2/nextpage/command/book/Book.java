package com.team2.nextpage.command.book;

import com.team2.nextpage.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 소설(Book) 애그리거트 루트
 *
 * @author 최현지
 */
@Entity
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String title;

    // 상태 관리 (jeong jin-ho architected logic, implemented here)
    private String status;
    private Integer currentSequence;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Sentence> sentences = new ArrayList<>();

    // Domain Logic methods...
}
