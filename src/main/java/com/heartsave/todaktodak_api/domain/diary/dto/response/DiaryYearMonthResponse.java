package com.heartsave.todaktodak_api.domain.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryYearMonthProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "연월 일기 작성 현황 API 응답 객체")
public class DiaryYearMonthResponse {
  @Schema(description = "일기 연월 작성 현황 목록")
  @JsonProperty("diaryIndexes")
  private List<DiaryYearMonthProjection> diaryYearMonths;
}
