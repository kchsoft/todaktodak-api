package com.heartsave.todaktodak_api.ai.webhook.repository;

import static com.heartsave.todaktodak_api.common.BaseTestEntity.DUMMY_STRING_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@Slf4j
@DataJpaTest
class AiRepositoryTest {

  @Autowired private AiRepository aiRepository;

  @Autowired private TestEntityManager tem;

  private MemberEntity member;
  private DiaryEntity diary;
  private String TEST_BGM_URL = "test-bgm-url";
  private String TEST_WEBTOON_URL = "test-webtoon-url";
  private LocalDateTime nowDateTime = LocalDateTime.now();

  @BeforeEach
  void setUp() {
    member = BaseTestEntity.createMemberNoId();
    tem.persist(member);

    diary = BaseTestEntity.createDiaryNoIdWithMember(member);
    tem.persist(diary);

    tem.flush();
    tem.clear();
  }

  @Nested
  @DisplayName("updateWebtoonUrl 메소드는")
  class UpdateWebtoonUrl {

    @Test
    @DisplayName("웹툰 URL을 성공적으로 업데이트")
    void updateWebtoonUrlSuccessfully() {
      String newUrl = "https://new-url/webtoon.jpg";

      aiRepository.updateWebtoonUrl(
          member.getId(), diary.getDiaryCreatedTime().toLocalDate(), newUrl);

      DiaryEntity updatedDiary = tem.find(DiaryEntity.class, diary.getId());
      assertThat(updatedDiary.getWebtoonImageUrl()).isEqualTo(newUrl);
    }

    @Test
    @DisplayName("존재하지 않는 데이터에 대해 업데이트를 시도해도 예외가 발생하지 않음")
    void updateWebtoonUrlWithNonExistentData() {
      String newUrl = "https://example.com/webtoon.jpg";
      LocalDate nonExistentDate = LocalDate.now().plusDays(1);

      aiRepository.updateWebtoonUrl(member.getId(), nonExistentDate, newUrl);

      DiaryEntity unchangedDiary = tem.find(DiaryEntity.class, diary.getId());
      assertThat(unchangedDiary.getWebtoonImageUrl()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("isContentCompleted 메서드는")
  class IsContentCompleted {

    @Test
    @DisplayName("bgmUrl과 webtoonImageUrl이 모두 빈 문자열이면 false를 반환한다")
    void returnsFalseWhenBothUrlsAreEmpty() {
      Boolean result =
          aiRepository
              .isContentCompleted(member.getId(), diary.getDiaryCreatedTime().toLocalDate())
              .get();

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("bgmUrl만 존재할 때 false를 반환한다")
    void returnsFalseWhenOnlyBgmUrlExists() {
      DiaryEntity diaryWithBgm =
          DiaryEntity.builder()
              .memberEntity(member)
              .content(DUMMY_STRING_CONTENT)
              .bgmUrl(TEST_BGM_URL)
              .webtoonImageUrl("")
              .diaryCreatedTime(nowDateTime)
              .emotion(DiaryEmotion.HAPPY)
              .build();

      tem.persist(diaryWithBgm);
      tem.flush();
      tem.clear();

      Boolean result =
          aiRepository
              .isContentCompleted(member.getId(), diaryWithBgm.getDiaryCreatedTime().toLocalDate())
              .get();

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("webtoonImageUrl만 존재할 때 false를 반환한다")
    void returnsFalseWhenOnlyWebtoonUrlExists() {

      DiaryEntity diaryWithWebtoon =
          DiaryEntity.builder()
              .memberEntity(member)
              .content(DUMMY_STRING_CONTENT)
              .bgmUrl("")
              .webtoonImageUrl(TEST_WEBTOON_URL)
              .diaryCreatedTime(nowDateTime)
              .emotion(DiaryEmotion.HAPPY)
              .build();
      tem.persist(diaryWithWebtoon);
      tem.flush();
      tem.clear();

      Boolean result =
          aiRepository
              .isContentCompleted(
                  member.getId(), diaryWithWebtoon.getDiaryCreatedTime().toLocalDate())
              .get();

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("bgmUrl과 webtoonImageUrl이 모두 존재할 때 true를 반환한다")
    void returnsTrueWhenBothUrlsExist() {
      DiaryEntity completedDiary =
          DiaryEntity.builder()
              .memberEntity(member)
              .content(DUMMY_STRING_CONTENT)
              .bgmUrl(TEST_BGM_URL)
              .webtoonImageUrl(TEST_WEBTOON_URL)
              .diaryCreatedTime(LocalDateTime.now().plusDays(1)) // BeforeEach의 diary 날짜 다르게 하기 위해
              .emotion(DiaryEmotion.HAPPY)
              .build();

      tem.persist(completedDiary);
      tem.flush();
      tem.clear();

      Boolean result =
          aiRepository
              .isContentCompleted(
                  member.getId(), completedDiary.getDiaryCreatedTime().toLocalDate())
              .get();

      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 데이터에 대해 false를 반환한다")
    void returnsNullForNonExistentData() {
      LocalDate nonExistentDate = LocalDate.now().plusDays(1);

      Boolean result =
          aiRepository.isContentCompleted(member.getId(), nonExistentDate).orElse(false);

      assertThat(result).isFalse();
    }
  }
}
