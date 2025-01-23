package com.heartsave.todaktodak_api.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.ai.client.config.properties.AiServerProperties;
import com.heartsave.todaktodak_api.domain.diary.cache.ContentReactionCountCache;
import com.heartsave.todaktodak_api.domain.diary.service.PublicDiaryCacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class MockBeanConfiguration {

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

  @Primary
  @Bean
  public AiServerProperties mockAiServerProperties() {
    String aiServerDomain = "http://localhost:50000";
    AiServerProperties mockProperties = mock(AiServerProperties.class);

    when(mockProperties.imageDomain()).thenReturn(aiServerDomain);
    when(mockProperties.bgmDomain()).thenReturn(aiServerDomain);
    when(mockProperties.textDomain()).thenReturn(aiServerDomain);
    return mockProperties;
  }
}
