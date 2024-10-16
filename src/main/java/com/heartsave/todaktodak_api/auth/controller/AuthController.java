package com.heartsave.todaktodak_api.auth.controller;

import com.heartsave.todaktodak_api.auth.dto.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.NicknameCheckRequest;
import com.heartsave.todaktodak_api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/nickname")
  ResponseEntity<Void> checkNickname(@Valid @RequestBody NicknameCheckRequest dto) {
    if (!authService.isDuplicatedNickname(dto)) return ResponseEntity.noContent().build();
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  @PostMapping("/login-id")
  ResponseEntity<Void> checkLoginId(@Valid @RequestBody LoginIdCheckRequest dto) {
    if (!authService.isDuplicatedLoginId(dto)) return ResponseEntity.noContent().build();
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }
}
