package com.heartsave.todaktodak_api.member.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public class MemberException extends BaseException {

  protected MemberException(
      ErrorSpec errorSpec, String clientMessage, String debugMessage, Long memberId) {
    super(errorSpec, clientMessage, debugMessage);
    getErrorField().add("memberId", memberId);
  }
}
