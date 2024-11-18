package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;

public class DiaryReactionExistException extends DiaryReactionException {

  public DiaryReactionExistException(
      ErrorSpec errorSpec, Long memberId, Long diaryId, DiaryReactionType type) {
    super(
        errorSpec,
        ErrorFieldBuilder.builder()
            .add("memberId", memberId)
            .add("diaryId", diaryId)
            .add("reactionType", type)
            .build());
  }
}
