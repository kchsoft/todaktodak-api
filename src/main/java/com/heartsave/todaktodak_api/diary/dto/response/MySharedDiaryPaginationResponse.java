package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import java.util.List;

public record MySharedDiaryPaginationResponse(
    List<MySharedDiaryPreviewProjection> sharedDiaries, Long after) {

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
