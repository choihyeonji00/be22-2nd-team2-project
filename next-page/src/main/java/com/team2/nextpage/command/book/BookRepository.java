package com.team2.nextpage.command.book;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 소설 Command Repository
 *
 * @author 최현지
 */
public interface BookRepository extends JpaRepository<Book, Long> {
}
