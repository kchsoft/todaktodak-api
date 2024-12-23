package com.heartsave.todaktodak_api.ai.webhook.repository;

import static com.heartsave.todaktodak_api.common.BaseTestObject.DUMMY_STRING_CONTENT;
import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_BGM_KEY_URL;
import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_WEBTOON_KEY_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.ai.webhook.domain.WebhookBgmCompletion;
import com.heartsave.todaktodak_api.ai.webhook.domain.WebhookWebtoonCompletion;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.diary.constant.DiaryBgmGenre;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@Slf4j
@DataJpaTest
@Import(AiRepository.class)
class AiRepositoryTest {

  @Autowired private AiRepository aiRepository;

  @Autowired private TestEntityManager tem;

  private MemberEntity member;
  private DiaryEntity diary;
  private Instant nowDateTime = Instant.now();

  @BeforeEach
  void setUp() {
    member = BaseTestObject.createMemberNoId();
    tem.persist(member);

    diary = BaseTestObject.createDiaryNoIdWithMember(member);
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
      WebhookWebtoonCompletionRequest request =
          new WebhookWebtoonCompletionRequest(member.getId(), diary.getDiaryCreatedTime(), newUrl);
      WebhookWebtoonCompletion completion = WebhookWebtoonCompletion.from(request, newUrl);
      aiRepository.updateWebtoonUrl(completion);

      DiaryEntity updatedDiary = tem.find(DiaryEntity.class, diary.getId());
      assertThat(updatedDiary.getWebtoonImageUrl()).isEqualTo(newUrl);
    }

