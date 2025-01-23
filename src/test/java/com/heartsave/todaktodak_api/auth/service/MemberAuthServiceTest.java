package com.heartsave.todaktodak_api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.NicknameCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.domain.auth.exception.AuthException;
import com.heartsave.todaktodak_api.domain.auth.service.AuthService;
import com.heartsave.todaktodak_api.common.exception.errorspec.auth.AuthErrorSpec;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {
  @Mock private MemberRepository memberRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthService authService;

  private static final String LOGIN_ID = "TEST_LOGIN";
  private static final String NICKNAME = "TEST_NICKNAME";
  private static final SignUpRequest signUpDto =
      new SignUpRequest("test@test.com", "unique", "todak", "todak!");

  private static final MemberEntity memberEntity =
      MemberEntity.builder().loginId("TEST_LOGIN").nickname("TEST_NICKNAME").build();

  @Test
  @DisplayName("로그인 아이디 중복 확인")
  void isDuplicatedLoginId() {
    // given
    when(memberRepository.findMemberEntityByLoginId(anyString()))
        .thenReturn(Optional.of(memberEntity));

    // when
    LoginIdCheckRequest dto = new LoginIdCheckRequest(LOGIN_ID);
    boolean isDuplicated = authService.isDuplicatedLoginId(dto.loginId());

    // then
    verify(memberRepository, times(1)).findMemberEntityByLoginId(anyString());
    assertThat(isDuplicated).isEqualTo(true);
  }

  @Test
  @DisplayName("닉네임 중복 확인")
  void checkNicknameDuplicationTest() {
    // given
    when(memberRepository.findMemberEntityByNickname(anyString()))
        .thenReturn(Optional.of(memberEntity));

    // when
    NicknameCheckRequest dto = new NicknameCheckRequest(NICKNAME);
    boolean isDuplicated = authService.isDuplicatedNickname(dto.nickname());

    // then
    verify(memberRepository, times(1)).findMemberEntityByNickname(anyString());
    assertThat(isDuplicated).isEqualTo(true);
  }

  @Test
  @DisplayName("회원가입 성공")
  void signUpSuccessTest() {

    // given
    when(memberRepository.findMemberEntityByEmail(anyString())).thenReturn(Optional.empty());
    when(memberRepository.findMemberEntityByNickname(anyString())).thenReturn(Optional.empty());
    when(memberRepository.findMemberEntityByLoginId(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("!kadot");

    // when
    assertDoesNotThrow(() -> authService.signUp(signUpDto));

    // then
    verify(memberRepository).save(any(MemberEntity.class));
    verify(passwordEncoder).encode(signUpDto.password());
  }

  @Test
  @DisplayName("중복된 이메일에 대한 회원가입 실패")
  void signUpDuplicatedEmailFailTest() {
    // given
    when(memberRepository.findMemberEntityByEmail(signUpDto.email()))
        .thenReturn(Optional.of(MemberEntity.builder().build()));

    // when + then
    AuthException exception =
        assertThrows(AuthException.class, () -> authService.signUp(signUpDto));
    assertEquals(AuthErrorSpec.DUPLICATED_INFORMATION, exception.getErrorSpec());
    verify(memberRepository, never()).save(any());
  }
}
