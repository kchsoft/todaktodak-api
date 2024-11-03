package com.heartsave.todaktodak_api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "회원 정보 API 응답 객체")
public record MemberProfileResponse(
    @Schema(example = "todak", description = "회원 닉네임") String nickname,
    @Schema(example = "https://example/character/123.webp", description = "회원 캐릭터 이미지 주소")
        String characterImageUrl,
    @Schema(example = "todak@kakao.com", description = "회원 이메일") String email) {}
