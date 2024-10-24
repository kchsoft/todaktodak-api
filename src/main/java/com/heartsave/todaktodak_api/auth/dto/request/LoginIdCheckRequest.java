package com.heartsave.todaktodak_api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 ID 중복 확인 요청 데이터")
public record LoginIdCheckRequest(
    @Schema(example = "todak", description = "최대 30자")
        @NotBlank(message = "Blank data")
        @Size(max = 30, message = "Exceed size")
        String loginId) {}
