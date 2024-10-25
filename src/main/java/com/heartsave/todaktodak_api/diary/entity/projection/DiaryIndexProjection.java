package com.heartsave.todaktodak_api.diary.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "연월 일기 작성 현황 API 응답 데이터")
public interface DiaryIndexProjection {

  @JsonProperty("diaryId")
  Long getId();

  @JsonProperty("date")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  LocalDateTime getDiaryCreatedTime();
}
