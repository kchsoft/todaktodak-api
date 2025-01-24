package com.heartsave.todaktodak_api.domain.diary.integrate;

import static com.heartsave.todaktodak_api.config.BaseTestObject.DUMMY_STRING_CONTENT;
import static com.heartsave.todaktodak_api.config.BaseTestObject.createDiaryNoIdWithMember;
import static com.heartsave.todaktodak_api.config.BaseTestObject.createMemberNoId;
import static com.heartsave.todaktodak_api.domain.diary.constant.DiaryBgmGenre.ACOUSTIC;
import static com.heartsave.todaktodak_api.domain.diary.constant.DiaryEmotion.HAPPY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.config.WithMockTodakUser;
import com.heartsave.todaktodak_api.config.BaseIntegrateTest;
import com.heartsave.todaktodak_api.domain.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.exception.DiaryDailyWritingLimitExceedException;
import com.heartsave.todaktodak_api.domain.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DiaryIntegrateTest extends BaseIntegrateTest {

  @Autowired private DiaryRepository diaryRepository;
  @Autowired private MemberRepository memberRepository;

  private MockWebServer mockWebServer;

  private MemberEntity member;
  private final Instant NOW_DATE_TIME = Instant.now();
  private final String DEFAULT_URL = "/api/v1/diary/my";

  @BeforeAll
  void init() throws IOException {
    // set member
    member = BaseTestObject.createMember();
    memberRepository.save(member);

    // set mock ai server
    mockWebServer = new MockWebServer();
    mockWebServer.start(50000);
    mockWebServer.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            switch (request.getPath()) {
              case "/webtoon", "/music-ai":
                return new MockResponse();
              case "/comment":
                return new MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"content\": \"" + "AI_COMMENT" + "\"}")
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
              default:
                return new MockResponse().setResponseCode(404);
            }
          }
        });
  }

  @Nested
  @DisplayName("일기 작성 테스트")
  class Diary_Write_Test {

    @Test
    @DisplayName("일기 작성 성공")
    @WithMockTodakUser
    void write_Success() throws Exception {
      DiaryWriteRequest request =
          new DiaryWriteRequest(NOW_DATE_TIME, HAPPY, DUMMY_STRING_CONTENT, ACOUSTIC);

      mockMvc
          .perform(
              post(DEFAULT_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("하루 일기 작성 횟수 초과 에러 발생")
    @WithMockTodakUser
    void write_Fail_DailyLimit() throws Exception {
      DiaryWriteRequest request =
          new DiaryWriteRequest(NOW_DATE_TIME, HAPPY, DUMMY_STRING_CONTENT, ACOUSTIC);

      // first write - success
      mockMvc
          .perform(
              post(DEFAULT_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      // second write - fail
      mockMvc
          .perform(
              post(DEFAULT_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(DiaryDailyWritingLimitExceedException.class));
    }
  }

  @Nested
  @DisplayName("일기 삭제 테스트")
  class Diary_Delete_Test {

    @Test
    @DisplayName("일기 삭제 성공")
    @WithMockTodakUser
    void delete_Success() throws Exception {
      // given
      DiaryEntity testDiary = createDiaryNoIdWithMember(member);
      diaryRepository.save(testDiary);

      // when
      mockMvc
          .perform(delete(DEFAULT_URL + "/{diaryId}", testDiary.getId()))
          .andExpect(status().isNoContent());

      assertThat(diaryRepository.findById(testDiary.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 일기 삭제 시도 - 실패")
    @WithMockTodakUser
    void delete_Fail_NotFound() throws Exception {
      Long nonExistentId = 99999L;

      mockMvc
          .perform(delete(DEFAULT_URL + "/{diaryId}", nonExistentId))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(DiaryNotFoundException.class));
    }

    @Test
    @DisplayName("다른 사람 일기 삭제 시도 - 실패")
    @WithMockTodakUser
    void delete_others_Fail_NotFound() throws Exception {
      // given
      MemberEntity member2 = createMemberNoId();
      memberRepository.save(member2);
      DiaryEntity testDiary = createDiaryNoIdWithMember(member2);
      diaryRepository.save(testDiary);

      // when & then
      mockMvc
          .perform(delete(DEFAULT_URL + "/{diaryId}", testDiary.getId()))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(DiaryNotFoundException.class));
    }
  }

  @Nested
  @DisplayName("연월별 일기 조회 테스트")
  class Diary_YearMonth_Test {

    @Test
    @DisplayName("연월 일기 작성 현황 0회 조회 성공")
    @WithMockTodakUser
    void Zero_getYearMonth_Success() throws Exception {
      Instant currentTime = Instant.now();

      mockMvc
          .perform(
              get(DEFAULT_URL)
                  .param("yearMonth", currentTime.toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andDo(print());
    }

    @Test
    @DisplayName("연월 일기 작성 현황 조회 성공")
    @WithMockTodakUser
    void getYearMonth_Success() throws Exception {
      DiaryEntity testDiary = createDiaryNoIdWithMember(member);
      diaryRepository.save(testDiary);

      mockMvc
          .perform(
              get(DEFAULT_URL)
                  .param("yearMonth", testDiary.getDiaryCreatedTime().toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.diaryIndexes[0].diaryId").value(testDiary.getId()))
          .andExpect(status().isOk())
          .andDo(print());
    }
  }

  @Nested
  @DisplayName("일기 상세 조회 테스트")
  class Diary_Detail_Test {

    @Test
    @DisplayName("일기 상세 조회 성공")
    @WithMockTodakUser
    void getDiary_Success() throws Exception {

      // given
      DiaryEntity testDiary = createDiaryNoIdWithMember(member);
      diaryRepository.save(testDiary);

      mockMvc
          .perform(
              get(DEFAULT_URL + "/detail")
                  .param("date", testDiary.getDiaryCreatedTime().toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.diaryId").value(testDiary.getId()))
          .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 일기 조회 - 실패")
    @WithMockTodakUser
    void getDiary_Fail_NotFound() throws Exception {
      Instant nextDayTime = Instant.now().minus(1000, ChronoUnit.DAYS);
      mockMvc
          .perform(
              get(DEFAULT_URL + "/detail")
                  .param("date", nextDayTime.toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(DiaryNotFoundException.class));
    }
  }
}
