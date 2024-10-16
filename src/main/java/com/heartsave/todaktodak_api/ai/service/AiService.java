package com.heartsave.todaktodak_api.ai.service;

import com.heartsave.todaktodak_api.ai.dto.AiContentRequest;
import com.heartsave.todaktodak_api.diary.common.DiaryEmotion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Service
@Slf4j
public class AiService {

  private final WebClient webClient;

  @Value("${ai.server.url.domain}")
  private String AI_SERVER_URL_DOMAIN;

  @Autowired
  public AiService(Builder webClientBuilder) {
    this.webClient =
        webClientBuilder
            .baseUrl(AI_SERVER_URL_DOMAIN)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  public void callWebtoon(Long memberId, String diaryContent, DiaryEmotion emotion) {
    webClient
        .post()
        .uri("/webtoon")
        .bodyValue(getAiContentRequest(memberId, diaryContent, emotion))
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("Webtoon 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("Webtoon 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  public void callBgm(Long memberId, String content, DiaryEmotion emotion) {
    webClient
        .post()
        .uri("/bgm")
        .bodyValue(getAiContentRequest(memberId, content, emotion))
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("BGM 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("BGM 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  public void callComment(Long memberId, String content, DiaryEmotion emotion) {
    webClient
        .post()
        .uri("/comment")
        .bodyValue(getAiContentRequest(memberId, content, emotion))
        .retrieve()
        .bodyToMono(String.class)
        .doOnSuccess(result -> log.info("AI 코멘트 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("AI 코멘트 생성 요청에 오류가 발생했습니다."))
        .subscribe();
  }

  private AiContentRequest getAiContentRequest(
      Long memberId, String content, DiaryEmotion emotion) {
    return AiContentRequest.builder().id(memberId).content(content).emotion(emotion).build();
  }
}
