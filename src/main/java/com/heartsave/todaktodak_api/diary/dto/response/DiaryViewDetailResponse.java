package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryViewDetailResponse {
  private Long diaryId;
  private DiaryEmotion emotion;
  private String content;
  private String webtoonImageUrl;
  private String bgmUrl;
  private String aiComment;
  private DiaryReactionCountProjection reactionCount;
  private LocalDate date;
}
