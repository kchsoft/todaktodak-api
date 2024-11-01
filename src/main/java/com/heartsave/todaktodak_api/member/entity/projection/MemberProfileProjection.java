package com.heartsave.todaktodak_api.member.entity.projection;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "회원 정보 DB 조회 데이터")
public interface MemberProfileProjection {
  String getNickname();

  String getEmail();

  String getCharacterImageUrl();
}
