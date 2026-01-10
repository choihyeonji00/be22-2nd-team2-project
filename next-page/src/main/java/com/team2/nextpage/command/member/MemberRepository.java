package com.team2.nextpage.command.member;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 Command Repository
 *
 * @author 김태형
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
}
