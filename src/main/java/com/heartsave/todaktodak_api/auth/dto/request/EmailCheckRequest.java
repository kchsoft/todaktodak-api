package com.heartsave.todaktodak_api.auth.dto.request;

import static com.heartsave.todaktodak_api.common.security.constant.ConstraintConstant.Member.EMAIL_MAX_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증 및 중복 확인 요청 데이터")
public record EmailCheckRequest(
    @NotBlank(message = "Blank data")
        @Pattern(
            regexp = "^[^@.]+@[^@.]+\\.[^@.]+$",
            message = "올바른 이메일 형식이 아닙니다") // @와 . 사이에 문자 최소 1개 존재
        @Size(max = EMAIL_MAX_SIZE, message = "Exceed size")
        String email) {}
