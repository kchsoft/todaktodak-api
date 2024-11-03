package com.heartsave.todaktodak_api.common.storage;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class S3FileStorageService { // Todo : S3 SDK 연동 및 pre-signed url 부여 로직 작성
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
