package com.heartsave.todaktodak_api.common.storage.s3;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.URL.DEFAULT_URL;
import static com.heartsave.todaktodak_api.common.constant.CoreConstant.URL.TEMP_CHARACTER_IMAGE_URL_PREFIX;

import com.heartsave.todaktodak_api.common.config.properties.S3Properties;
import com.heartsave.todaktodak_api.common.exception.errorspec.S3ErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.expcetion.InvalidS3UrlException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@RequiredArgsConstructor
@Slf4j
@Service
public class S3FileStorageManager {
  private final S3Presigner s3Presigner;
  private final S3Properties s3Properties;
  private final S3Client s3Client;

  private final String IMAGE_FILE_FORMAT = ".webp";

  // TODO: 존재하지 않는 이미지 key에 대한 예외 처리 필요
  public List<String> preSignedWebtoonUrlFrom(List<String> s3FolderUrl) {
    if (s3FolderUrl.isEmpty()) return List.of();

    List<String> s3ImageUrls = new ArrayList<>();
    String folderUrl = s3FolderUrl.getFirst();
    for (int order = 1; order <= 4; order++) {
      s3ImageUrls.add(folderUrl + order + IMAGE_FILE_FORMAT);
    }

    return s3ImageUrls.stream().map(this::preSign).toList();
  }

  public String preSignedFirstWebtoonUrlFrom(String key) {
    return key == null
        ? preSign(s3Properties.defaultKey().webtoon())
        : preSign(key + "1" + IMAGE_FILE_FORMAT);
  }

  public String preSignedCharacterImageUrlFrom(String key) {
    return key == null ? preSign(s3Properties.defaultKey().character()) : preSign(key);
  }

  public String preSignedBgmUrlFrom(String key) {
    return key == null ? preSign(s3Properties.defaultKey().bgm()) : preSign(key);
  }

  public String parseKeyFrom(String url) {
    if (url == null || !url.contains(s3Properties.bucketName()))
      throw new InvalidS3UrlException(S3ErrorSpec.INVALID_S3_URL, url);

    URL s3FullUrl;
    String path = "";
    try {
      s3FullUrl = new URL(url);
      path = s3FullUrl.getPath();
    } catch (MalformedURLException e) {
      throw new InvalidS3UrlException(S3ErrorSpec.INVALID_S3_URL, url);
    }
    return path.substring(1);
  }

  private String preSign(String key) throws NoSuchKeyException {
    if (key.equals(DEFAULT_URL)) return DEFAULT_URL;

    var preSignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(s3Properties.presignDuration()))
            .getObjectRequest(
                GetObjectRequest.builder().bucket(s3Properties.bucketName()).key(key).build())
            .build();
    String preSignedUrl = s3Presigner.presignGetObject(preSignRequest).url().toString();
    log.info("pre-signed KEY={}, URL{}", key, preSignedUrl);
    return preSignedUrl;
  }

  public void deleteDiary(String key) {
    if (DEFAULT_URL.equals(key) || !isObjectExist(key)) return;

    log.info("S3에 일기 컨텐츠 삭제를 요청합니다.");
    deleteObject(key);
    log.info("S3에서 일기 컨텐츠를 삭제했습니다.");
  }

  public void deleteDiaries(List<String> keys) {
    keys.forEach(this::deleteDiary);
  }

  // 객체 메타데이터 조회로 존재 확인
  public boolean isObjectExist(String key) {
    try {
      var headObjectRequest =
          HeadObjectRequest.builder().bucket(s3Properties.bucketName()).key(key).build();
      s3Client.headObject(headObjectRequest);
      return true;
    } catch (NoSuchKeyException e) {
      log.error("{}라는 S3 객체가 존재하지 않습니다.", key);
      return false;
    } catch (Exception e) {
      log.error("S3 객체 조회 중 문제가 발생했습니다={}", e.getMessage());
      return false;
    }
  }

  // 생성된 임시 캐릭터를 프로필로 등록
  public void replaceCharacterImageUrl(String tempKey) {
    String key = tempKey.replace(TEMP_CHARACTER_IMAGE_URL_PREFIX, "");
    try {
      copyObject(tempKey, key);
      deleteObject(tempKey);
    } catch (NoSuchKeyException e) {
      throw new InvalidS3UrlException(S3ErrorSpec.INVALID_S3_URL, tempKey + "->" + key);
    }
  }

  private void copyObject(String from, String to) {
    CopyObjectRequest copyObjectRequest =
        CopyObjectRequest.builder()
            .sourceBucket(s3Properties.bucketName())
            .sourceKey(from)
            .destinationBucket(s3Properties.bucketName())
            .destinationKey(to)
            .build();
    s3Client.copyObject(copyObjectRequest);
  }

  private void deleteObject(String key) {
    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(s3Properties.bucketName()).key(key).build();
    s3Client.deleteObject(deleteObjectRequest);
  }
}
