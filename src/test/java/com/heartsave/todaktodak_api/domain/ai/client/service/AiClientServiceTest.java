package com.heartsave.todaktodak_api.domain.ai.client.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.domain.ai.client.config.properties.AiServerProperties;
import com.heartsave.todaktodak_api.domain.ai.client.dto.response.AiClientCommentResponse;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import java.io.IOException;
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
class AiClientServiceTest {

  private static MockWebServer mockWebServer;
  private static AiClientService aiClientService;
  private static AiServerProperties aiServerProperties;
  private static final String WEBTOON_URI = "/webtoon";
  private static final String BGM_URI = "/music-ai";
  private static final String COMMENT_URI = "/comment";
  private static final String AI_COMMENT = "ai-comment";

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
                return new MockResponse();
              case COMMENT_URI:
                return new MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"content\": \"" + AI_COMMENT + "\"}")
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
              default:
                return new MockResponse().setResponseCode(404);
            }
          }
        });

    String AI_URL = String.format("http://localhost:%s", mockWebServer.getPort());
    aiServerProperties = new AiServerProperties(AI_URL, AI_URL, AI_URL);

    WebClient aiWebClient =
        WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    aiClientService = new AiClientService(aiServerProperties, aiWebClient);
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  @DisplayName("AI 컨텐츠 요청 결과 확인")
  void aiContentRequestTest() throws InterruptedException {
    MemberEntity member = BaseTestObject.createMember();
    DiaryEntity diary = BaseTestObject.createDiaryWithMember(member);

    AiClientCommentResponse aiResponse = aiClientService.callDiaryContent(diary);
    log.info("aiComment 결과 = {}", aiResponse.getAiComment());
    assertThat(aiResponse.getAiComment()).as("AI 코멘트 비동기 요청 응답이 올바르지 않습니다.").isEqualTo(AI_COMMENT);
    assertThat(aiResponse.getAiComment().startsWith("{"))
        .as("응답 형식은 '{' (json 형식) 로 시작하면 안됩니다.")
        .isFalse();

    for (int i = 0; i < mockWebServer.getRequestCount(); i++) {
      RecordedRequest request = mockWebServer.takeRequest();
      String body = request.getBody().readUtf8();
      String path = request.getPath();
      log.info("{} = {}", path, body);
    }
  }
}
