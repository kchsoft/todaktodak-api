package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryPageIndexProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@DataJpaTest(
    includeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = DiaryPageIndexFactory.class))
public class PublicDiaryRepositoryTest {

  @Autowired private PublicDiaryRepository publicDiaryRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private DiaryPageIndexFactory indexFactory;
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

  @TestConfiguration
  @EnableJpaAuditing
  public static class TestAuditConfig {}

  @BeforeEach
  void setup() {
    // member , diary 생성
    member = BaseTestObject.createMemberNoId();
    diary = BaseTestObject.createDiaryNoIdWithMember(member);
    member = memberRepository.save(member);
    diary = diaryRepository.save(diary);

    // public diary 생성
    publicDiary =
        PublicDiaryEntity.builder()
            .diaryEntity(diary)
            .memberEntity(member)
            .publicContent(PUBLIC_CONTENT)
            .build();
    publicDiary = publicDiaryRepository.save(publicDiary);

    // member1 , diary1 생성
    member1 = BaseTestObject.createMemberNoId();
    diary1 = BaseTestObject.createDiaryNoIdWithMember(member1);
    member1 = memberRepository.save(member1);
    diary1 = diaryRepository.save(diary1);

    // public diary1 생성
    publicDiary1 =
        PublicDiaryEntity.builder()
            .diaryEntity(diary1)
            .memberEntity(member1)
            .publicContent("첫 번째 공개 일기")
            .build();
    publicDiary1 = publicDiaryRepository.save(publicDiary1);

    // member2 , diary2 생성
    member2 = BaseTestObject.createMemberNoId();
    diary2 = BaseTestObject.createDiaryNoIdWithMember(member2);
    member2 = memberRepository.save(member2);
    diary2 = diaryRepository.save(diary2);

    // public diary2 생성
    publicDiary2 =
        PublicDiaryEntity.builder()
            .diaryEntity(diary2)
            .memberEntity(member2)
            .publicContent("두 번째 공개 일기")
            .build();
    publicDiary2 = publicDiaryRepository.save(publicDiary2);

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
    PublicDiaryPageIndexProjection indexProjection =
        publicDiaryRepository.findLatestCreatedTimeAndId().get();

    PublicDiaryEntity findPublicDiary =
        publicDiaryRepository.findById(indexProjection.getPublicDiaryId()).orElse(null);
    assertThat(indexProjection.getPublicDiaryId())
        .as("최신 공개 일기 ID는 조회된 공개 일기의 ID와 일치해야 합니다.")
        .isEqualTo(findPublicDiary.getId());

    assertThat(indexProjection.getPublicDiaryId())
        .as("최신 공개 일기 ID는 diary2의 공개 일기 ID와 일치해야 합니다.")
        .isEqualTo(diary2.getPublicDiaryEntity().getId());
  }

  @Test
  @DisplayName("특정 ID 이하의 공개 일기 ContentOnly 조회 성공")
  void findContentOnlyById_Success() {
    publicDiary =
        publicDiaryRepository.findById(publicDiary.getId()).get(); // createdTime 의 ms을 구하기 위해
    publicDiary1 = publicDiaryRepository.findById(publicDiary1.getId()).get();
    publicDiary2 = publicDiaryRepository.findById(publicDiary2.getId()).get();
    // page 5개
    Pageable pageable = PageRequest.of(0, 5);

    DiaryPageRequest request1 =
        new DiaryPageRequest(publicDiary1.getId(), publicDiary1.getCreatedTime());
    DiaryPageIndex pageIndex1 = indexFactory.createFrom(request1);
    List<PublicDiaryContentProjection> contentOnly1 =
        publicDiaryRepository.findNextContents(pageIndex1, pageable);

    DiaryPageRequest request2 =
        new DiaryPageRequest(publicDiary2.getId(), publicDiary2.getCreatedTime());
    DiaryPageIndex pageIndex2 = indexFactory.createFrom(request2);
    List<PublicDiaryContentProjection> contentOnly2 =
        publicDiaryRepository.findNextContents(pageIndex2, pageable);

    assertThat(contentOnly1.size()).isNotEqualTo(contentOnly2.size());
    assertThat(contentOnly2).hasSize(2);
    assertThat(contentOnly2.get(0).getPublicContent()).isEqualTo(publicDiary1.getPublicContent());
    assertThat(contentOnly2.get(1).getPublicContent()).isEqualTo(publicDiary.getPublicContent());
  }
}
