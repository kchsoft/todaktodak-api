package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "나의 공개된 일기 목록 조회 응답")
public record MySharedDiaryPaginationResponse(
    @Schema(description = "조회된 공개 일기 목록") List<MySharedDiaryPreviewProjection> sharedDiaries,
    @Schema(description = "다음 페이지 조회를 위한 마지막 일기 ID", example = "2") Long after) {

  public MySharedDiaryPaginationResponse(
      List<MySharedDiaryPreviewProjection> sharedDiaries, Long after) {
    this.sharedDiaries = sharedDiaries;
    this.after = after;
  }

  public static MySharedDiaryPaginationResponse of(List<MySharedDiaryPreviewProjection> previews) {
    Long minId = previews.getLast().getPublicDiaryId();
    return new MySharedDiaryPaginationResponse(previews, minId);
  }
}
