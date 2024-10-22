package com.heartsave.todaktodak_api.common;

import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.LocalDateTime;

public class BaseTestObject {

  private static final String DUMMY_STRING_CONTENT =
      "이렇게 한국말을 적으면 그래도 글자 수가 좀 더 많이 올라가지 않을까? 왜냐하면 더 많은 bit를 사용하기 때문이지 그러나 utf-8로 인코딩을 한다면 영어나 한글이나 같은 단위로 쪼개져 계산이 되기 때문에 어차피 비슷할 수 도 있겠구나 그렇다면 이제부터 무지성 영어를 눌러야겠다."
          + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

  public static MemberEntity createMemberEntity() {
    return MemberEntity.builder()
        .loginId("TEST_LOGIN_ID")
        .password("TEST_PASSWORD")
        .email("TEST_EMAIL@kakao.com")
        .authType(AuthType.BASE)
        .nickname("TEST_NICKNAME")
        .characterInfo("{\"TEST_CHARACTER_INFO\":\"test-character-info\"}")
        .characterImageUrl("http://s3-url/test-character-image")
        .build();
  }

  public static DiaryEntity createDiaryEntity(MemberEntity member) {
    return DiaryEntity.builder()
        .emotion(DiaryEmotion.JOY)
        .diaryCreatedAt(LocalDateTime.of(2024, 10, 22, 9, 55))
        .content("TEST_DIARY_CONTENT : need to fill min Len = " + DUMMY_STRING_CONTENT)
        .memberEntity(member)
        .webtoonImageUrl("http://s3-url/test-webtoon")
        .bgmUrl("http://s3-url/test-bgm")
        .aiComment("http://s3-url/test-comment")
        .build();
  }
}
