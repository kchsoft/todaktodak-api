package com.heartsave.todaktodak_api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 중복 확인 요청 데이터")
public record NicknameCheckRequest(
    @Schema(example = "todak", description = "최대 50자")
        @NotBlank(message = "Blank data")
        @Size(max = 50, message = "Exceed size")
        String nickname) {}
