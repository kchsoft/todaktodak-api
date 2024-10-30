package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.dto.PublicDiaryViewDetail;
import java.util.List;
import lombok.Getter;

@Getter
public class PublicDiaryViewDetailResponse {
  private List<PublicDiaryViewDetail> diaries;
  private Long after;

  public PublicDiaryViewDetailResponse() {
    after = Long.MAX_VALUE;
  }

  public void addViewDetail(PublicDiaryViewDetail viewDetail) {
    diaries.add(viewDetail);
    after = Math.min(viewDetail.getPublicDiaryId(), after);
  }
}
