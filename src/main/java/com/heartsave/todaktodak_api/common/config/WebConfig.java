package com.heartsave.todaktodak_api.common.config;

import com.heartsave.todaktodak_api.ai.webhook.interceptor.AiServerApiKeyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AiServerApiKeyInterceptor aiServerApiKeyInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(aiServerApiKeyInterceptor).addPathPatterns("/api/v1/webhook/ai/**");
  }
}
