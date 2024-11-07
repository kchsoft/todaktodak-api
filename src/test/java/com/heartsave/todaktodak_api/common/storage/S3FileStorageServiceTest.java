package com.heartsave.todaktodak_api.common.storage;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.heartsave.todaktodak_api.common.config.properties.S3Properties;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageService;
import com.heartsave.todaktodak_api.common.storage.s3.expcetion.InvalidS3UrlException;
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
  private static final String TEST_ORIGINAL_URL =
      "https://test-bucket.s3.amazonaws.com/original-url";
  private static final String DEFAULT_WEBTOON_KEY = "default/webtoon.jpg";
  private static final String DEFAULT_CHARACTER_KEY = "default/character.jpg";
  private static final String DEFAULT_BGM_KEY = "default/bgm.mp3";

  @BeforeEach
  void setUp() {
    s3Service = new S3FileStorageService(s3Presigner, s3Properties);
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

    @BeforeEach
    void setUp() {
      mockBasicProperties();
    }

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

    @BeforeEach
    void setUp() {
      mockBasicProperties();
    }

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

    @BeforeEach
    void setUp() {
      mockBasicProperties();
    }

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

  @Nested
  @DisplayName("웹툰 폴더 URL presign 테스트")
  final class WebtoonFolderUrlTests {

    @BeforeEach
    void setUp() {
      mockBasicProperties();
    }

    @Test
    @DisplayName("웹툰 폴더 URL에서 순차적으로 이미지 URL 생성 및 presign 성공")
    void preSignedWebtoonUrlFromFolderTest() throws Exception {
      // given
      mockPresign();
      List<String> folderUrls = List.of("webtoon/chapter1");

      // when
      List<String> presignedUrls = s3Service.preSignedWebtoonUrlFrom(folderUrls);

      // then
      assertThat(presignedUrls).hasSize(1).allMatch(url -> url.equals(TEST_PRE_SIGNED_URL));
    }
  }

  @Test
  @DisplayName("빈 폴더 URL 리스트에 대해 빈 리스트 반환")
  void preSignedWebtoonUrlFromEmptyListTest() {
    // given
    List<String> emptyUrls = List.of();

    // when
    List<String> presignedUrls = s3Service.preSignedWebtoonUrlFrom(emptyUrls);

    // then
    assertThat(presignedUrls).isEmpty();
  }

  @Nested
  @DisplayName("S3 URL 파싱 테스트")
  final class ParseKeyTests {

    private final String VALID_S3_URL =
        "https://" + TEST_BUCKET + ".s3.amazonaws.com/path/to/file.jpg";
    private final String EXPECTED_KEY = "/path/to/file.jpg";

    @Test
    @DisplayName("유효한 S3 URL에서 키 추출 성공")
    void parseKeyFromValidUrlTest() {
      // when
      given(s3Properties.bucketName()).willReturn(TEST_BUCKET);
      String key = s3Service.parseKeyFrom(VALID_S3_URL);

      // then
      assertThat(key).isEqualTo(EXPECTED_KEY);
    }

    @Test
    @DisplayName("null URL에 대해 예외 발생")
    void parseKeyFromNullUrlTest() {
      assertThatThrownBy(() -> s3Service.parseKeyFrom(null))
          .isInstanceOf(InvalidS3UrlException.class);
    }

    @Test
    @DisplayName("잘못된 형식의 URL에 대해 예외 발생")
    void parseKeyFromMalformedUrlTest() {
      // given
      given(s3Properties.bucketName()).willReturn(TEST_BUCKET);
      String invalidUrl = "not-a-url";

      // when + then
      assertThatThrownBy(() -> s3Service.parseKeyFrom(invalidUrl))
          .isInstanceOf(InvalidS3UrlException.class);
    }

    @Test
    @DisplayName("버킷 이름이 없는 URL에 대해 예외 발생")
    void parseKeyFromUrlWithoutBucketTest() {
      // given
      given(s3Properties.bucketName()).willReturn(TEST_BUCKET);
      String urlWithoutBucket = "https://different-bucket.s3.amazonaws.com/file.jpg";

      // when + then
      assertThatThrownBy(() -> s3Service.parseKeyFrom(urlWithoutBucket))
          .isInstanceOf(InvalidS3UrlException.class);
    }
  }
}
