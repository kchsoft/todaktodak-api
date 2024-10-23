package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "일기장", description = "일기장 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/my")
public class DiaryController {

  private final DiaryService diaryService;

  @Operation(summary = "일기 작성")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "일기 작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @PostMapping
  public ResponseEntity<DiaryWriteResponse> writeDiary(
      @AuthenticationPrincipal TodakUser principal,
      @Valid @RequestBody DiaryWriteRequest writeRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(diaryService.write(principal, writeRequest));
  }

  @Operation(summary = "일기 삭제")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "일기 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @DeleteMapping("/{diaryId}")
  public ResponseEntity<Void> deleteDiary(
      @AuthenticationPrincipal TodakUser principal,
      @Parameter(
              name = "diaryId",
              description = "삭제할 일기 ID",
              example = "1",
              required = true,
              schema = @Schema(type = "Long", minimum = "1"))
          @Valid
          @PathVariable
          @Min(value = 1, message = "diaryId의 값은 최소 1 이상이어야 합니다.")
          Long diaryId) {
    diaryService.delete(principal, diaryId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
