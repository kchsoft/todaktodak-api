package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@Slf4j
@DataJpaTest
public class PublicDiaryRepositoryTest {

  @Autowired private PublicDiaryRepository publicDiaryRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private DiaryRepository diaryRepository;

  private MemberEntity member;
  private DiaryEntity diary;
  private PublicDiaryEntity publicDiary;
  private final String PUBLIC_CONTENT = "테스트 공개 일기 내용";

  @BeforeEach
  void setup() {
    member = BaseTestEntity.createMemberNoId();
    diary = BaseTestEntity.createDiaryNoIdWithMember(member);

    memberRepository.save(member);
    diaryRepository.save(diary);

    publicDiary =
        PublicDiaryEntity.builder()
            .diaryEntity(diary)
            .memberEntity(member)
            .publicContent(PUBLIC_CONTENT)
            .build();
  }

  @Test
  @DisplayName("공개 일기 작성 성공")
  void writePublicDiary_Success() {
    PublicDiaryEntity savedPublicDiary = publicDiaryRepository.save(publicDiary);

    assertThat(savedPublicDiary).as("저장된 공개 일기가 null이 아니어야 합니다").isNotNull();
    assertThat(savedPublicDiary.getPublicContent())
        .as("저장된 공개 일기의 내용이 원본 내용과 일치해야 합니다")
        .isEqualTo(PUBLIC_CONTENT);
    assertThat(savedPublicDiary.getMemberEntity().getId())
        .as("저장된 공개 일기의 작성자 ID가 원본 작성자 ID와 일치해야 합니다")
        .isEqualTo(member.getId());
    assertThat(savedPublicDiary.getDiaryEntity().getId())
        .as("저장된 공개 일기의 원본 일기 ID가 원본 일기 ID와 일치해야 합니다")
        .isEqualTo(diary.getId());
  }

  @Test
  @DisplayName("존재하지 않는 일기에 대한 공개글 작성 시도")
  void writePublicDiary_DiaryNotFound() {
    Long nonExistentDiaryId = 999L;

    DiaryNotFoundException exception =
        assertThrows(
            DiaryNotFoundException.class,
            () -> {
              DiaryEntity nonExistentDiary =
                  diaryRepository
                      .findById(nonExistentDiaryId)
                      .orElseThrow(
                          () ->
                              new DiaryNotFoundException(
                                  DiaryErrorSpec.DIARY_NOT_FOUND,
                                  member.getId(),
                                  nonExistentDiaryId));
            });

    assertThat(exception.getErrorSpec())
        .as("존재하지 않는 일기에 대한 접근 시 DIARY_NOT_FOUND 에러가 발생해야 합니다")
        .isEqualTo(DiaryErrorSpec.DIARY_NOT_FOUND);
  }

  @Test
  @DisplayName("공개 일기 조회")
  void findPublicDiary_Success() {
    PublicDiaryEntity savedPublicDiary = publicDiaryRepository.save(publicDiary);

    PublicDiaryEntity foundPublicDiary =
        publicDiaryRepository.findById(savedPublicDiary.getId()).orElseThrow();

    assertThat(foundPublicDiary).as("조회된 공개 일기가 null이 아니어야 합니다").isNotNull();
    assertThat(foundPublicDiary.getPublicContent())
        .as("조회된 공개 일기의 내용이 원본 내용과 일치해야 합니다")
        .isEqualTo(PUBLIC_CONTENT);
    assertThat(foundPublicDiary.getMemberEntity().getId())
        .as("조회된 공개 일기의 작성자 ID가 원본 작성자 ID와 일치해야 합니다")
        .isEqualTo(member.getId());
  }
}
