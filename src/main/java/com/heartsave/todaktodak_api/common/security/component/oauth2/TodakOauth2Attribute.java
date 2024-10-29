package com.heartsave.todaktodak_api.common.security.component.oauth2;

import static com.heartsave.todaktodak_api.common.security.constant.ConstraintConstant.Member.LOGIN_ID_MAX_SIZE;

import java.util.Map;
import lombok.Getter;

// 소셜 로그인별로 정형화된 형식의 리소스로 가공
@Getter
public class TodakOauth2Attribute {
  private static final String NAVER = "naver";
  private static final String KAKAO = "kakao";
  private static final String GOOGLE = "google";

  private final String username;
  private final String email;

  private TodakOauth2Attribute(String username, String email) {
    this.username = username;
    this.email = email;
  }

  //
  public static TodakOauth2Attribute of(Map<String, Object> attributes, String authType) {
    return switch (authType) {
      case NAVER -> ofNaver(attributes);
      case KAKAO -> ofKakao(attributes);
      case GOOGLE -> ofGoogle(attributes);
      default -> null;
    };
  }

  @SuppressWarnings("unchecked")
  private static TodakOauth2Attribute ofKakao(Map<String, Object> attributes) {
    String id = String.valueOf(attributes.get("id")); // Long -> String 변환
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    String email = (String) kakaoAccount.get("email");
    return new TodakOauth2Attribute(id + "_" + KAKAO, email);
  }

  @SuppressWarnings("unchecked")
  private static TodakOauth2Attribute ofNaver(Map<String, Object> attributes) {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
    String id = (String) response.get("id");
    id = id.substring(0, Math.min(id.length(), LOGIN_ID_MAX_SIZE - 6));
    String email = (String) response.get("email");
    return new TodakOauth2Attribute(id + "_" + NAVER, email);
  }

  private static TodakOauth2Attribute ofGoogle(Map<String, Object> attributes) {
    return new TodakOauth2Attribute(
        attributes.get("sub") + "_" + GOOGLE, (String) attributes.get("email"));
  }
}
