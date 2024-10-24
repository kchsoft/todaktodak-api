package com.heartsave.todaktodak_api.member.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public abstract class MemberException extends BaseException {

  protected MemberException(
      ErrorSpec errorSpec, String clientMessage, String explain, Long memberId) {
    super(errorSpec, clientMessage, getLog(errorSpec, explain, memberId));
  }

  private static String getLog(ErrorSpec errorSpec, String explain, Long memberId) {
    return String.format("%s : %s [ memberId=%d ]", errorSpec.name(), explain, memberId);
  }
}
