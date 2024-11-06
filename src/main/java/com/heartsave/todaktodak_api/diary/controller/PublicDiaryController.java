package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<PublicDiaryPaginationResponse> getPublicDiaries(
      @AuthenticationPrincipal TodakUser principal,
      @Valid @Min(0L) @RequestParam(name = "after", defaultValue = "0", required = false)
          Long publicDiaryId) {
    log.info("공개 일기를 조회를 요청합니다. after = {}", publicDiaryId);
    PublicDiaryPaginationResponse response =
        publicDiaryService.getPublicDiaryPagination(principal, publicDiaryId);
    log.info("공개 일기 조회를 성공적으로 마쳤습니다.");
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
      @AuthenticationPrincipal TodakUser principal,
      @Parameter(description = "일기 공개 업로드 요청 데이터", required = true) @Valid @RequestBody
          PublicDiaryWriteRequest request) {
    log.info("공개 일기 업로드 시작");
    publicDiaryService.write(principal, request.publicContent(), request.diaryId());
    log.info("공개 일기 업로드 성공");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(
      summary = "일기 반응 토글",
      description = "공개 일기에 대한 반응(좋아요 등)을 토글합니다. 이미 있는 반응은 제거되고, 없는 반응은 추가됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "반응 토글 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
      })
  @PostMapping("/reaction")
  public ResponseEntity<Void> toggleReaction(
      @Parameter(hidden = true) @AuthenticationPrincipal TodakUser principal,
      @Parameter(description = "일기 반응 요청 데이터", required = true) @Valid @RequestBody
          PublicDiaryReactionRequest request) {
    log.info("일기장 반응 토글을 요청합니다");
    publicDiaryService.toggleReactionStatus(principal, request);
    log.info("일기장 반응 토글을 성공적으로 마쳤습니다.");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
