package com.heartsave.todaktodak_api.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "캐릭터 등록 API 응답 객체")
public record CharacterRegisterResponse(
    @Schema(description = "일기 작성 권한이 생긴 인증 토큰") String accessToken) {}
