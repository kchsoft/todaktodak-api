package com.heartsave.todaktodak_api.diary.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일기 감정 상태")
public enum DiaryEmotion {
  @Schema(description = "행복함", example = "happy")
  HAPPY("happy"),

  @Schema(description = "평온함", example = "peaceful")
  PEACEFUL("peaceful"),

  @Schema(description = "생각많음", example = "thoughtful")
  THOUGHTFUL("thoughtful"),

  @Schema(description = "아쉬움", example = "regretful")
  REGRETFUL("regretful"),

  @Schema(description = "씁쓸함", example = "bitter")
  BITTER("bitter");

  private final String emotion;

  DiaryEmotion(String emotion) {
    this.emotion = emotion;
  }

  @JsonValue
  public String getEmotion() {
    return emotion;
  }
}
