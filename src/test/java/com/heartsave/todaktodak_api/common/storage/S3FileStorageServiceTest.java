package com.heartsave.todaktodak_api.common.storage;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.heartsave.todaktodak_api.common.config.properties.S3Properties;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
final class S3FileStorageServiceTest {

  @Mock private static S3Presigner s3Presigner;
  @Mock private static S3Properties s3Properties;

  @InjectMocks private static S3FileStorageService s3Service;

  private static final String TEST_BUCKET = "test-bucket";
  private static final Long TEST_DURATION = 3600L;
  private static final String TEST_PRE_SIGNED_URL =
      "https://test-bucket.s3.amazonaws.com/presigned-url";
  private static final String DEFAULT_WEBTOON_KEY = "default/webtoon.jpg";
  private static final String DEFAULT_CHARACTER_KEY = "default/character.jpg";
  private static final String DEFAULT_BGM_KEY = "default/bgm.mp3";

  @BeforeEach
  void setUp() {
    s3Service = new S3FileStorageService(s3Presigner, s3Properties);
    mockBasicProperties();
  }

  private void mockBasicProperties() {
    given(s3Properties.bucketName()).willReturn(TEST_BUCKET);
    given(s3Properties.presignDuration()).willReturn(TEST_DURATION);
  }

  private void mockDefaultKey() {
    given(s3Properties.defaultKey())
        .willReturn(
            new S3Properties.DefaultKeys(
                DEFAULT_CHARACTER_KEY, DEFAULT_WEBTOON_KEY, DEFAULT_BGM_KEY));
  }

  private void mockPresign() throws Exception {
    PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
    given(presignedRequest.url()).willReturn(new URI(TEST_PRE_SIGNED_URL).toURL());
    given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .willReturn(presignedRequest);
  }

  @Nested
  @DisplayName("웹툰 이미지 presign 테스트")
  final class WebtoonUrlTests {

    @Test
    @DisplayName("웹툰 전체 이미지 presign 성공")
    void preSignWebtoonUrlsSuccessTest() throws Exception {
      // given
      mockPresign();
      List<String> keys = List.of("1.webp", "2.webp", "3.webp", "4.webp");

      // when
      List<String> urls = s3Service.preSignedWebtoonUrlFrom(keys);

      // then
      assertThat(urls).hasSize(4).allMatch(url -> url.equals(TEST_PRE_SIGNED_URL));
    }

    @Test
    @DisplayName("존재하지 않는 웹툰 이미지에 대해 기본 이미지로 대체")
    void preSignWebtoonUrlsDefaultTest() throws Exception {
      // given
      mockDefaultKey();
      mockPresign();

      // when
      String url = s3Service.preSignedFirstWebtoonUrlFrom(null);

      // then
      assertThat(url).isEqualTo(TEST_PRE_SIGNED_URL);
    }
  }

  @Nested
  @DisplayName("캐릭터 이미지 presign 테스트")
  final class CharacterUrlTests {
    @Test
    @DisplayName("캐릭터 이미지 presign 성공")
    void preSignCharacterImageUrlSuccessTest() throws Exception {
      // given
      mockPresign();
      String key = "character.jpg";

      // when
      String url = s3Service.preSignedCharacterImageUrlFrom(key);

      // then
      assertThat(url).isEqualTo(TEST_PRE_SIGNED_URL);
    }

    @Test
    @DisplayName("존재하지 않는 캐릭터 이미지에 대해 기본 이미지로 대체")
    void preSignCharacterUrlDefaultTest() throws Exception {
      // given
      mockDefaultKey();
      mockPresign();

      // when
      String url = s3Service.preSignedCharacterImageUrlFrom(null);

      // then
      assertThat(url).isEqualTo(TEST_PRE_SIGNED_URL);
    }
  }

  @Nested
  @DisplayName("BGM presign 테스트")
  final class BgmUrlTests {
    @Test
    @DisplayName("BGM presign 성공")
    void preSignBgmUrlSuccessTest() throws Exception {
      // given
      mockPresign();
      String key = "music.mp3";

      // when
      String url = s3Service.preSignedBgmUrlFrom(key);

      // then
      assertThat(url).isEqualTo(TEST_PRE_SIGNED_URL);
    }

    @Test
    @DisplayName("존재하지 않는 BGM에 대해 기본 이미지로 대체")
    void preSignBgmUrlDefaultTest() throws Exception {
      // given
      mockDefaultKey();
      mockPresign();

      // when
      String url = s3Service.preSignedBgmUrlFrom(null);

      // then
      assertThat(url).isEqualTo(TEST_PRE_SIGNED_URL);
    }
  }

  @Test
  @DisplayName("존재하지 않는 S3 객체 Key에 대해 예외 발생")
  void preSignFail_throwNoSuchKeyExceptionTest() {
    // given
    given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .willThrow(NoSuchKeyException.class);

    // when + then
    assertThatThrownBy(() -> s3Service.preSignedBgmUrlFrom("non-existent.mp3"))
        .isInstanceOf(NoSuchKeyException.class);
  }
}
