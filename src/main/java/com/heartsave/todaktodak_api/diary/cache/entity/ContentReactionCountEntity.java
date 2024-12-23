package com.heartsave.todaktodak_api.diary.cache.entity;

import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentReactionCountEntity {
  private String orderPivot;
  private Long publicDiaryId;
  private Long diaryId;
  private String characterImageUrl;
  private String nickname;
  private String publicContent;
  private List<String> webtoonImageUrls;
  private String bgmUrl;
  private Instant date;
  private DiaryReactionCount reactionCount;

  private ContentReactionCountEntity(PublicDiaryContentProjection projections) {
    this.publicDiaryId = projections.getPublicDiaryId();
    this.diaryId = projections.getDiaryId();
    this.characterImageUrl = projections.getCharacterImageUrl();
    this.nickname = projections.getNickname();
    this.publicContent = projections.getPublicContent();
    this.webtoonImageUrls = List.of(projections.getWebtoonImageUrl());
    this.bgmUrl = projections.getBgmUrl();
    this.date = projections.getDate();
  }

  public static ContentReactionCountEntity createFrom(PublicDiaryContentProjection projections) {
    return new ContentReactionCountEntity(projections);
  }

  public void applyReactionCount(DiaryReactionCount count) {
    this.reactionCount = count;
  }

  public void updateWebtoonImageUrls(List<String> urls) {
    this.webtoonImageUrls = urls;
  }

  public void updateBgmUrl(String url) {
    this.bgmUrl = url;
  }

  public void updateCharacterImageUrl(String url) {
    this.characterImageUrl = url;
  }

  public void setOrderPivot(String orderPivot) {
    this.orderPivot = orderPivot;
  }

  public Long getMilsTimeStamp() {
    return getDate().toEpochMilli();
  }
}
