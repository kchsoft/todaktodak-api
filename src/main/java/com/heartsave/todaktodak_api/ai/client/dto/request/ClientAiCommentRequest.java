package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import lombok.Getter;

@Getter
public class ClientAiCommentRequest {
  private final String content;

  private ClientAiCommentRequest(DiaryEntity diary) {
    this.content = diary.getContent();
  }

  public static ClientAiCommentRequest of(DiaryEntity diary) {
    return new ClientAiCommentRequest(diary);
  }
}
