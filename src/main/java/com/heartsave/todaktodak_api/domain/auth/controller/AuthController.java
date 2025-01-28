package com.heartsave.todaktodak_api.domain.auth.controller;

import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.NicknameCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.response.TokenReissueResponse;
import com.heartsave.todaktodak_api.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    if (!authService.isDuplicatedNickname(dto.nickname()))
      return ResponseEntity.noContent().build();
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
    if (!authService.isDuplicatedLoginId(dto.loginId())) return ResponseEntity.noContent().build();
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  @Operation(summary = "회원가입")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검사 실패"),
        @ApiResponse(responseCode = "409", description = "중복 정보 존재")
      })
  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest dto) {
    authService.signUp(dto);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(summary = "토큰 재발급")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 재발급 실패")
      })
  @PostMapping("/refresh-token")
  public ResponseEntity<TokenReissueResponse> reissueToken(
      HttpServletRequest request, HttpServletResponse response) {
    return ResponseEntity.ok(authService.reissueToken(request, response));
  }

  @Operation(summary = "로그인")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "로그인 실패")
      })
  @PostMapping("/login")
  public void login(@Valid @RequestBody LoginRequest request) {
    // JwtAuthFilter 참조
  }
}
