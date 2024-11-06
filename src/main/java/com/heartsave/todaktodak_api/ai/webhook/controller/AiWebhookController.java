package com.heartsave.todaktodak_api.ai.webhook.controller;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiBgmRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.service.AiDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 웹훅", description = "AI 웹훅 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook/ai")
public class AiWebhookController {

  private final AiDiaryService aiDiaryService;

  @Operation(summary = "AI 웹툰 저장")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "AI 웹툰 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @PostMapping("/webtoon")
  public ResponseEntity<Void> saveWebtoon(@Valid @RequestBody AiWebtoonRequest request) {
    log.info(
        "AI 웹툰 저장을 시작합니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    aiDiaryService.saveWebtoon(request);
    log.info(
        "AI 웹툰 저장을 마침니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/bgm")
  public ResponseEntity<Void> saveBgm(@Valid @RequestBody AiBgmRequest request) {
    aiDiaryService.saveBgm(request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
