package com.heartsave.todaktodak_api.diary.controller;

import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/diary")
public class DiaryController {

  private final DiaryService diaryService;

  @PostMapping("/my")
  public ResponseEntity<Void> writeDiary(
      @AuthenticationPrincipal OAuth2User auth,
      @Valid @RequestBody DiaryWriteRequest writeRequest) {
    diaryService.write(auth, writeRequest);
    log.info("일기 작성을 완료하였습니다.");
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
