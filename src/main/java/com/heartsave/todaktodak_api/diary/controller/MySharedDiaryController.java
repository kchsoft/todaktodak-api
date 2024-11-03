package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.service.MySharedDiaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "나의 공개된 일기장", description = "나의 공개된 일기장 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/diary/my/shared")
public class MySharedDiaryController {

  private final MySharedDiaryService mySharedDiaryService;

  @GetMapping("/shared")
  public ResponseEntity<MySharedDiaryPaginationResponse> getMySharedDiaryPreviews(
      @AuthenticationPrincipal TodakUser principal,
      @Valid @Min(0L) @RequestParam(name = "after", defaultValue = "0") Long publicDiaryId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(mySharedDiaryService.getPagination(principal, publicDiaryId));
  }
}
