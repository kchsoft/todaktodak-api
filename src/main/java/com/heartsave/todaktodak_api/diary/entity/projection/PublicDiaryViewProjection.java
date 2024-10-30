package com.heartsave.todaktodak_api.diary.entity.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "공개 일기 조회 프로젝션")
public interface PublicDiaryViewProjection {

  @Schema(description = "공개 일기 ID", example = "1")
  Long getPublicDiaryId();

  @Schema(description = "원본 일기 ID", example = "1")
  Long getDiaryId();

  @Schema(description = "작성자 캐릭터 이미지 URL", example = "https://example.com/character/123.jpg")
  String getCharacterImageUrl();

  @Schema(description = "작성자 닉네임", example = "Todak")
  String getNickname();

  @Schema(description = "공개된 일기 내용", example = "오늘은 행복한 하루였습니다...")
  String getPublicContent();

  @Schema(description = "웹툰 이미지 URL", example = "https://example.com/webtoon/123.jpg")
  String getWebtoonImageUrl();

  @Schema(description = "배경음악 URL", example = "https://example.com/bgm/123.mp3")
  String getBgmUrl();

  @Schema(description = "일기 작성 날짜", example = "2024-10-26", type = "string", format = "date")
  LocalDate getDate();
}
