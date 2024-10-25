package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "연월 일기 작성 현황 API 응답 객체")
public class DiaryIndexResponse {
  private final List<DiaryIndexProjection> diaryIndexes;
}
