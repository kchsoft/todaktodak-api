package com.heartsave.todaktodak_api.diary.entity.projection;

import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import java.time.LocalDate;

public interface DiaryViewDetailProjection {
  Long getId();

  DiaryEmotion getEmotion();

  String getContent();

  String getWebtoonImageUrl();

  String getBgmUrl();

  String getAiComment();

  DiaryReactionCountProjection getReactionCount();

  LocalDate getDiaryCreatedTime();
}
