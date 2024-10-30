package com.heartsave.todaktodak_api.auth.controller;

import com.heartsave.todaktodak_api.auth.dto.request.EmailCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.request.EmailOtpCheckRequest;
import com.heartsave.todaktodak_api.auth.exception.AuthException;
import com.heartsave.todaktodak_api.auth.service.EmailService;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
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

  @PostMapping
  public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailCheckRequest dto) {
    emailService.sendOtp(dto);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/otp")
  public ResponseEntity<Void> verifyEmailOtp(@Valid @RequestBody EmailOtpCheckRequest dto) {
    boolean isVerified = emailService.verifyOtp(dto);
    if (isVerified) return ResponseEntity.noContent().build();
    else throw new AuthException(AuthErrorSpec.INCORRECT_EMAIL_OTP);
  }
}
