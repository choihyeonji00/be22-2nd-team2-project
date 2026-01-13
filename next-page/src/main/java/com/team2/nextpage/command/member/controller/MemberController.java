package com.team2.nextpage.command.member.controller;

import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.service.MemberService;
import com.team2.nextpage.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 Command 컨트롤러
 *
 * @author 김태형
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  // 1. 회원가입
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<String>> signup(@RequestBody @Valid SignUpRequest memberCreateRequest) {
    memberService.registUser(memberCreateRequest); // 서비스에게 가입 처리 위임
    return ResponseEntity.ok(ApiResponse.success("회원가입 성공")); // 성공 응답 반환
  }

  // 2. 관리자
  @PostMapping("/admin")
  public ResponseEntity<ApiResponse<String>> signupAdmin(@RequestBody @Valid SignUpRequest memberCreateRequest) {
    memberService.registAdmin(memberCreateRequest);
    return ResponseEntity.ok(ApiResponse.success("관리자 가입 성공"));
  }

  // 3. 회원 탈퇴 (본인)
  @DeleteMapping("/withdraw")
  public ResponseEntity<ApiResponse<Void>> withdraw() {
    memberService.withdraw();
    return ResponseEntity.ok(ApiResponse.success());
  }

  // 4. 관리자에 의한 회원 강제 탈퇴
  /**
   * 관리자 전용 - 특정 회원 강제 탈퇴
   * DELETE /api/auth/admin/users/{userId}
   * 
   * @param userId 탈퇴시킬 회원 ID
   * @return 성공 응답
   */
  @DeleteMapping("/admin/users/{userId}")
  public ResponseEntity<ApiResponse<Void>> withdrawByAdmin(@PathVariable Long userId) {
    memberService.withdrawByAdmin(userId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  // 5. 이메일 중복 체크 (실시간 검증용)
  @GetMapping("/check-email")
  public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam String email) {
    memberService.validateDuplicateEmail(email); // 중복이면 예외 발생
    return ResponseEntity.ok(ApiResponse.success()); // 중복 아니면 성공 응답
  }

  // 6. 닉네임 중복 체크 (실시간 검증용)
  @GetMapping("/check-nickname")
  public ResponseEntity<ApiResponse<Void>> checkNickname(@RequestParam String nickname) {
    memberService.validateDuplicateNicknm(nickname); // 중복이면 예외 발생
    return ResponseEntity.ok(ApiResponse.success()); // 중복 아니면 성공 응답
  }
}
