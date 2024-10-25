package com.heartsave.todaktodak_api.member.exception;


import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;

public class MemberException extends BaseException {

  protected MemberException(MemberErrorSpec errorSpec, Long memberId) {
    super(errorSpec, ErrorFieldBuilder.builder().add("memberId", memberId).build());
  }
}
