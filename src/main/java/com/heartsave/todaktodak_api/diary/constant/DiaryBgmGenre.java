package com.heartsave.todaktodak_api.diary.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DiaryBgmGenre {
  POP("pop"),
  ROCK("rock"),
  ACOUSTIC("acoustic"),
  JAZZ("jazz"),
  CLASSICAL("classical"),
  EDM("edm");

  private final String genre;

  DiaryBgmGenre(String genre) {
    this.genre = genre;
  }

  @JsonValue
  public String getGenre() {
    return genre;
  }
}
