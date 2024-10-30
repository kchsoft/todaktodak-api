package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.dto.PublicDiaryViewDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Schema(description = "공개 일기 상세 조회 응답")
@Getter
public class PublicDiaryViewDetailResponse {

  @Schema(description = "조회된 공개 일기 목록")
  private List<PublicDiaryViewDetail> diaries;

  @Schema(description = "다음 페이지 조회를 위한 마지막 일기 ID", example = "97")
  private Long after;

  public PublicDiaryViewDetailResponse() {
    after = Long.MAX_VALUE;
  }

  public void addViewDetail(PublicDiaryViewDetail viewDetail) {
    diaries.add(viewDetail);
    after = Math.min(viewDetail.getPublicDiaryId(), after);
  }
}
