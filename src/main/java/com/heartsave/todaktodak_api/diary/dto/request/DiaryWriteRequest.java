package com.heartsave.todaktodak_api.diary.dto.request;

import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_CONTENT_MAX_SIZE;
import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_CONTENT_MIN_SIZE;

import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "일기 작성 요청 데이터")
public class DiaryWriteRequest {

  @PastOrPresent(message = "Diary Writing Date is Future")
  private LocalDateTime date;

  @NotNull(message = "DiaryEmotion is Null")
  private DiaryEmotion emotion;

  @Size(
      min = DIARY_CONTENT_MIN_SIZE,
      max = DIARY_CONTENT_MAX_SIZE,
      message = "Diary Content Length Out Of Range") // 임시 범위
  private String content;
}
