package com.heartsave.todaktodak_api.ai.service;

import com.heartsave.todaktodak_api.ai.dto.AiContentRequest;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiService {

  private final WebClient webClient;

  public void callAiContent(DiaryEntity diary) {
    AiContentRequest request = getAiContentRequest(diary);
    callWebtoon(request);
    callBgm(request);
    callComment(request);
  }

  private AiContentRequest getAiContentRequest(DiaryEntity diary) {
    return AiContentRequest.builder()
        .id(diary.getMemberEntity().getId())
        .content(diary.getContent())
        .emotion(diary.getEmotion())
        .build();
  }

  private void callWebtoon(AiContentRequest request) {
    webClient
        .post()
        .uri("/webtoon")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("Webtoon 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("Webtoon 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private void callBgm(AiContentRequest request) {
    webClient
        .post()
        .uri("/bgm")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("BGM 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("BGM 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private void callComment(AiContentRequest request) {
    webClient
        .post()
        .uri("/comment")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class)
        .doOnSuccess(result -> log.info("AI 코멘트 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("AI 코멘트 생성 요청에 오류가 발생했습니다."))
        .subscribe();
  }
}
