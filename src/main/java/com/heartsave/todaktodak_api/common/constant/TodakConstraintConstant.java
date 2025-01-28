package com.heartsave.todaktodak_api.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TodakConstraintConstant {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Member {
    public static final int EMAIL_MAX_SIZE = 50;
    public static final int NICKNAME_MAX_SIZE = 50;
    public static final int LOGIN_ID_MAX_SIZE = 50;
    public static final int PASSWORD_MAX_SIZE = 80;
  }

  public static final class Diary {
    public static final int DIARY_CONTENT_MAX_SIZE = 3000;
    public static final int DIARY_CONTENT_MIN_SIZE = 100;
  }

  public static final class PublicDiary {
    public static final int PUBLIC_DIARY_CONTENT_MAX_SIZE = 3000;
  }
}
