package com.team2.nextpage.command.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 Command 서비스 (회원가입, 탈퇴 등)
 *
 * @author 김태형
 */
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 신규 회원 가입
     */
    public void signUp() {
        // Implementation
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    public void withdraw(Long userId) {
        // Implementation
    }
}
