package com.heartsave.todaktodak_api.ai.webhook.controller;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.service.AiDiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook/ai")
public class AiWebhookController {

  private final AiDiaryService aiDiaryService;

  @PostMapping("/webtoon")
  public ResponseEntity<Void> saveWebtoon(@Valid @RequestBody AiWebtoonRequest request) {
    log.info(
        "AI 웹툰 저장을 시작합니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    aiDiaryService.saveWebtoon(request);
    log.info(
        "AI 웹툰 저장을 마침니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
