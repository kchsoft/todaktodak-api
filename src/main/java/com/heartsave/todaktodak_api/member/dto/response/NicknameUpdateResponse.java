package com.heartsave.todaktodak_api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NicknameUpdateResponse(
    @Schema(example = "todak", description = "변경된 닉네임") String nickname) {}
