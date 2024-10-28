package com.heartsave.todaktodak_api.diary.controller;

import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.PUBLIC_DIARY_CONTENT_MAX_SIZE;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "일기장", description = "공개 일기장 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/public")
public class PublicDiaryController {

  private PublicDiaryService publicDiaryService;

  @PostMapping
  public ResponseEntity<Void> writePublicContent(
      @AuthenticationPrincipal TodakUser principal,
      @Valid @Size(max = PUBLIC_DIARY_CONTENT_MAX_SIZE) String publicContent,
      @Valid @Min(1L) Long diaryId) {
    publicDiaryService.write(principal, publicContent, diaryId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
