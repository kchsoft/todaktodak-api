package com.heartsave.todaktodak_api.common.security.component.jwt;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.JwtUtils;
import com.heartsave.todaktodak_api.common.security.util.UtilConfig;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtValidationFilterTest {

  @Mock private AuthenticationEntryPoint authenticationEntryPoint;
  private JwtValidationFilter jwtValidationFilter;
  private TodakUser user;

  @BeforeAll
  static void beforeAll() {
    UtilConfig.jwtUtilSetup();
  }

  @BeforeEach
  void setup() {
    var member = BaseTestObject.createMember();
    user =
        TodakUser.builder()
            .id(member.getId())
            .username(member.getLoginId())
            .role(TodakRole.ROLE_USER.name())
            .build();
    jwtValidationFilter = new JwtValidationFilter(authenticationEntryPoint);
  }

  @Test
  @DisplayName("유효한 토큰 인증 성공")
  void authenticate_validTokenTest() throws ServletException, IOException {
    // given
    ReflectionTestUtils.setField(JwtUtils.class, "ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND", 3600L);
    String validToken = JwtUtils.issueToken(user, ACCESS_TYPE);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HEADER_KEY, TOKEN_PREFIX + validToken);

    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    // when
    jwtValidationFilter.doFilterInternal(request, response, filterChain);

    // then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isInstanceOf(TodakUser.class);
    assertThat(((TodakUser) authentication.getPrincipal()).getUsername())
        .isEqualTo(user.getUsername());
    assertThat(filterChain.getRequest()).isNotNull(); // 다음 필터로 진행되었는지 확인
  }

  @Test
  @DisplayName("만료된 토큰 인증 실패")
  void authenticate_expiredTokenTest() throws ServletException, IOException {
    // given
    ReflectionTestUtils.setField(JwtUtils.class, "ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND", -1000L);
    String expiredToken = JwtUtils.issueToken(user, ACCESS_TYPE);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HEADER_KEY, TOKEN_PREFIX + expiredToken);

    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    // when
    jwtValidationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(authenticationEntryPoint).commence(any(), any(), any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("유효하지 않은 토큰 인증 실패")
  void authenticate_invalidTokenTest() throws ServletException, IOException {
    // given
    String invalidToken = "invalid.token.string";

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HEADER_KEY, TOKEN_PREFIX + invalidToken);

    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    // when
    jwtValidationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(authenticationEntryPoint).commence(any(), any(), any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
