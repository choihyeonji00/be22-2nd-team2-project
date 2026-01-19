package com.team2.memberservice.command.member.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.memberservice.command.member.dto.request.SignUpRequest;
import com.team2.memberservice.command.member.entity.Member;
import com.team2.memberservice.command.member.entity.UserRole;
import com.team2.memberservice.command.member.entity.UserStatus;
import com.team2.memberservice.command.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import com.team2.commonmodule.util.SecurityUtil;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

/**
 * MemberService 단위 테스트
 * Mockito를 사용하여 의존성을 격리한 순수한 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private SignUpRequest signUpRequest;
    private Member testMember;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest("newuser@example.com", "password123!", "새유저");

        testMember = Member.builder()
                .userId(1L)
                .userEmail("existing@example.com")
                .userPw("$2a$10$encoded.password")
                .userNicknm("기존유저")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("일반 사용자 등록 성공")
    void registUserSuccess() {
        // Given
        given(memberRepository.findByUserEmail(signUpRequest.getUserEmail()))
                .willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm(signUpRequest.getUserNicknm()))
                .willReturn(false);
        given(passwordEncoder.encode(signUpRequest.getUserPw()))
                .willReturn("$2a$10$encoded.password.hash");
        given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        memberService.registUser(signUpRequest);

        // Then
        then(memberRepository).should(times(1)).findByUserEmail(signUpRequest.getUserEmail());
        then(memberRepository).should(times(1)).existsByUserNicknm(signUpRequest.getUserNicknm());
        then(passwordEncoder).should(times(1)).encode(signUpRequest.getUserPw());
        then(memberRepository).should(times(1)).save(argThat(member ->
                member.getUserEmail().equals(signUpRequest.getUserEmail()) &&
                        member.getUserNicknm().equals(signUpRequest.getUserNicknm()) &&
                        member.getUserRole() == UserRole.USER &&
                        member.getUserStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("일반 사용자 등록 실패 - 이메일 중복")
    void registUserFail_DuplicateEmail() {
        // Given
        given(memberRepository.findByUserEmail(signUpRequest.getUserEmail()))
                .willReturn(Optional.of(testMember));

        // When & Then
        assertThatThrownBy(() -> memberService.registUser(signUpRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        // Verify
        then(memberRepository).should(never()).save(any(Member.class));
    }

    @Test
    @DisplayName("일반 사용자 등록 실패 - 닉네임 중복")
    void registUserFail_DuplicateNickname() {
        // Given
        given(memberRepository.findByUserEmail(signUpRequest.getUserEmail()))
                .willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm(signUpRequest.getUserNicknm()))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.registUser(signUpRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

        // Verify
        then(memberRepository).should(never()).save(any(Member.class));
    }

    @Test
    @DisplayName("관리자 등록 성공 - PENDING 상태로 생성")
    void registAdminSuccess() {
        // Given
        given(memberRepository.findByUserEmail(signUpRequest.getUserEmail()))
                .willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm(signUpRequest.getUserNicknm()))
                .willReturn(false);
        given(passwordEncoder.encode(signUpRequest.getUserPw()))
                .willReturn("$2a$10$encoded.password.hash");
        given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        memberService.registAdmin(signUpRequest);

        // Then
        then(memberRepository).should(times(1)).save(argThat(member ->
                member.getUserEmail().equals(signUpRequest.getUserEmail()) &&
                        member.getUserRole() == UserRole.ADMIN &&
                        member.getUserStatus() == UserStatus.PENDING
        ));
    }

    @Test
    @DisplayName("관리자 등록 실패 - 이메일 중복")
    void registAdminFail_DuplicateEmail() {
        // Given
        given(memberRepository.findByUserEmail(signUpRequest.getUserEmail()))
                .willReturn(Optional.of(testMember));

        // When & Then
        assertThatThrownBy(() -> memberService.registAdmin(signUpRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("관리자 승인 성공")
    void approveAdminSuccess() {
        // Given
        Member pendingAdmin = Member.builder()
                .userId(2L)
                .userEmail("admin@example.com")
                .userPw("$2a$10$encoded.password")
                .userNicknm("관리자후보")
                .userRole(UserRole.ADMIN)
                .userStatus(UserStatus.PENDING)
                .build();

        given(memberRepository.findById(2L))
                .willReturn(Optional.of(pendingAdmin));

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::isAdmin)
                    .thenReturn(true);

            // When
            memberService.approveAdmin(2L);

            // Then
            assertThat(pendingAdmin.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
            then(memberRepository).should(times(1)).findById(2L);
        }
    }

    @Test
    @DisplayName("관리자 승인 실패 - 관리자 권한 없음")
    void approveAdminFail_AccessDenied() {
        // Given
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::isAdmin)
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> memberService.approveAdmin(2L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

            // Verify
            then(memberRepository).should(never()).findById(anyLong());
        }
    }

    @Test
    @DisplayName("관리자 승인 실패 - 사용자를 찾을 수 없음")
    void approveAdminFail_MemberNotFound() {
        // Given
        given(memberRepository.findById(999L))
                .willReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::isAdmin)
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> memberService.approveAdmin(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - Soft Delete")
    void withdrawSuccess() {
        // Given
        String currentUserEmail = "existing@example.com";

        given(memberRepository.findByUserEmail(currentUserEmail))
                .willReturn(Optional.of(testMember));

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            // When
            memberService.withdraw();

            // Then
            then(memberRepository).should(times(1)).findByUserEmail(currentUserEmail);
            then(memberRepository).should(times(1)).delete(testMember);
        }
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 사용자를 찾을 수 없음")
    void withdrawFail_MemberNotFound() {
        // Given
        String currentUserEmail = "notfound@example.com";

        given(memberRepository.findByUserEmail(currentUserEmail))
                .willReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            // When & Then
            assertThatThrownBy(() -> memberService.withdraw())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            // Verify
            then(memberRepository).should(never()).delete(any(Member.class));
        }
    }

    @Test
    @DisplayName("이메일 중복 검증 - 중복되지 않음")
    void validateDuplicateEmail_NotDuplicated() {
        // Given
        String email = "unique@example.com";
        given(memberRepository.findByUserEmail(email))
                .willReturn(Optional.empty());

        // When & Then
        assertThatCode(() -> memberService.validateDuplicateEmail(email))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이메일 중복 검증 - 중복됨")
    void validateDuplicateEmail_Duplicated() {
        // Given
        String email = "existing@example.com";
        given(memberRepository.findByUserEmail(email))
                .willReturn(Optional.of(testMember));

        // When & Then
        assertThatThrownBy(() -> memberService.validateDuplicateEmail(email))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("닉네임 중복 검증 - 중복되지 않음")
    void validateDuplicateNicknm_NotDuplicated() {
        // Given
        String nickname = "유니크닉네임";
        given(memberRepository.existsByUserNicknm(nickname))
                .willReturn(false);

        // When & Then
        assertThatCode(() -> memberService.validateDuplicateNicknm(nickname))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("닉네임 중복 검증 - 중복됨")
    void validateDuplicateNicknm_Duplicated() {
        // Given
        String nickname = "기존유저";
        given(memberRepository.existsByUserNicknm(nickname))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.validateDuplicateNicknm(nickname))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("관리자에 의한 회원 탈퇴 성공")
    void withdrawByAdminSuccess() {
        // Given
        Long targetUserId = 1L;

        given(memberRepository.findById(targetUserId))
                .willReturn(Optional.of(testMember));

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::isAdmin)
                    .thenReturn(true);

            // When
            memberService.withdrawByAdmin(targetUserId);

            // Then
            then(memberRepository).should(times(1)).findById(targetUserId);
            then(memberRepository).should(times(1)).delete(testMember);
        }
    }

    @Test
    @DisplayName("관리자에 의한 회원 탈퇴 실패 - 관리자 권한 없음")
    void withdrawByAdminFail_AccessDenied() {
        // Given
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::isAdmin)
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> memberService.withdrawByAdmin(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

            // Verify
            then(memberRepository).should(never()).findById(anyLong());
            then(memberRepository).should(never()).delete(any(Member.class));
        }
    }

    @Test
    @DisplayName("관리자에 의한 회원 탈퇴 실패 - 사용자를 찾을 수 없음")
    void withdrawByAdminFail_MemberNotFound() {
        // Given
        Long nonExistentUserId = 999L;

        given(memberRepository.findById(nonExistentUserId))
                .willReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {

            securityUtil.when(SecurityUtil::isAdmin)
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> memberService.withdrawByAdmin(nonExistentUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            // Verify
            then(memberRepository).should(never()).delete(any(Member.class));
        }
    }
}
