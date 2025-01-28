package com.heartsave.todaktodak_api.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "닉네임 변경 API 응답 객체")
public record NicknameUpdateResponse(
    @Schema(example = "todak", description = "변경된 닉네임") String nickname) {}
