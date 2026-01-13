package com.team2.nextpage.command.member.service;

import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.entity.UserRole;
import com.team2.nextpage.command.member.entity.UserStatus;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 Command 서비스 (회원가입, 탈퇴 등)
 *
 * @author 김태형
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 일반 사용자 등록
   * 
   * @param memberCreateRequest 회원가입 요청 DTO
   * @throws BusinessException 이미 존재하는 이메일일 경우
   */
  public void registUser(SignUpRequest memberCreateRequest) {
    validateDuplicateEmail(memberCreateRequest.getUserEmail());

    Member member = Member.builder()
        .userEmail(memberCreateRequest.getUserEmail())
        .userPw(passwordEncoder.encode(memberCreateRequest.getUserPw()))
        .userNicknm(memberCreateRequest.getUserNicknm())
        .userRole(UserRole.USER)
        .userStatus(UserStatus.ACTIVE)
        .build();

    memberRepository.save(member);
  }

  /**
   * 관리자 등록
   * 
   * @param memberCreateRequest 회원가입 요청 DTO
   * @throws BusinessException 이미 존재하는 이메일일 경우
   */
  public void registAdmin(SignUpRequest memberCreateRequest) {
    validateDuplicateEmail(memberCreateRequest.getUserEmail());

    Member member = Member.builder()
        .userEmail(memberCreateRequest.getUserEmail())
        .userPw(passwordEncoder.encode(memberCreateRequest.getUserPw()))
        .userNicknm(memberCreateRequest.getUserNicknm())
        .userRole(UserRole.ADMIN)
        .userStatus(UserStatus.ACTIVE)
        .build();

    memberRepository.save(member);
  }

  /**
   * 회원 탈퇴 (Soft Delete)
   * 현재 로그인한 사용자를 탈퇴 처리합니다.
   * 
   * @throws BusinessException 회원 정보를 찾을 수 없는 경우
   */
  public void withdraw() {
    String userEmail = SecurityUtil.getCurrentUserEmail();

    Member member = memberRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    // Soft Delete 수행
    memberRepository.delete(member);
  }

  /**
   * 이메일 중복 검증
   * 
   * @param email 검증할 이메일
   * @throws BusinessException 이미 존재하는 이메일일 경우
   */
  private void validateDuplicateEmail(String email) {
    if (memberRepository.findByUserEmail(email).isPresent()) {
      throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
    }
  }
}
