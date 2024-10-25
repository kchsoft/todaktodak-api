package com.heartsave.todaktodak_api.auth.controller;

import com.heartsave.todaktodak_api.auth.dto.request.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.response.NicknameCheckRequest;
import com.heartsave.todaktodak_api.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;

  @Operation(summary = "닉네임 중복 확인")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "유일한 닉네임"),
        @ApiResponse(responseCode = "400", description = "유효성 검사 실패"),
        @ApiResponse(responseCode = "409", description = "중복 닉네임 존재")
      })
  @PostMapping("/nickname")
  public ResponseEntity<Void> checkNickname(@Valid @RequestBody NicknameCheckRequest dto) {
    if (!authService.isDuplicatedNickname(dto)) return ResponseEntity.noContent().build();
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  @Operation(summary = "로그인 ID 중복 확인")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "유일한 아이디"),
        @ApiResponse(responseCode = "400", description = "유효성 검사 실패"),
        @ApiResponse(responseCode = "409", description = "중복 아이디 존재")
      })
  @PostMapping("/login-id")
  public ResponseEntity<Void> checkLoginId(@Valid @RequestBody LoginIdCheckRequest dto) {
    if (!authService.isDuplicatedLoginId(dto)) return ResponseEntity.noContent().build();
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }
}
