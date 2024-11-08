package com.heartsave.todaktodak_api.common.security.component;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class TodakUserDetailsServiceTest {
  @Mock private MemberRepository memberRepository;
  @InjectMocks private TodakUserDetailsService userDetailsService;

  private MemberEntity member;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
  }

  @Test
  @DisplayName("로그인 과정에서 DB의 회원 조회 성공")
  void loadUserByUsername_success() {
    // given
    member.updateRole(TodakRole.ROLE_USER.name());
    doReturn(Optional.of(member))
        .when(memberRepository)
        .findMemberEntityByLoginId(eq(member.getLoginId()));

    // when
    TodakUser userDetails = (TodakUser) userDetailsService.loadUserByUsername(member.getLoginId());

    // then
    verify(memberRepository).findMemberEntityByLoginId(eq(member.getLoginId()));

    assertThat(userDetails.getId()).isEqualTo(member.getId());
    assertThat(userDetails.getUsername()).isEqualTo(member.getLoginId());
    assertThat(userDetails.getRole()).isEqualTo(member.getRole().name());
  }

  @Test
  @DisplayName("로그인 과정에서 DB의 회원 조회 실패 - 존재하지 않는 회원")
  void loadUserByUsername_fail_notFound() {
    // given
    doThrow(new UsernameNotFoundException("USER NOT FOUND"))
        .when(memberRepository)
        .findMemberEntityByLoginId(anyString());

    // when + then
    assertThrows(
        UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(anyString()));
  }
}
