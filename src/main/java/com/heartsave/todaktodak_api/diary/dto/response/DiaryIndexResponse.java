package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
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
public class DiaryIndexResponse {
  private List<DiaryIndexProjection> diaryIndexes;
}
