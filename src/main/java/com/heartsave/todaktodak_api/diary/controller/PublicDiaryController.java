package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "일기장", description = "공개 일기장 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/public")
public class PublicDiaryController {

  private final PublicDiaryService publicDiaryService;

  @Operation(summary = "공개 일기 작성")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "공개 일기 작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @PostMapping
  public ResponseEntity<Void> writePublicContent(
      @AuthenticationPrincipal TodakUser principal,
      @Valid @RequestBody PublicDiaryWriteRequest request) {
    log.info("공개 일기 업로드 시작");
    publicDiaryService.write(principal, request.publicContent(), request.diaryId());
    log.info("공개 일기 업로드 성공");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
