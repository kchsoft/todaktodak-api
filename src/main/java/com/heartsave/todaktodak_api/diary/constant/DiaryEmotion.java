package com.heartsave.todaktodak_api.diary.constant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일기 감정 상태")
public enum DiaryEmotion {
  @Schema(description = "즐거움", example = "joy")
  JOY("joy");

  private final String emotion;

  DiaryEmotion(String emotion) {
    this.emotion = emotion;
  }
}
