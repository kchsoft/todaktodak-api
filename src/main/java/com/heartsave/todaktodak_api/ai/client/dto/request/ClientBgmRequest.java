package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ClientBgmRequest {
  private final Long memberId;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate date;

  private final String content;
  private final DiaryEmotion emotion;

  private ClientBgmRequest(DiaryEntity diary, MemberEntity member) {
    this.memberId = member.getId();
    this.date = diary.getDiaryCreatedTime().toLocalDate();
    this.content = diary.getContent();
    this.emotion = diary.getEmotion();
  }

  public static ClientBgmRequest of(DiaryEntity diary, MemberEntity member) {
    return new ClientBgmRequest(diary, member);
  }
}
