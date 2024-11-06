package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "나의 공개된 일기 목록 조회 응답")
public record MySharedDiaryPaginationResponse(
    @Schema(description = "조회된 공개 일기 목록") List<MySharedDiaryPreviewProjection> sharedDiaries,
    @Schema(description = "다음 페이지 조회를 위한 마지막 일기 ID", example = "2") Long after,
    @Schema(description = "조회할 수 있는 다음 페이지 존재 여부 (true: 더 이상 조회 불가)", example = "false")
        Boolean isEnd) {

  public MySharedDiaryPaginationResponse(
      List<MySharedDiaryPreviewProjection> sharedDiaries, Long after, Boolean isEnd) {
    this.sharedDiaries = sharedDiaries;
    this.after = after;
    this.isEnd = isEnd;
  }

  public static MySharedDiaryPaginationResponse of(List<MySharedDiaryPreviewProjection> previews) {
    Long minId;
    Boolean isEnd = false;
    if (previews.isEmpty()) {
      minId = 1L;
      isEnd = true;
    } else minId = previews.getLast().getPublicDiaryId();

    return new MySharedDiaryPaginationResponse(previews, minId, isEnd);
  }
}
