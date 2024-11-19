package com.heartsave.todaktodak_api.diary.entity.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.heartsave.todaktodak_api.common.constant.CoreConstant.TIME_FORMAT;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "연월 일기 작성 현황 API 응답 데이터")
public interface DiaryIndexProjection {

  @JsonProperty("diaryId")
  Long getId();

  @JsonProperty("date")
  @JsonFormat(pattern = TIME_FORMAT.ISO_DATETIME_WITH_MILLISECONDS, timezone = "UTC")
  Instant getDiaryCreatedTime();
}
