package com.heartsave.todaktodak_api.diary.entity.projection;

import java.time.LocalDate;

public interface PublicDiaryViewProjection {
  Long getPublicDiaryId();

  Long getDiaryId();

  String getCharacterImageUrl();

  String getNickname();

  String getPublicContent();

  String getWebtoonImageUrl();

  String getBgmUrl();

  LocalDate getDate();
}
