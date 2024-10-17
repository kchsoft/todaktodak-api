package com.heartsave.todaktodak_api.diary.dto.request;

import static com.heartsave.todaktodak_api.diary.common.DiaryContentConstraintConstant.DIARY_CONTENT_MAX_SIZE;
import static com.heartsave.todaktodak_api.diary.common.DiaryContentConstraintConstant.DIARY_CONTENT_MIN_SIZE;
import static com.heartsave.todaktodak_api.diary.common.DiaryContentConstraintConstant.DIARY_PUBLIC_CONTENT_MAX_SIZE;

import com.heartsave.todaktodak_api.diary.common.DiaryEmotion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DiaryWriteRequest {
  @PastOrPresent private LocalDateTime date;

  @NotNull private DiaryEmotion emotion;

  @Size(min = DIARY_CONTENT_MIN_SIZE, max = DIARY_CONTENT_MAX_SIZE) // 임시 범위
  private String content;

  @Size(max = DIARY_PUBLIC_CONTENT_MAX_SIZE) // 임시 범위
  private String publicContent;

  @NotNull private Boolean isPublic;
}
