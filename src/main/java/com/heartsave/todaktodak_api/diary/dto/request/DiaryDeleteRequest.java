package com.heartsave.todaktodak_api.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
@Schema(description = "일기 삭제 요청 데이터")
public class DiaryDeleteRequest {
  @Positive(message = "1 이상의 정수가 와야 합니다.")
  @Min(value = 1)
  private Long diaryId;
}
