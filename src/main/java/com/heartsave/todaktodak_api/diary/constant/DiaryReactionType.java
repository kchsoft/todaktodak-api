package com.heartsave.todaktodak_api.diary.constant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일기 반응 타입")
public enum DiaryReactionType {
  @Schema(description = "좋아요", example = "like")
  LIKE("like"),

  @Schema(description = "놀라워요", example = "surprised")
  SURPRISED("surprised"),

  @Schema(description = "공감해요", example = "empathize")
  EMPATHIZE("empathize"),

  @Schema(description = "응원해요", example = "cheering")
  CHEERING("cheering");

  private final String reaction;

  DiaryReactionType(String reaction) {
    this.reaction = reaction;
  }
}
