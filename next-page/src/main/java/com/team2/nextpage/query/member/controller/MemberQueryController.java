package com.team2.nextpage.query.member.controller;

import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.response.ApiResponse;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.query.member.dto.response.MemberDto;
import com.team2.nextpage.query.member.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 Query 컨트롤러
 *
 * @author 김태형
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberQueryController {

  private final MemberQueryService memberQueryService;

  /**
   * 마이페이지 조회 API
   * GET /api/members/me
   *
   * @return 현재 로그인한 사용자 정보 (활동 통계 포함)
   * @throws BusinessException 로그인하지 않은 경우
   */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MemberDto>> getMyInfo() {
    String userEmail = SecurityUtil.getCurrentUserEmail();

    if (userEmail == null) {
      throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    MemberDto memberInfo = memberQueryService.getMyPage(userEmail);
    return ResponseEntity.ok(ApiResponse.success(memberInfo));
  }
}
