package com.heartsave.todaktodak_api.domain.auth.dto.request;

import static com.heartsave.todaktodak_api.common.constant.TodakConstraintConstant.Member.EMAIL_MAX_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "OTP 검증 데이터")
public record EmailOtpCheckRequest(
    @NotBlank(message = "Blank data")
        @Pattern(
            regexp = "^[^@.]+@[^@.]+\\.[^@.]+$",
            message = "올바른 이메일 형식이 아닙니다") // @와 . 사이에 문자 최소 1개 존재
        @Size(max = EMAIL_MAX_SIZE, message = "Exceed size")
        String email,
    @NotBlank(message = "Blank data") String emailOtp) {}
