package com.heartsave.todaktodak_api.member.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public class MemberNotFoundException extends MemberException {

  public MemberNotFoundException(ErrorSpec errorSpec, Long memberId) {
    super(errorSpec, "회원님의 정보를 찾을 수 없습니다.", "사용자의 ID와 일치하는 회원 정보가 없습니다.", memberId);
  }
}
