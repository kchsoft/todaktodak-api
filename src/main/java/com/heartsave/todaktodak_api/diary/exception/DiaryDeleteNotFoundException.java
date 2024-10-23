package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public class DiaryDeleteNotFoundException extends BaseException {

  public DiaryDeleteNotFoundException(ErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(
        errorSpec,
        "일기 삭제에 실패했습니다.",
        String.format(
            "%s : 사용자의 일기를 찾을 수 없습니다. [ memberId=%d, diaryId=%d ]",
            errorSpec.getDescription(), memberId, diaryId));
  }
}
