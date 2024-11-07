package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@DataJpaTest
public class PublicDiaryRepositoryTest {

  @Autowired private PublicDiaryRepository publicDiaryRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private TestEntityManager tem;

  private MemberEntity member;
  private MemberEntity member1;
  private MemberEntity member2;
  private DiaryEntity diary;
  private DiaryEntity diary1;
  private DiaryEntity diary2;
  private PublicDiaryEntity publicDiary;
  private final String PUBLIC_CONTENT = "테스트 공개 일기 내용";
  private PublicDiaryEntity publicDiary1;
  private PublicDiaryEntity publicDiary2;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMemberNoId();
    diary = BaseTestObject.createDiaryNoIdWithMember(member);

    memberRepository.save(member);
    diaryRepository.save(diary);

    publicDiary =
        PublicDiaryEntity.builder()
            .diaryEntity(diary)
            .memberEntity(member)
            .publicContent(PUBLIC_CONTENT)
            .build();

    member1 = BaseTestObject.createMemberNoId();
    diary1 = BaseTestObject.createDiaryNoIdWithMember(member1);
    memberRepository.save(member1);
    diary1 = diaryRepository.save(diary1);
    publicDiary1 =
        PublicDiaryEntity.builder()
            .diaryEntity(diary1)
            .memberEntity(member1)
            .publicContent("첫 번째 공개 일기")
            .build();
    publicDiaryRepository.save(publicDiary1);

    member2 = BaseTestObject.createMemberNoId();
    diary2 = BaseTestObject.createDiaryNoIdWithMember(member2);
    memberRepository.save(member2);
    diary2 = diaryRepository.save(diary2);
    publicDiary2 =
        PublicDiaryEntity.builder()
            .diaryEntity(diary2)
            .memberEntity(member2)
            .publicContent("두 번째 공개 일기")
            .build();
    publicDiaryRepository.save(publicDiary2);

    tem.flush();
    tem.clear();
    log.info("==== flush setup jpa method ====");
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

  @Test
  @DisplayName("최신 공개 일기 ID 조회 성공")
  void findLatestId_Success() {
    diary2 = diaryRepository.findById(diary2.getId()).get();
    Long latestId = publicDiaryRepository.findLatestId().get();

    PublicDiaryEntity findPublicDiary = publicDiaryRepository.findById(latestId).orElse(null);
    assertThat(latestId)
        .as("최신 공개 일기 ID는 조회된 공개 일기의 ID와 일치해야 합니다.")
        .isEqualTo(findPublicDiary.getId());

    assertThat(latestId)
        .as("최신 공개 일기 ID는 diary2의 공개 일기 ID와 일치해야 합니다.")
        .isEqualTo(diary2.getPublicDiaryEntity().getId());
  }

  @Test
  @DisplayName("특정 ID 이하의 공개 일기 ContentOnly 조회 성공")
  void findContentOnlyById_Success() {

    Pageable pageable = PageRequest.of(0, 5);

    List<PublicDiaryContentOnlyProjection> contentOnly2 =
        publicDiaryRepository.findNextContentOnlyById(publicDiary2.getId() + 1, pageable);

    List<PublicDiaryContentOnlyProjection> contentOnly1 =
        publicDiaryRepository.findNextContentOnlyById(publicDiary1.getId() + 1, pageable);
    assertThat(contentOnly1.size()).isNotEqualTo(contentOnly2.size());
    assertThat(contentOnly2).hasSize(2);
    assertThat(contentOnly2.get(0).getPublicContent()).isEqualTo(publicDiary2.getPublicContent());
    assertThat(contentOnly2.get(1).getPublicContent()).isEqualTo(publicDiary1.getPublicContent());
  }
}
