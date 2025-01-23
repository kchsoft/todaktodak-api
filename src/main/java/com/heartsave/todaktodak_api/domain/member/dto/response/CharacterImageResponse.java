package com.heartsave.todaktodak_api.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "회원 캐릭터 이미지 응답 객체")
public record CharacterImageResponse(
    @Schema(example = "temp_profile.webp", description = "생성됐지만 미등록된 캐릭터 이미지 주소")
        String tempCharacterImageUrl,
    @Schema(example = "profile.webp", description = "등록된 회원 캐릭터 이미지 주소")
        String characterImageUrl) {}
