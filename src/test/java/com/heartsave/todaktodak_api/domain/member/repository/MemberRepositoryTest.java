package com.heartsave.todaktodak_api.domain.member.repository;

import static org.assertj.core.api.Assertions.*;

import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest // 기본적으로 h2로 테스트하게 돼있음
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
    var retrievedMember = memberRepository.findMemberEntityByLoginId(loginId);

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
    var retrievedMember = memberRepository.findMemberEntityByNickname(nickname);

    // Then
    assertThat(retrievedMember.isPresent()).isEqualTo(true);
  }

  @Test
  @DisplayName("이메일로 회원 조회")
  void getMemberByEmailTest() {

    // Given
    String email = "todak@todak.com";
    MemberEntity memberEntity = MemberEntity.builder().loginId(email).build();
    memberRepository.save(memberEntity);

    // When
    var retrievedMember = memberRepository.findMemberEntityByLoginId(email);

    // Then
    assertThat(retrievedMember.isPresent()).isEqualTo(true);
  }

  @Test
  @DisplayName("중복 이메일 확인")
  void getDupliactedEmailTest() {
    // Given
    MemberEntity member = memberRepository.save(BaseTestObject.createMemberNoId());

    // When
    boolean isExisted = memberRepository.existsByEmail(member.getEmail());

    // Then
    assertThat(isExisted).isEqualTo(true);
  }

  @Test
  @DisplayName("회원 프로필 조회")
  void getMemberProfileProjectionTest() {
    // Given
    MemberEntity member = memberRepository.save(BaseTestObject.createMemberNoId());

    // When
    var memberProfile = memberRepository.findProjectedById(member.getId()).orElse(null);

    // Then
    assertThat(memberProfile).isNotNull();
    assertThat(memberProfile.getNickname()).isEqualTo(member.getNickname());
    assertThat(memberProfile.getEmail()).isEqualTo(member.getEmail());
  }
}
