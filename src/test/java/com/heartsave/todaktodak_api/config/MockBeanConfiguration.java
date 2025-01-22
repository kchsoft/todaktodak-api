package com.heartsave.todaktodak_api.config;

import static org.mockito.Mockito.mock;

import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.cache.ContentReactionCountCache;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryCacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public abstract class MockBeanConfiguration {

  @Primary
  @Bean
  public PublicDiaryCacheService mockPublicDiaryCacheService() {
    return mock(PublicDiaryCacheService.class);
  }

  @Primary
  @Bean
  public S3FileStorageManager mockS3FileStorageManager() {
    return mock(S3FileStorageManager.class);
  }

  @Primary
  @Bean
  public ContentReactionCountCache mockContentReactionCountCache() {
    return mock(ContentReactionCountCache.class);
  }
}
