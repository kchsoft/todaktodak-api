package com.heartsave.todaktodak_api.common.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

@RequiredArgsConstructor
@Service
public class S3FileStorageService { // Todo : S3 SDK 연동 및 pre-signed url 부여 로직 작성
  private final S3Client s3Client;

  private List<String> getBucketList() {
    var response = s3Client.listBuckets();
    return response.buckets().stream().map(Bucket::name).collect(Collectors.toList());
  }

  public List<String> preSignedWebtoonUrlFrom(List<String> s3FolderUrl) {
    return new ArrayList<>();
  }

  public String preSignedFirstWebtoonUrlFrom(String s3FileUrl) {
    return "";
  }

  public String preSignedCharacterImageUrlFrom(String s3FileUrl) {
    return "";
  }

  public String preSignedBgmUrlFrom(String s3FileUrl) {
    return "";
  }
}
