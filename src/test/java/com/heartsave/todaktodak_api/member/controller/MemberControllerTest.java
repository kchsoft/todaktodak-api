package com.heartsave.todaktodak_api.member.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.config.TestSecurityConfig;
import com.heartsave.todaktodak_api.member.dto.request.NicknameUpdateRequest;
import com.heartsave.todaktodak_api.member.dto.response.MemberProfileResponse;
import com.heartsave.todaktodak_api.member.dto.response.NicknameUpdateResponse;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = MemberController.class)
@Import(TestSecurityConfig.class)
final class MemberControllerTest {
  @Autowired WebApplicationContext context;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private MemberService memberService;
  private MockMvc mockMvc;

  private static final String INVALID_NICKNAME_OVER_LENGTH =
      "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .defaultRequest(
                patch("/**").contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .alwaysDo(print())
            .build();
  }

  @Test
  @DisplayName("닉네임 변경 요청 성공")
  @WithMockTodakUser
  void updateNickname_success_200Test() throws Exception {
    // given
    final String NEW_NICKNAME = "NEW_NICKNAME";
    NicknameUpdateRequest request = new NicknameUpdateRequest(NEW_NICKNAME);
    NicknameUpdateResponse response =
        NicknameUpdateResponse.builder().nickname(NEW_NICKNAME).build();

    // when
    doReturn(response)
        .when(memberService)
        .updateNickname(anyLong(), any(NicknameUpdateRequest.class));

    // then
    mockMvc
        .perform(
            patch("/api/v1/member/nickname")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value(NEW_NICKNAME))
        .andReturn();
    verify(memberService, times(1)).updateNickname(anyLong(), any(NicknameUpdateRequest.class));
  }

  @ParameterizedTest
  @WithMockTodakUser
  @DisplayName("닉네임 변경 요청 실패 - 유효하지 않은 닉네임")
  @ValueSource(strings = {"", " ", INVALID_NICKNAME_OVER_LENGTH})
  void updateNickname_fail_400Test(String invalidNickname) throws Exception {
    // given
    var request = new NicknameUpdateRequest(invalidNickname);

    // then
    mockMvc
        .perform(patch("/api/v1/member/nickname").content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"));

    verify(memberService, never()).updateNickname(anyLong(), any(NicknameUpdateRequest.class));
  }

  @Test
  @DisplayName("닉네임 변경 요청 실패 - 존재하지 않는 회원")
  @WithMockTodakUser
  void updateNickname_fail_404Test() throws Exception {
    // given
    final String NEW_NICKNAME = "NEW_NICKNAME";
    NicknameUpdateRequest request = new NicknameUpdateRequest(NEW_NICKNAME);
    Long NON_EXISTED_ID =
        ((TodakUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

    // when
    doThrow(new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, NON_EXISTED_ID))
        .when(memberService)
        .updateNickname(anyLong(), any(NicknameUpdateRequest.class));

    // then
    mockMvc
        .perform(patch("/api/v1/member/nickname").content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value(MemberErrorSpec.NOT_FOUND.name()));

    verify(memberService, times(1)).updateNickname(anyLong(), any(NicknameUpdateRequest.class));
  }

  @Test
  @DisplayName("회원 프로필 조회 성공")
  @WithMockTodakUser
  void getMemberProfile_success_200Test() throws Exception {
    // given
    MemberProfileResponse response =
        MemberProfileResponse.builder()
            .nickname("testUser")
            .email("test@example.com")
            .characterImageUrl("presigned-url")
            .build();

    // when
    doReturn(response).when(memberService).getMemberProfile(anyLong());

    // then
    mockMvc
        .perform(get("/api/v1/member/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value("testUser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.characterImageUrl").value("presigned-url"));

    verify(memberService, times(1)).getMemberProfile(anyLong());
  }

  @Test
  @DisplayName("회원 프로필 조회 실패 - 존재하지 않는 회원")
  @WithMockTodakUser
  void getMemberProfile_fail_404Test() throws Exception {
    // given
    TodakUser mockUser =
        (TodakUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // when
    doThrow(new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, mockUser.getId()))
        .when(memberService)
        .getMemberProfile(anyLong());

    // then
    mockMvc
        .perform(get("/api/v1/member/profile"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value(MemberErrorSpec.NOT_FOUND.name()));

    verify(memberService, times(1)).getMemberProfile(anyLong());
  }

  @Test
  @DisplayName("회원 탈퇴 성공")
  @WithMockTodakUser
  void deactivateMember_success_204Test() throws Exception {
    // when
    doNothing().when(memberService).deactivate(any(HttpServletResponse.class), anyLong());

    // then
    mockMvc.perform(post("/api/v1/member/deactivate")).andExpect(status().isNoContent());
    verify(memberService, times(1)).deactivate(any(HttpServletResponse.class), anyLong());
  }

  @Test
  @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
  @WithMockTodakUser
  void deactivateMember_fail_404Test() throws Exception {
    // given
    TodakUser mockUser =
        (TodakUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // when
    doThrow(new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, mockUser.getId()))
        .when(memberService)
        .deactivate(any(HttpServletResponse.class), anyLong());

    // then
    mockMvc
        .perform(post("/api/v1/member/deactivate"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value(MemberErrorSpec.NOT_FOUND.name()));
    verify(memberService, times(1)).deactivate(any(HttpServletResponse.class), anyLong());
  }
}
