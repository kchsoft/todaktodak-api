package com.heartsave.todaktodak_api.common.security.component.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TodakOauth2AttributeTest {
  private MemberEntity member = BaseTestObject.createTempMember();
  private static final String attributeId = "12345";

  @Test
  @DisplayName("카카오 리소스")
  void attributeAsKakaoTest() {
    // given
    var attribute = Map.of("id", attributeId, "kakao_account", Map.of("email", member.getEmail()));

    // when
    TodakOauth2Attribute oauth2Attribute =
        TodakOauth2Attribute.of(attribute, AuthType.KAKAO.name());

    // then
    assertThat(oauth2Attribute.getUsername()).isEqualTo(attributeId + "_" + AuthType.KAKAO.name());
  }

  @Test
  @DisplayName("네이버 리소스")
  void attributeAsNaverTest() {
    // given
    var attribute = Map.of("response", Map.of("id", attributeId), "email", member.getEmail());

    // when
    TodakOauth2Attribute oauth2Attribute =
        TodakOauth2Attribute.of(attribute, AuthType.NAVER.name());

    // then
    assertThat(oauth2Attribute.getUsername()).isEqualTo(attributeId + "_" + AuthType.NAVER.name());
  }

  @Test
  @DisplayName("구글 리소스")
  void attributeAsGoogleTest() {
    // given
    Map<String, Object> attribute = Map.of("sub", attributeId, "email", member.getEmail());

    // when
    TodakOauth2Attribute oauth2Attribute =
        TodakOauth2Attribute.of(attribute, AuthType.GOOGLE.name());

    // then
    assertThat(oauth2Attribute.getUsername()).isEqualTo(attributeId + "_" + AuthType.GOOGLE.name());
  }

  @Test
  @DisplayName("지원하지 않는 소셜 로그인")
  void unsupoortedOauth2FailTest() {
    // given
    Map<String, Object> attribute = Map.of();

    // when
    TodakOauth2Attribute oauth2Attribute = TodakOauth2Attribute.of(attribute, "UNKNOWN");

    // then
    assertThat(oauth2Attribute).isEqualTo(null);
  }

  @Test
  @DisplayName("정확하지 않은 리소스 형식")
  void wrongAttributeFormatTest() {
    // given
    Map<String, Object> attribute = Map.of();

    // when
    TodakOauth2Attribute oauth2Attribute =
        TodakOauth2Attribute.of(attribute, AuthType.KAKAO.name());

    // then
    assertThat(oauth2Attribute).isEqualTo(null);
  }
}
