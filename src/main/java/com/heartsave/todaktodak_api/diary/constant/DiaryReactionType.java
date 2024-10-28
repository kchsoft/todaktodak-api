package com.heartsave.todaktodak_api.diary.constant;

public enum DiaryReactionType {
  LIKE("like"),
  SURPRISED("surprised"),
  EMPATHIZE("empathize"),
  CHEERING("cheering");

  private final String reaction;

  DiaryReactionType(String reaction) {
    this.reaction = reaction;
  }
}
