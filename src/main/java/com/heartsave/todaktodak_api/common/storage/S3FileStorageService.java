package com.heartsave.todaktodak_api.common.storage;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@RequiredArgsConstructor
@Service
public class S3FileStorageService {
  private final S3Presigner s3Presigner;

  @Value("${aws.s3.bucket-name}")
  private String BUCKET_NAME;

  @Value("${aws.s3.presign-duration}")
  private Long PRE_SIGN_DURATION;

  @Value("${aws.s3.default-key.character}")
  private String DEFAULT_CHARACTER_IMAGE_KEY;

  @Value("${aws.s3.default-key.webtoon}")
  private String DEFAULT_WEBTOON_IMAGE_KEY;

  @Value("${aws.s3.default-key.bgm}")
  private String DEFAULT_BGM_IMAGE_KEY;

  public List<String> preSignedWebtoonUrlFrom(List<String> s3FolderUrl) {
    return s3FolderUrl.stream().map(this::preSign).toList();
  }

  public String preSignedFirstWebtoonUrlFrom(String s3FileUrl) {
    return preSign(s3FileUrl);
  }

  public String preSignedCharacterImageUrlFrom(String s3FileUrl) {
    return s3FileUrl == null ? preSign(DEFAULT_CHARACTER_IMAGE_KEY) : preSign(s3FileUrl);
  }

  public String preSignedBgmUrlFrom(String s3FileUrl) {
    return preSign(s3FileUrl);
  }

  private String preSign(String url) {
    var preSignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(PRE_SIGN_DURATION))
            .getObjectRequest(GetObjectRequest.builder().bucket(BUCKET_NAME).key(url).build())
            .build();

    return s3Presigner.presignGetObject(preSignRequest).url().toString();
  }
}
