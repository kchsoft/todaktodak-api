package com.heartsave.todaktodak_api.common.type;

import lombok.Getter;

@Getter
public enum AuthType {
  BASE("BASE"),
  KAKAO("KAKAO"),
  NAVER("NAVER"),
  GOOGLE("GOOGLE");

  private final String type;

  AuthType(String type) {
    this.type = type;
  }
}
