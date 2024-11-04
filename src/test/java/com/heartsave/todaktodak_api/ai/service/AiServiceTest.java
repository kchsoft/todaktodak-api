package com.heartsave.todaktodak_api.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.ai.dto.response.AiContentResponse;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
class AiServiceTest {

  private static MockWebServer mockWebServer;
  private static AiService aiService;
  private static final String WEBTOON_URI = "/webtoon";
  private static final String BGM_URI = "/bgm";
  private static final String COMMENT_URI = "/comment";
  private static final String AI_COMMENT = "this is ai comment";

  @BeforeAll
  static void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    mockWebServer.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            switch (request.getPath()) {
              case WEBTOON_URI, BGM_URI:
                return new MockResponse().setHeadersDelay(300, TimeUnit.MILLISECONDS);
              case COMMENT_URI:
                return new MockResponse().setResponseCode(200).setBody(AI_COMMENT);
              default:
                return new MockResponse().setResponseCode(404);
            }
          }
        });

    String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
    WebClient aiWebClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    aiService = new AiService(aiWebClient);
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  @DisplayName("AI 컨텐츠 요청 결과 확인")
  void aiContentRequestTest() throws InterruptedException {
    MemberEntity member = MemberEntity.builder().id(2L).build();
    DiaryEntity diary =
        DiaryEntity.builder()
            .id(1L)
            .emotion(DiaryEmotion.JOY)
            .content("content")
            .memberEntity(member)
            .build();

    AiContentResponse aiResponse = aiService.callAiContent(diary);
    log.info("aiComment 결과 = {}", aiResponse.getAiComment());
    Thread.sleep(300);
    assertThat(aiResponse.getAiComment()).as("AI 코멘트 비동기 요청 응답이 올바르지 않습니다.").isEqualTo(AI_COMMENT);
  }
}
