package com.heartsave.todaktodak_api.domain.ai.client.dto.request;

import com.heartsave.todaktodak_api.domain.diary.constant.DiaryBgmGenre;
import com.heartsave.todaktodak_api.domain.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import java.time.Instant;
import lombok.Getter;

@Getter
public class AiClientBgmRequest extends AiClientRequest {
  private final Long memberId;

  private final Instant date;

  private final String content;
  private final DiaryEmotion emotion;
  private final DiaryBgmGenre genre;

  private AiClientBgmRequest(DiaryEntity diary, MemberEntity member) {
    this.memberId = member.getId();
    this.date = diary.getDiaryCreatedTime();
    this.content = diary.getContent();
    this.emotion = diary.getEmotion();
    this.genre = diary.getBgmGenre();
  }

  public static AiClientBgmRequest of(DiaryEntity diary, MemberEntity member) {
    return new AiClientBgmRequest(diary, member);
  }
}
