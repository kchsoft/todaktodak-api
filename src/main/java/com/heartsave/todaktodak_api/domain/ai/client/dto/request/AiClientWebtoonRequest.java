package com.heartsave.todaktodak_api.domain.ai.client.dto.request;

import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import java.time.Instant;
import lombok.Getter;

@Getter
public class AiClientWebtoonRequest extends AiClientRequest {
  private final String memberId;

  private final Instant date;

  private final String content;
  private final String characterInfo;
  private final Integer seedNum;
  private final String characterStyle;

  private AiClientWebtoonRequest(DiaryEntity diary, MemberEntity member) {
    this.memberId = String.valueOf(member.getId());
    this.date = diary.getDiaryCreatedTime();
    this.content = diary.getContent();
    this.characterInfo = member.getCharacterInfo();
    this.seedNum = member.getCharacterSeed();
    this.characterStyle = member.getCharacterStyle();
  }

  public static AiClientWebtoonRequest of(DiaryEntity diary, MemberEntity member) {
    return new AiClientWebtoonRequest(diary, member);
  }
}
