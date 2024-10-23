package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public class DiaryDailyWritingLimitExceedException extends BaseException {

  public DiaryDailyWritingLimitExceedException(ErrorSpec errorSpec, Long memberId) {
    super(
        errorSpec,
        "하루 일기 작성 횟수를 초과할 수 없습니다.",
        errorSpec.getDescription() + " : 하루 일기 작성 초과 회원 ID = " + memberId);
  }
}
