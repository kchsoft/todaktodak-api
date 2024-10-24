package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorSpec;

public class DiaryDeleteNotFoundException extends DiaryException {

  public DiaryDeleteNotFoundException(ErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(
        errorSpec,
        "일기 삭제에 실패했습니다.",
        "사용자가 삭제하려는 일기의 작성자가 아니거나, 삭제하려는 일기가 없습니다.",
        memberId,
        diaryId);
  }
}
