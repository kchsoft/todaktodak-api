package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.auth.annotation.TodakUserId;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryReactionErrorSpec;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.exception.DiaryReactionExistException;
import com.heartsave.todaktodak_api.diary.service.DiaryReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공개 일기장(피드)", description = "공개 일기장(피드) API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/public")
public class DiaryReactionController {

  private final DiaryReactionService reactionService;

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
      @Parameter(hidden = true) @TodakUserId Long memberId,
      @Parameter(description = "일기 반응 요청 데이터", required = true) @Valid @RequestBody
          PublicDiaryReactionRequest request) {
    log.info("일기장 반응 토글을 요청합니다");
    try {
      reactionService.toggleReactionStatus(memberId, request);
    } catch (DataIntegrityViolationException exception) {
      throw new DiaryReactionExistException(
          DiaryReactionErrorSpec.DIARY_REACTION_EXIST,
          memberId,
          request.publicDiaryId(),
          request.reactionType());
    }
    log.info("일기장 반응 토글을 성공적으로 마쳤습니다.");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
