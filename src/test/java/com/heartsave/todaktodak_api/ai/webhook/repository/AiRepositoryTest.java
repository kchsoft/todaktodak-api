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
  @DisplayName("isDefaultBgmUrl 메소드는")
  class IsDefaultBgmUrl {

    @Test
    @DisplayName("BGM URL이 비어있을 때 true를 반환한다")
    void returnsTrueWhenBgmUrlIsEmpty() {

      Boolean result =
          aiRepository.isDefaultBgmUrl(member.getId(), diary.getDiaryCreatedTime().toLocalDate());

      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("BGM URL이 설정되어 있을 때 false를 반환한다")
    void returnsFalseWhenBgmUrlIsSet() {
      DiaryEntity diaryBgmurl =
          DiaryEntity.builder()
              .memberEntity(member)
              .bgmUrl("pre-sigend-bgm-url")
              .webtoonImageUrl("")
              .content(DUMMY_STRING_CONTENT)
              .emotion(DiaryEmotion.HAPPY)
              .diaryCreatedTime(LocalDateTime.now().minusDays(1L))
              .build();
      tem.persist(diaryBgmurl);
      tem.flush();
      tem.clear();
      Boolean result =
          aiRepository.isDefaultBgmUrl(
              member.getId(), diaryBgmurl.getDiaryCreatedTime().toLocalDate());

      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 데이터에 대해 null을 반환한다")
    void returnsNullForNonExistentData() {
      LocalDate nonExistentDate = LocalDate.now().plusDays(1);

      Boolean result = aiRepository.isDefaultBgmUrl(member.getId(), nonExistentDate);
      assertThat(result).isNull();
    }
  }
}
