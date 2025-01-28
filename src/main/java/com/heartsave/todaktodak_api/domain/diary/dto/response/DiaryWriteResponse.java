package com.heartsave.todaktodak_api.domain.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "일기 작성 응답")
public class DiaryWriteResponse {

  @Schema(description = "AI가 작성한 코멘트", example = "오늘 하루도 수고 많으셨어요!")
  private String aiComment;
}
