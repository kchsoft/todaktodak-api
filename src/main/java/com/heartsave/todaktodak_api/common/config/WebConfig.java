package com.heartsave.todaktodak_api.common.config;

import com.heartsave.todaktodak_api.ai.webhook.interceptor.AiServerApiKeyInterceptor;
import com.heartsave.todaktodak_api.auth.annotation.TodakUserIdResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AiServerApiKeyInterceptor aiServerApiKeyInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(aiServerApiKeyInterceptor).addPathPatterns("/api/v1/webhook/ai/**");
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new TodakUserIdResolver());
  }
}
