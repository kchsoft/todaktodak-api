package com.heartsave.todaktodak_api.diary.controller;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.HEADER.DEFAULT_TIME_ZONE;
import static com.heartsave.todaktodak_api.common.constant.CoreConstant.HEADER.TIME_ZONE_KEY;

import com.heartsave.todaktodak_api.auth.annotation.TodakUserId;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryYearMonthResponse;
import com.heartsave.todaktodak_api.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "나의 일기장", description = "나의 일기장 API")
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
      @TodakUserId Long memberId,
      @Valid @RequestBody DiaryWriteRequest writeRequest,
      @RequestHeader(name = TIME_ZONE_KEY, defaultValue = DEFAULT_TIME_ZONE) String zoneName) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(diaryService.write(memberId, writeRequest, zoneName));
  }

  @Operation(summary = "일기 삭제")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "일기 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @DeleteMapping("/{diaryId}")
  public ResponseEntity<Void> deleteDiary(
      @TodakUserId Long memberId,
      @Parameter(name = "diaryId", description = "삭제할 일기 ID", example = "1", required = true)
          @Valid
          @PathVariable
          @Min(value = 1, message = "diaryId의 값은 최소 1 이상이어야 합니다.")
          Long diaryId) {
    diaryService.delete(memberId, diaryId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(summary = "일기 목록 조회")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "일기 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @GetMapping
  public ResponseEntity<DiaryYearMonthResponse> getDiaryYearMonthInfo(
      @TodakUserId Long memberId,
      @Parameter(
              name = "yearMonth",
              description = "조회할 연월",
              example = "2024-10",
              required = true,
              schema = @Schema(type = "string"))
          @RequestParam("yearMonth")
          Instant request,
      @RequestHeader(name = TIME_ZONE_KEY, defaultValue = DEFAULT_TIME_ZONE) String zoneName) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(diaryService.getYearMonth(memberId, request, zoneName));
  }

  @Operation(summary = "일기 상세 조회")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "일기 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "해당 날짜의 일기를 찾을 수 없음")
      })
  @GetMapping("/detail")
  public ResponseEntity<DiaryResponse> getDiary(
      @TodakUserId Long memberId,
      @Parameter(
              name = "date",
              description = "조회할 일기의 날짜",
              example = "2024-10-26",
              required = true,
              schema = @Schema(type = "string", format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
          @Valid
          @PastOrPresent(message = "현재 날짜 이전의 일기만 조회가 가능합니다.")
          @RequestParam("date")
          Instant request) {
    return ResponseEntity.status(HttpStatus.OK).body(diaryService.getDiary(memberId, request));
  }
}
