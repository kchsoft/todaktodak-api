package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.auth.annotation.TodakUserId;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryResponse;
import com.heartsave.todaktodak_api.diary.service.MySharedDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "나의 게시물", description = "나의 게시물 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/my/shared")
public class MySharedDiaryController {

  private final MySharedDiaryService mySharedDiaryService;

  @Operation(summary = "나의 공개된 일기 목록 조회")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "공개된 일기 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @GetMapping
  public ResponseEntity<MySharedDiaryPaginationResponse> getMySharedDiaryPagination(
      @TodakUserId Long memberId,
      @Parameter(
              name = "after",
              description = "마지막으로 조회된 공개 일기 ID",
              example = "0",
              schema = @Schema(type = "Long", minimum = "0", defaultValue = "0"))
          @Valid
          @Min(0L)
          @RequestParam(name = "after", defaultValue = "0")
          Long publicDiaryId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(mySharedDiaryService.getPagination(memberId, publicDiaryId));
  }

  @Operation(summary = "특정 날짜의 나의 공개된 일기 상세 조회")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "공개된 일기 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "해당 날짜의 일기를 찾을 수 없음")
      })
  @GetMapping("/detail")
  public ResponseEntity<MySharedDiaryResponse> getMySharedDiary(
      @TodakUserId Long memberId,
      @Parameter(
              name = "date",
              description = "조회할 일기 날짜",
              example = "2024-11-04",
              schema = @Schema(type = "string", format = "date"))
          @Valid
          @PastOrPresent
          @RequestParam("date")
          LocalDateTime requestDate) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(mySharedDiaryService.getDiary(memberId, requestDate.toLocalDate()));
  }
}
