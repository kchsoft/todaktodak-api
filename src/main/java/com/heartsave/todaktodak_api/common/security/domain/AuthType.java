package com.heartsave.todaktodak_api.common.security.domain;

import lombok.Getter;

@Getter
public enum AuthType {
  BASE,
  KAKAO,
  NAVER,
  GOOGLE;
}
