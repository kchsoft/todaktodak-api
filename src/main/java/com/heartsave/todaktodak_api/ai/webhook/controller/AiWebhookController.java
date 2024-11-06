package com.heartsave.todaktodak_api.ai.webhook.controller;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.service.AiDiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook/ai")
public class AiWebhookController {

  private final AiDiaryService aiDiaryService;

  @PostMapping("/webtoon")
  public ResponseEntity<Void> saveWebtoon(@Valid @RequestBody AiWebtoonRequest request) {
    aiDiaryService.saveWebtoon(request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
