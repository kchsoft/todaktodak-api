package com.heartsave.todaktodak_api.domain.member.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.member.MemberErrorSpec;

public class MemberNotFoundException extends MemberException {

  public MemberNotFoundException(MemberErrorSpec errorSpec, Long memberId) {
    super(errorSpec, memberId);
  }
}
