package com.heartsave.todaktodak_api.auth.dto.request;

import static com.heartsave.todaktodak_api.common.security.constant.ConstraintConstant.Member.EMAIL_MAX_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증 및 중복 확인 요청 데이터")
public record EmailCheckRequest(
    @NotBlank(message = "Blank data") @Size(max = EMAIL_MAX_SIZE, message = "Exceed size")
        String email) {}
