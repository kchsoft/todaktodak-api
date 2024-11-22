package com.heartsave.todaktodak_api.common;

import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BaseTestObject {

  // diary
  public static final String DUMMY_STRING_CONTENT =
      "이렇게 한국말을 적으면 그래도 글자 수가 좀 더 많이 올라가지 않을까? 왜냐하면 더 많은 bit를 사용하기 때문이지 그러나 utf-8로 인코딩을 한다면 영어나 한글이나 같은 단위로 쪼개져 계산이 되기 때문에 어차피 비슷할 수 도 있겠구나 그렇다면 이제부터 무지성 영어를 눌러야겠다."
          + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbb";
  public static final String TEST_WEBTOON_KEY_URL = "webtoon/1/2024/11/06";
  public static final String TEST_WEBTOON_URL = "https:test-url/webtoon/1/2024/11/06";
  public static final String TEST_BGM_KEY_URL = "music-ai/1/2024/11/06/bgm.mp3";
  public static final String TEST_BGM_URL = "https:test-url/1/2024/11/06/bgm.mp3";
  private static String TEST_DIARY_CONTENT =
      "TEST_DIARY_CONTENT : need to fill min Len = " + DUMMY_STRING_CONTENT;
  private static String TEST_DEFAULT_WEBTOON_URL = "";
  private static String TEST_DEFAULT_BGM_URL = "";
  private static String TEST_COMMENT = "test-ai-comment";

  // member
  private static String TEST_LOGIN_ID = "TEST_LOGIN_ID";
  private static String TEST_PASSWORD = "TEST_PASSWORD";
  private static String TEST_EMAIL = "TEST_EMAIL@kakao.com";
  private static String TEST_NICKNAME = "TEST_NICKNAME";
  private static String TEST_CHARACTER_INFO = "{\"TEST_CHARACTER_INFO\":\"test-character-info\"}";
  private static String TEST_CHARACTER_IMAGE_URL = "character/profile.webp";
  private static LocalDateTime TEST_LOCAL_DATE_TIME = LocalDateTime.of(2024, 10, 22, 9, 55);
  private static String TEST_CHARACTER_STYLE = "romance";
  private static int TEST_CHARACTER_SEED = 13564;

  public static MemberEntity createTempMember() {
    var member = createMember();
    member.updateRole(TodakRole.ROLE_TEMP.name());
    return member;
  }

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
        .characterStyle(TEST_CHARACTER_STYLE)
        .characterSeed(TEST_CHARACTER_SEED)
        .role(TodakRole.ROLE_USER)
        .build();
  }

  public static MemberEntity createMemberNoId() {
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
        .emotion(DiaryEmotion.HAPPY)
        .diaryCreatedTime(Instant.now())
        .content(TEST_DIARY_CONTENT)
        .memberEntity(createMember())
        .webtoonImageUrl(TEST_DEFAULT_WEBTOON_URL)
        .bgmUrl(TEST_DEFAULT_BGM_URL)
        .aiComment(TEST_COMMENT)
        .build();
  }

  public static DiaryEntity createDiaryWithMember(MemberEntity member) {
    return DiaryEntity.builder()
        .id(1L)
        .emotion(DiaryEmotion.HAPPY)
        .diaryCreatedTime(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .content(TEST_DIARY_CONTENT)
        .memberEntity(member)
        .webtoonImageUrl(TEST_DEFAULT_WEBTOON_URL)
        .bgmUrl(TEST_DEFAULT_BGM_URL)
        .aiComment(TEST_COMMENT)
        .build();
  }

  public static DiaryEntity createDiaryNoIdWithMember(MemberEntity member) {
    return DiaryEntity.builder()
        .emotion(DiaryEmotion.HAPPY)
        .diaryCreatedTime(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .content(TEST_DIARY_CONTENT)
        .memberEntity(member)
        .webtoonImageUrl(TEST_DEFAULT_WEBTOON_URL)
        .bgmUrl(TEST_DEFAULT_BGM_URL)
        .aiComment(TEST_COMMENT)
        .build();
  }

  public static DiaryEntity createDiaryNoIdWithMemberAndCreatedDateTime(
      MemberEntity member, Instant createdDateTime) {
    return DiaryEntity.builder()
        .emotion(DiaryEmotion.HAPPY)
        .diaryCreatedTime(createdDateTime)
        .content(TEST_DIARY_CONTENT)
        .memberEntity(member)
        .webtoonImageUrl(TEST_DEFAULT_WEBTOON_URL)
        .bgmUrl(TEST_DEFAULT_BGM_URL)
        .aiComment(TEST_COMMENT)
        .build();
  }
}
