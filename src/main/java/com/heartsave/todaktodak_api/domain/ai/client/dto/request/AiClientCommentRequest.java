package com.heartsave.todaktodak_api.domain.ai.client.dto.request;

import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import lombok.Getter;

@Getter
public class AiClientCommentRequest extends AiClientRequest {
  private final String content;

  private AiClientCommentRequest(DiaryEntity diary) {
    this.content = diary.getContent();
  }

  public static AiClientCommentRequest of(DiaryEntity diary) {
    return new AiClientCommentRequest(diary);
  }
}
