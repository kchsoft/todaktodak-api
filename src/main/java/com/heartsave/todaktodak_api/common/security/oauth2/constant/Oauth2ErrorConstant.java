package com.heartsave.todaktodak_api.common.security.oauth2.constant;

import org.springframework.security.oauth2.core.OAuth2Error;

public final class Oauth2ErrorConstant {
  public static final String DUPLICATED_EMAIL_ERROR_KEY = "DUPLICATED_EMAIL";
  public static final OAuth2Error DUPLICATED_EMAIL_ERROR =
      new OAuth2Error(DUPLICATED_EMAIL_ERROR_KEY, "중복된 이메일로 회원가입 시도", null);
}
