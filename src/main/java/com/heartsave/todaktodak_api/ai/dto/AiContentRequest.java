package com.heartsave.todaktodak_api.ai.dto;

import com.heartsave.todaktodak_api.diary.common.DiaryEmotion;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AiContentRequest { // springboot request to ai server
  private String content;
  private Long id;
  private DiaryEmotion emotion;
}
