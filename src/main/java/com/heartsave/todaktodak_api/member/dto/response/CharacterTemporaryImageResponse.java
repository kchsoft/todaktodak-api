package com.heartsave.todaktodak_api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "회원 캐릭터 생성 과정에서 기존에 생성된 회원 캐릭터 이미지 응답 객체")
public record CharacterTemporaryImageResponse(
    @Schema(example = "character/123", description = "회원 캐릭터 이미지 주소") String characterImageUrl) {}
