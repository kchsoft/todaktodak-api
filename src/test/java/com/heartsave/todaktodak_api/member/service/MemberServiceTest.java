package com.heartsave.todaktodak_api.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.member.dto.request.NicknameUpdateRequest;
import com.heartsave.todaktodak_api.member.dto.response.MemberProfileResponse;
import com.heartsave.todaktodak_api.member.dto.response.NicknameUpdateResponse;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.entity.projection.MemberProfileProjection;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
final class MemberServiceTest {
  @Mock private MemberRepository memberRepository;
  @Mock private S3FileStorageService s3Service;
  @InjectMocks private MemberService memberService;
  private MemberEntity member;
  private MemberProfileProjection memberProfileProjection;
  private TodakUser principal;

  @BeforeEach
  void setup() {
    member = BaseTestEntity.createMember();
    memberProfileProjection =
        new MemberProfileProjection() {
          @Override
          public String getNickname() {
            return member.getNickname();
          }

          @Override
          public String getEmail() {
            return member.getEmail();
          }

          @Override
          public String getCharacterImageUrl() {
            return member.getCharacterImageUrl();
          }
        };
    principal = mock(TodakUser.class);
    when(principal.getId()).thenReturn(member.getId());
  }

  @Test
  @DisplayName("닉네임 변경 성공")
  void updateNicknameTest() {
    // given
    String newNickname = new StringBuilder(member.getNickname()).reverse().toString();
    NicknameUpdateRequest request = new NicknameUpdateRequest(newNickname);
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));

    // when
    NicknameUpdateResponse response = memberService.updateNickname(principal, request);

    // then
    assertThat(response.nickname()).isEqualTo(newNickname);
  }

  @Test
  @DisplayName("회원 프로필 조회")
  void getMemberProfileTest() {
    // given
    when(memberRepository.findProjectedById(anyLong()))
        .thenReturn(Optional.of(memberProfileProjection));

    // when
    MemberProfileResponse response = memberService.getMemberProfileById(principal);

    // then
    assertThat(response.nickname()).isEqualTo(member.getNickname());
    assertThat(response.email()).isEqualTo(member.getEmail());
    assertThat(response.characterImageUrl()).isNotEqualTo(member.getCharacterImageUrl());
  }
}
