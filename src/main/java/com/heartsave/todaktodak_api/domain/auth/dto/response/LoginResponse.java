package com.heartsave.todaktodak_api.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "로그인 API 응답 객체")
public record LoginResponse(
    @Schema(description = "로그인 아이디") String username,
    @Schema(description = "인증 토큰") String accessToken) {}
