package com.heartsave.todaktodak_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(apiInfo());
  }

  private Info apiInfo() {
    return new Info().title("Todak Todak").description("일기를 공유하고 마음을 소통하세요").version("v1");
  }
}
