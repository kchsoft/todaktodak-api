package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.Instant;
import lombok.Getter;

@Getter
public class ClientWebtoonRequest extends ClientDomainInfo {
  private final String memberId;

  private final Instant date;

  private final String content;
  private final String characterInfo;
  private final Integer seedNum;
  private final String characterStyle;

  private ClientWebtoonRequest(DiaryEntity diary, MemberEntity member) {
    this.memberId = String.valueOf(member.getId());
    this.date = diary.getDiaryCreatedTime();
    this.content = diary.getContent();
    this.characterInfo = member.getCharacterInfo();
    this.seedNum = member.getCharacterSeed();
    this.characterStyle = member.getCharacterStyle();
  }

  public static ClientWebtoonRequest of(DiaryEntity diary, MemberEntity member) {
    return new ClientWebtoonRequest(diary, member);
  }
}