    @Test
    @DisplayName("존재하지 않는 데이터에 대해 업데이트를 시도해도 예외가 발생하지 않음")
    void updateWebtoonUrlWithNonExistentData() {
      String newUrl = "https://example.com/webtoon.jpg";
      Instant nonExistentDate = Instant.now().plus(1, ChronoUnit.DAYS);
      WebhookWebtoonCompletionRequest request =
          new WebhookWebtoonCompletionRequest(member.getId(), nonExistentDate, newUrl);
      WebhookWebtoonCompletion completion = WebhookWebtoonCompletion.from(request, newUrl);

      aiRepository.updateWebtoonUrl(completion);

      DiaryEntity unchangedDiary = tem.find(DiaryEntity.class, diary.getId());
      assertThat(unchangedDiary.getWebtoonImageUrl()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("updateBgmUrl 메소드는")
  class UpdateBgmUrl {

    @Test
    @DisplayName("BGM URL을 성공적으로 업데이트")
    void updateBgmUrlSuccessfully() {
      String newBgmUrl = "/music-ai/1/2024/11/06/bgm.mp3";
      WebhookBgmCompletionRequest request =
          new WebhookBgmCompletionRequest(member.getId(), diary.getDiaryCreatedTime(), newBgmUrl);
      WebhookBgmCompletion completion = WebhookBgmCompletion.from(request, newBgmUrl);

      aiRepository.updateBgmUrl(completion);

      DiaryEntity updatedDiary = tem.find(DiaryEntity.class, diary.getId());
      assertThat(updatedDiary.getBgmUrl()).isEqualTo(newBgmUrl);
    }

    @Test
    @DisplayName("존재하지 않는 데이터에 대해 업데이트를 시도해도 예외가 발생하지 않음")
    void updateBgmUrlWithNonExistentData() {
      // given
      String newBgmUrl = "https://example.com/bgm.mp3";
      Instant nonExistentDate = Instant.now().plus(1, ChronoUnit.DAYS);
      WebhookBgmCompletionRequest request =
          new WebhookBgmCompletionRequest(member.getId(), nonExistentDate, newBgmUrl);
      WebhookBgmCompletion completion = WebhookBgmCompletion.from(request, newBgmUrl);

      aiRepository.updateBgmUrl(completion);

      DiaryEntity unchangedDiary = tem.find(DiaryEntity.class, diary.getId());
      assertThat(unchangedDiary.getBgmUrl()).isEqualTo("");
    }

    @Test
    @DisplayName("같은 날짜에 여러 일기가 있을 경우 memberId와 날짜로 정확히 찾아서 업데이트")
    void updateBgmUrlWithMultipleDiariesOnSameDate() {
      MemberEntity member2 = BaseTestObject.createMemberNoId();
      tem.persist(member2);

      DiaryEntity diary2 =
          DiaryEntity.builder()
              .memberEntity(member2)
              .content(DUMMY_STRING_CONTENT)
              .bgmUrl("")
              .webtoonImageUrl("")
              .diaryCreatedTime(diary.getDiaryCreatedTime())
              .emotion(DiaryEmotion.HAPPY)
              .bgmGenre(DiaryBgmGenre.POP)
              .build();
      tem.persist(diary2);
      tem.flush();
      tem.clear();

      String newBgmUrl = "https://new-url/target-member-bgm.mp3";
      WebhookBgmCompletionRequest request =
          new WebhookBgmCompletionRequest(member.getId(), diary.getDiaryCreatedTime(), newBgmUrl);
      WebhookBgmCompletion completion = WebhookBgmCompletion.from(request, newBgmUrl);

      aiRepository.updateBgmUrl(completion);

      DiaryEntity targetDiary = tem.find(DiaryEntity.class, diary.getId());
      DiaryEntity otherDiary = tem.find(DiaryEntity.class, diary2.getId());

      assertThat(targetDiary.getBgmUrl()).isEqualTo(newBgmUrl);
      assertThat(otherDiary.getBgmUrl()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("isContentCompleted 메서드는")
  class IsContentCompleted {

    @Test
    @DisplayName("bgmUrl과 webtoonImageUrl이 모두 빈 문자열이면 false를 반환한다")
    void returnsFalseWhenBothUrlsAreEmpty() {
      Boolean result = aiRepository.isContentCompleted(member.getId(), diary.getDiaryCreatedTime());

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("bgmUrl만 존재할 때 false를 반환한다")
    void returnsFalseWhenOnlyBgmUrlExists() {
      DiaryEntity diaryWithBgm =
          DiaryEntity.builder()
              .memberEntity(member)
              .content(DUMMY_STRING_CONTENT)
              .bgmUrl(TEST_BGM_KEY_URL)
              .webtoonImageUrl("")
              .diaryCreatedTime(nowDateTime)
              .emotion(DiaryEmotion.HAPPY)
              .bgmGenre(DiaryBgmGenre.CLASSICAL)
              .build();

      tem.persist(diaryWithBgm);
      tem.flush();
      tem.clear();

      Boolean result = aiRepository.isContentCompleted(member.getId(), nowDateTime);

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
              .webtoonImageUrl(TEST_WEBTOON_KEY_URL)
              .diaryCreatedTime(nowDateTime)
              .emotion(DiaryEmotion.HAPPY)
              .bgmGenre(DiaryBgmGenre.JAZZ)
              .build();
      tem.persist(diaryWithWebtoon);
      tem.flush();
      tem.clear();

      Boolean result = aiRepository.isContentCompleted(member.getId(), nowDateTime);

      assertThat(result).isFalse();
    }

    //    @Test
    //    @DisplayName("bgmUrl과 webtoonImageUrl이 모두 존재할 때 true를 반환한다")
    //    void returnsTrueWhenBothUrlsExist() {
    //      DiaryEntity completedDiary =
    //          DiaryEntity.builder()
    //              .memberEntity(member)
    //              .content(DUMMY_STRING_CONTENT)
    //              .bgmUrl(TEST_BGM_KEY_URL)
    //              .webtoonImageUrl(TEST_WEBTOON_KEY_URL)
    //              .diaryCreatedTime(
    //                  Instant.now().plus(1, ChronoUnit.DAYS)) // BeforeEach의 diary 날짜 다르게 하기 위해
    //              .emotion(DiaryEmotion.HAPPY)
    //              .bgmGenre(DiaryBgmGenre.EDM)
    //              .build();
    //
    //      tem.persist(completedDiary);
    //      tem.flush();
    //      tem.clear();
    //
    //      Boolean result =
    //          aiRepository.isContentCompleted(member.getId(),
    // completedDiary.getDiaryCreatedTime());
    //
    //      assertThat(result).isTrue();
    //    }

    @Test
    @DisplayName("존재하지 않는 데이터에 대해 false를 반환한다")
    void returnsNullForNonExistentData() {
      Instant nonExistentDate = Instant.now().plus(1, ChronoUnit.DAYS);

      Boolean result = aiRepository.isContentCompleted(member.getId(), nonExistentDate);

      assertThat(result).isFalse();
    }
  }
}
