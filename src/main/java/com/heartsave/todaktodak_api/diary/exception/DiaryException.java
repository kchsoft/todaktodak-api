package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;
import lombok.Getter;

@Getter
public abstract class DiaryException extends BaseException {

  protected DiaryException(
      ErrorSpec errorSpec, String clientMessage, String explain, Long memberId, Long diaryId) {
    super(errorSpec, clientMessage, getLog(errorSpec, explain, memberId, diaryId));
  }

  protected DiaryException(
      ErrorSpec errorSpec, String clientMessage, String explain, Long memberId) {
    super(errorSpec, clientMessage, getLog(errorSpec, explain, memberId));
  }

  private static String getLog(ErrorSpec errorSpec, String explain, Long memberId, Long diaryId) {
    return String.format(
        "%s : %s [ memberId=%d, diaryId=%d ]", errorSpec.name(), explain, memberId, diaryId);
  }

  private static String getLog(ErrorSpec errorSpec, String explain, Long memberId) {
    return String.format("%s : %s [ memberId=%d ]", errorSpec.name(), explain, memberId);
  }
}
