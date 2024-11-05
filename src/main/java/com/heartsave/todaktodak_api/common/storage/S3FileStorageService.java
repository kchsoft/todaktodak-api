package com.heartsave.todaktodak_api.common.storage;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.URL.DEFAULT_URL;

import com.heartsave.todaktodak_api.common.config.properties.S3Properties;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@RequiredArgsConstructor
@Slf4j
@Service
public class S3FileStorageService {
  private final S3Presigner s3Presigner;
  private final S3Properties s3Properties;

  // TODO: 존재하지 않는 이미지 key에 대한 예외 처리 필요
  public List<String> preSignedWebtoonUrlFrom(List<String> s3FolderUrl) {
    return s3FolderUrl.stream().map(this::preSign).toList();
  }

  public String preSignedFirstWebtoonUrlFrom(String key) {
    return key == null ? preSign(s3Properties.defaultKey().webtoon()) : preSign(key);
  }

  public String preSignedCharacterImageUrlFrom(String key) {
    return key == null ? preSign(s3Properties.defaultKey().character()) : preSign(key);
  }

  public String preSignedBgmUrlFrom(String key) {
    return key == null ? preSign(s3Properties.defaultKey().bgm()) : preSign(key);
  }

  // TODO: presigned url 캐싱 관리
  private String preSign(String key) throws NoSuchKeyException {
    if (key.equals(DEFAULT_URL)) return DEFAULT_URL;

    var preSignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(s3Properties.presignDuration()))
            .getObjectRequest(
                GetObjectRequest.builder().bucket(s3Properties.bucketName()).key(key).build())
            .build();

    return s3Presigner.presignGetObject(preSignRequest).url().toString();
  }
}
