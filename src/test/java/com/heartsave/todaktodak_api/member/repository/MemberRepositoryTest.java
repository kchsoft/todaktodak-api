package com.heartsave.todaktodak_api.member.repository;

import com.heartsave.todaktodak_api.member.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("연동 확인을 위한 회원 저장")
    void testSaveMember() {
        // Given
        Member member = Member.builder().email("test@test.com").build();

        // When
        Member retrievedMember = memberRepository.save(member);

        // Then
        assertThat(retrievedMember.getEmail()).isEqualTo(member.getEmail());
    }
}