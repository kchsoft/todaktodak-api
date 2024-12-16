package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.auth.annotation.TodakUserId;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPageResponse;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공개 일기장(피드)", description = "공개 일기장(피드) API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/public")
public class PublicDiaryController {

  private final PublicDiaryService publicDiaryService;

  @Operation(summary = "공개 일기 조회", description = "공개 일기의 상세 내용을 조회합니다. after 파라미터를 통해 페이징을 처리합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "공개 일기 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @GetMapping
  public ResponseEntity<PublicDiaryPageResponse> getPagination(
      @TodakUserId Long memberId,
      @Min(0L) @RequestParam(name = "after", defaultValue = "0") Long publicDiaryId,
      @PastOrPresent @RequestParam(name = "date", defaultValue = "1970-01-01T00:00:00Z")
          Instant createdTime) {
    DiaryPageRequest request = new DiaryPageRequest(publicDiaryId, createdTime);
    log.info("공개 일기를 조회를 요청합니다. after = {}", request.publicDiaryId());
    PublicDiaryPageResponse response = publicDiaryService.getPagination(memberId, request);
    log.info("공개 일기 조회를 성공적으로 마쳤습니다. after = {}", request.publicDiaryId());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(summary = "공개 일기 작성")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "공개 일기 작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      })
  @PostMapping
  public ResponseEntity<Void> writePublicContent(
      @TodakUserId Long memberId,
      @Parameter(description = "일기 공개 업로드 요청 데이터", required = true) @Valid @RequestBody
          PublicDiaryWriteRequest request) {
    log.info("공개 일기 업로드 시작");
    publicDiaryService.write(memberId, request.diaryId(), request.publicContent());
    log.info("공개 일기 업로드 성공");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{publicDiaryId}")
  public ResponseEntity<Void> deletePublicDiary(
      @TodakUserId Long memberId, @Valid @NotNull @Min(0L) @PathVariable Long publicDiaryId) {
    publicDiaryService.delete(memberId, publicDiaryId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
