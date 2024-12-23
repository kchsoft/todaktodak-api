package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.heartsave.todaktodak_api.diary.constant.DiaryBgmGenre;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.Instant;
import lombok.Getter;

@Getter
public class ClientBgmRequest extends ClientDomainInfo {
  private final Long memberId;

  private final Instant date;

  private final String content;
  private final DiaryEmotion emotion;
  private final DiaryBgmGenre genre;

  private ClientBgmRequest(DiaryEntity diary, MemberEntity member) {
    this.memberId = member.getId();
    this.date = diary.getDiaryCreatedTime();
    this.content = diary.getContent();
    this.emotion = diary.getEmotion();
    this.genre = diary.getBgmGenre();
  }

  public static ClientBgmRequest of(DiaryEntity diary, MemberEntity member) {
    return new ClientBgmRequest(diary, member);
  }
}
