package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class DiaryRepositoryTest {

  @Autowired private DiaryRepository diaryRepository;
  @Autowired private MemberRepository memberRepository;
  private MemberEntity member;
  private DiaryEntity diary;

  @BeforeEach
  void setupAll() {
    member = BaseTestObject.createMemberEntity();
    diary = BaseTestObject.createDiaryEntity(member);
    memberRepository.save(member);
    diaryRepository.save(diary);
  }

  @Test
  @DisplayName("특정 날짜에 해당되는 사용자 일기 없음.")
  void notExistDiaryByDateAndMember() {
    LocalDateTime testTime = LocalDateTime.of(2025, 10, 22, 11, 1);
    boolean exist = diaryRepository.existsByDate(member.getId(), testTime);
    assertThat(exist).as("memberID와 날짜에 해당하는 일기가 있습니다.").isFalse();
  }

  @Test
  @DisplayName("특정 날짜에 해당되는 사용자 일기가 있음.")
  void existDiaryByDateAndMember() {
    boolean exist = diaryRepository.existsByDate(member.getId(), diary.getDiaryCreatedAt());
    assertThat(exist).as("memberID와 날짜에 해당하는 일기가 없습니다.").isTrue();
  }
}
