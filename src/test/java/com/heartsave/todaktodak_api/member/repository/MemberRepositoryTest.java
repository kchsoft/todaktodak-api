package com.heartsave.todaktodak_api.member.repository;

import static org.assertj.core.api.Assertions.*;

import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest // 기본적으로 h2로 테스트하게 돼있음
@ActiveProfiles("test")
class MemberRepositoryTest {
  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("로그인 아이디로 회원 조회")
  void getMemberByLoginIdTest() {

    // Given
    String loginId = "TEST_ID1";
    MemberEntity memberEntity = MemberEntity.builder().loginId(loginId).build();
    memberRepository.save(memberEntity);

    // When
    var retrievedMember = memberRepository.findMemberByLoginId(loginId);

    // Then
    assertThat(retrievedMember.isPresent()).isEqualTo(true);
  }

  @Test
  @DisplayName("닉네임으로 회원 조회")
  void getMemberByNicknameTest() {

    // Given
    String nickname = "TEST_NICKNAME";
    MemberEntity memberEntity = MemberEntity.builder().nickname(nickname).build();
    memberRepository.save(memberEntity);

    // When
    var retrievedMember = memberRepository.findMemberByNickname(nickname);

    // Then
    assertThat(retrievedMember.isPresent()).isEqualTo(true);
  }
}
