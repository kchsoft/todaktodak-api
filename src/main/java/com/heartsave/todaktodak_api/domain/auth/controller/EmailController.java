package com.heartsave.todaktodak_api.domain.auth.controller;

import com.heartsave.todaktodak_api.domain.auth.dto.request.EmailCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.EmailOtpCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.exception.AuthException;
import com.heartsave.todaktodak_api.domain.auth.service.EmailService;
import com.heartsave.todaktodak_api.common.exception.errorspec.auth.AuthErrorSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/email")
public class EmailController {
  private final EmailService emailService;

  @Operation(summary = "이메일 인증을 위한 OTP 전송")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "전송 완료"),
        @ApiResponse(responseCode = "409", description = "이미 가입된 이메일"),
        @ApiResponse(responseCode = "503", description = "전송 실패")
      })
  @PostMapping
  public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailCheckRequest dto) {
    emailService.sendOtp(dto);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "이메일 OTP 인증 번호 확인")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "인증 완료"),
        @ApiResponse(responseCode = "409", description = "인증번호 오류")
      })
  @PostMapping("/otp")
  public ResponseEntity<Void> verifyEmailOtp(@Valid @RequestBody EmailOtpCheckRequest dto) {
    boolean isVerified = emailService.verifyOtp(dto);
    if (isVerified) return ResponseEntity.noContent().build();
    else throw new AuthException(AuthErrorSpec.INCORRECT_EMAIL_OTP);
  }
}
