package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ClientWebtoonRequest {
  private final Long memberId;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate date;

  private final String content;
  private final String characterInfo;
  private final Integer seedNum;

  // Todo : characterStyle enum 추가

  private ClientWebtoonRequest(DiaryEntity diary, MemberEntity member) {
    this.memberId = member.getId();
    this.date = diary.getDiaryCreatedTime().toLocalDate();
    this.content = diary.getContent();
    this.characterInfo = (String) member.getCharacterInfo();
    this.seedNum = member.getCharacterSeed();
  }

  public static ClientWebtoonRequest of(DiaryEntity diary, MemberEntity member) {
    return new ClientWebtoonRequest(diary, member);
  }
}
