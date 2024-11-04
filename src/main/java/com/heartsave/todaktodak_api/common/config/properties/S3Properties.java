package com.heartsave.todaktodak_api.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(String bucketName, Long presignDuration, DefaultKeys defaultKey) {
  public record DefaultKeys(String character, String webtoon, String bgm) {}
}
