package com.heartsave.todaktodak_api.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.diary.common.DiaryEmotion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

  @BeforeAll
  static void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    mockWebServer.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            switch (request.getPath()) {
              case WEBTOON_URI:
                return new MockResponse().setResponseCode(200).setBody("webtoon request ok");
              case BGM_URI:
                return new MockResponse().setResponseCode(200).setBody("bgm request ok");
              case COMMENT_URI:
                return new MockResponse().setResponseCode(200).setBody("comment request ok");
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
  @DisplayName("WebClient가 실제로 비동기 API 요청을 보내는지 확인합니다.")
  void webclientAsyncRequest() throws InterruptedException {
    List<String> methodCallOrder = List.of(WEBTOON_URI, BGM_URI, COMMENT_URI);
    List<String> requestOrder = List.of(WEBTOON_URI, BGM_URI, COMMENT_URI);

    while (methodCallOrder.equals(requestOrder) == true) {
      requestOrder = new ArrayList<>();
      aiService.callWebtoon(1L, "content", DiaryEmotion.JOY);
      aiService.callBgm(1L, "content", DiaryEmotion.JOY);
      aiService.callComment(1L, "content", DiaryEmotion.JOY);

      RecordedRequest request = mockWebServer.takeRequest();
      requestOrder.add(request.getPath());
      request = mockWebServer.takeRequest();
      requestOrder.add(request.getPath());
      request = mockWebServer.takeRequest();
      requestOrder.add(request.getPath());
    }
    assertThat(methodCallOrder.equals(requestOrder)).isFalse();
  }
}
