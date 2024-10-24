package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;
import lombok.Getter;

@Getter
public abstract class DiaryException extends BaseException {

  protected DiaryException(
      ErrorSpec errorSpec, String clientMessage, String debugMessage, Long memberId, Long diaryId) {
    super(errorSpec, clientMessage, debugMessage);
    getErrorField().add("memberId", memberId).add("diaryId", diaryId);
  }

  protected DiaryException(
      ErrorSpec errorSpec, String clientMessage, String debugMessage, Long memberId) {
    super(errorSpec, clientMessage, debugMessage);
    getErrorField().add("memberId", memberId);
  }
}
