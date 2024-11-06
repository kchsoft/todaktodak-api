package com.heartsave.todaktodak_api.ai.webhook.controller;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiBgmRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.service.AiDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

  @Operation(summary = "AI 웹툰 저장", description = "AI가 생성한 웹툰을 저장합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "AI 웹툰 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @PostMapping("/webtoon")
  public ResponseEntity<Void> saveWebtoon(
      @Parameter(
              description = "AI 웹툰 저장 요청 정보",
              required = true,
              schema = @Schema(implementation = AiWebtoonRequest.class))
          @Valid
          @RequestBody
          AiWebtoonRequest request) {
    log.info(
        "AI 웹툰 저장을 시작합니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    aiDiaryService.saveWebtoon(request);
    log.info(
        "AI 웹툰 저장을 마침니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(summary = "AI BGM 저장", description = "AI가 생성한 BGM을 저장합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "AI BGM 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @PostMapping("/bgm")
  public ResponseEntity<Void> saveBgm(
      @Parameter(
              description = "AI BGM 저장 요청 정보",
              required = true,
              schema = @Schema(implementation = AiBgmRequest.class))
          @Valid
          @RequestBody
          AiBgmRequest request) {
    log.info(
        "AI BGM 저장을 시작합니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    aiDiaryService.saveBgm(request);
    log.info(
        "AI BGM 저장을 마칩니다. memberId={}, diaryDate={}", request.memberId(), request.createdDate());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
