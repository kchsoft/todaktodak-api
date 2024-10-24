package com.heartsave.todaktodak_api.common;

import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.LocalDateTime;

public class BaseTestEntity {

  public static final String DUMMY_STRING_CONTENT =
      "이렇게 한국말을 적으면 그래도 글자 수가 좀 더 많이 올라가지 않을까? 왜냐하면 더 많은 bit를 사용하기 때문이지 그러나 utf-8로 인코딩을 한다면 영어나 한글이나 같은 단위로 쪼개져 계산이 되기 때문에 어차피 비슷할 수 도 있겠구나 그렇다면 이제부터 무지성 영어를 눌러야겠다."
          + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbb";
  private static String TEST_LOGIN_ID = "TEST_LOGIN_ID";
  private static String TEST_PASSWORD = "TEST_PASSWORD";
  private static String TEST_EMAIL = "TEST_EMAIL@kakao.com";
  private static String TEST_NICKNAME = "TEST_NICKNAME";
  private static String TEST_CHARACTER_INFO = "{\"TEST_CHARACTER_INFO\":\"test-character-info\"}";
  private static String TEST_CHARACTER_IMAGE_URL = "http://s3-url/test-character-image";
  private static LocalDateTime TEST_LOCAL_DATE_TIME = LocalDateTime.of(2024, 10, 22, 9, 55);
  private static String TEST_DIARY_CONTENT =
      "TEST_DIARY_CONTENT : need to fill min Len = " + DUMMY_STRING_CONTENT;
  private static String TEST_WEBTOON_URL = "http://s3-url/test-webtoon";
  private static String TEST_BGM_URL = "http://s3-url/test-bgm";
  private static String TEST_COMMENT_URL = "http://s3-url/test-comment";

  public static MemberEntity createMember() {
    return MemberEntity.builder()
        .id(1L)
        .loginId(TEST_LOGIN_ID)
        .password(TEST_PASSWORD)
        .email(TEST_EMAIL)
        .authType(AuthType.BASE)
        .nickname(TEST_NICKNAME)
        .characterInfo(TEST_CHARACTER_INFO)
        .characterImageUrl(TEST_CHARACTER_IMAGE_URL)
        .build();
  }

  public static MemberEntity createMemberWithoutId() {
    return MemberEntity.builder()
        .loginId(TEST_LOGIN_ID)
        .password(TEST_PASSWORD)
        .email(TEST_EMAIL)
        .authType(AuthType.BASE)
        .nickname(TEST_NICKNAME)
        .characterInfo(TEST_CHARACTER_INFO)
        .characterImageUrl(TEST_CHARACTER_IMAGE_URL)
        .build();
  }

  public static DiaryEntity createDiary() {
    return DiaryEntity.builder()
        .id(1L)
        .emotion(DiaryEmotion.JOY)
        .diaryCreatedAt(TEST_LOCAL_DATE_TIME)
        .content(TEST_DIARY_CONTENT)
        .memberEntity(createMember())
        .webtoonImageUrl(TEST_WEBTOON_URL)
        .bgmUrl(TEST_BGM_URL)
        .aiComment(TEST_COMMENT_URL)
        .build();
  }

  public static DiaryEntity createDiaryWithMember(MemberEntity member) {
    return DiaryEntity.builder()
        .id(1L)
        .emotion(DiaryEmotion.JOY)
        .diaryCreatedAt(TEST_LOCAL_DATE_TIME)
        .content(TEST_DIARY_CONTENT)
        .memberEntity(member)
        .webtoonImageUrl(TEST_WEBTOON_URL)
        .bgmUrl(TEST_BGM_URL)
        .aiComment(TEST_COMMENT_URL)
        .build();
  }

  public static DiaryEntity createDiaryWithMemberHasNotId(MemberEntity member) {
    return DiaryEntity.builder()
        .emotion(DiaryEmotion.JOY)
        .diaryCreatedAt(TEST_LOCAL_DATE_TIME)
        .content(TEST_DIARY_CONTENT)
        .memberEntity(member)
        .webtoonImageUrl(TEST_WEBTOON_URL)
        .bgmUrl(TEST_BGM_URL)
        .aiComment(TEST_COMMENT_URL)
        .build();
  }
}
