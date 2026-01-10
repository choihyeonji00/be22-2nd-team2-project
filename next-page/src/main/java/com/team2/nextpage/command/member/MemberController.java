package com.team2.nextpage.command.member;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 Command 컨트롤러
 *
 * @author 김태형
 */
@RestController
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 회원가입 API
     */
    @PostMapping("/signup")
    public void signUp() {
        // Implementation
    }
}
