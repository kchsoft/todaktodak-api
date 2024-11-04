package com.heartsave.todaktodak_api.member.dto.request;

import static com.heartsave.todaktodak_api.common.constant.ConstraintConstant.Member.NICKNAME_MAX_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 변경 요청 데이터")
public record NicknameUpdateRequest(
    @Schema(example = "todak", description = "최대 50자")
        @NotBlank(message = "Blank data")
        @Size(max = NICKNAME_MAX_SIZE, message = "Exceed size")
        String nickname) {}
