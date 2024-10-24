package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public class DiaryDailyWritingLimitExceedException extends DiaryException {

  public DiaryDailyWritingLimitExceedException(ErrorSpec errorSpec, Long memberId) {
    super(
        errorSpec, "하루 일기 작성 횟수를 초과할 수 없습니다.", "사용자가 하루 일기 작성 횟수를 초과하여 일기 작성을 시도하였습니다.", memberId);
  }
}
