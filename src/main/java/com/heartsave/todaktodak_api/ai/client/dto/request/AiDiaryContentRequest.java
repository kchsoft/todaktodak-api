package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AiDiaryContentRequest { // springboot request to ai server
  private String content;
  private Long id;
  private DiaryEmotion emotion;
}
