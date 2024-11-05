package com.heartsave.todaktodak_api.ai.webhook.controller;

import com.heartsave.todaktodak_api.ai.webhook.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/webhook")
public class AiController {

  private final AiService webhookService;

  @PostMapping("/webtoon")
  public ResponseEntity<Void> saveWebtoon() {
    webhookService.saveWebtoon();
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
