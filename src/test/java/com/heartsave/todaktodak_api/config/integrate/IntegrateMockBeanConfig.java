package com.heartsave.todaktodak_api.config.integrate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.domain.ai.client.config.properties.AiServerProperties;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import com.heartsave.todaktodak_api.domain.diary.cache.ContentReactionCountCache;
import com.heartsave.todaktodak_api.domain.member.cache.CharacterCache;
import java.net.URL;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@TestConfiguration
@Profile("test")
public class IntegrateMockBeanConfig {

  @Primary
  @Bean
  public S3Client mockS3Client() {
    // mock object
    S3Client mockS3Client = mock(S3Client.class);
    ListObjectsV2Response mockResponse = mock(ListObjectsV2Response.class);

    // setting
    when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockResponse);
    when(mockResponse.contents()).thenReturn(List.of());

    return mockS3Client;
  }

  @Primary
  @Bean
  public S3Presigner mockS3Presigner() {
    // mock object
    S3Presigner mockS3Presigner = mock(S3Presigner.class);
    PresignedGetObjectRequest mockPresigned = mock(PresignedGetObjectRequest.class);
    URL mockUrl = mock(URL.class);

    // method setting
    when(mockS3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(mockPresigned);
    when(mockPresigned.url()).thenReturn(mock());
    when(mockUrl.toString()).thenReturn("presigned-url");

    return mockS3Presigner;
  }

  @Primary
  @Bean
  public CharacterCache mockCharacterCache() {
    return mock(CharacterCache.class);
  }

  @Primary
  @Bean
  public ContentReactionCountCache mockContentReactionCountCache() {
    return mock(ContentReactionCountCache.class);
  }

  @Primary
  @Bean
  public RefreshTokenCache mockRefreshTokenCache() {
    return mock(RefreshTokenCache.class);
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
