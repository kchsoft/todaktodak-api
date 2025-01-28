package com.heartsave.todaktodak_api.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "토큰 재발급 API 응답 객체")
public record TokenReissueResponse(@Schema(description = "재발급된 인증 토큰") String accessToken) {}
