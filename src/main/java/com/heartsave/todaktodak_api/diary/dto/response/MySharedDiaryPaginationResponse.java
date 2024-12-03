package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "나의 공개된 일기 목록 조회 응답")
public record MySharedDiaryPaginationResponse(
    @Schema(description = "조회된 공개 일기 목록") List<MySharedDiaryPreviewProjection> sharedDiaries,
    @Schema(description = "조회할 수 있는 다음 페이지 존재 여부 (true: 더 이상 조회 불가)", example = "false")
        Boolean isEnd) {

  public MySharedDiaryPaginationResponse(
      List<MySharedDiaryPreviewProjection> sharedDiaries, Boolean isEnd) {
    this.sharedDiaries = sharedDiaries;
    this.isEnd = isEnd;
  }

  public static MySharedDiaryPaginationResponse of(List<MySharedDiaryPreviewProjection> previews) {
    return new MySharedDiaryPaginationResponse(previews, previews.isEmpty());
  }
}
