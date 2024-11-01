package com.heartsave.todaktodak_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .components(components())
        .addSecurityItem(new SecurityRequirement().addList("기본 로그인"))
        .info(apiInfo());
  }

  private Info apiInfo() {
    return new Info().title("Todak Todak").description("일기를 공유하고 마음을 소통하세요").version("v1");
  }

  private Components components() {
    return new Components().addSecuritySchemes("기본 로그인", baseSecurityScheme());
  }

  private SecurityScheme baseSecurityScheme() {
    return new SecurityScheme()
        .name("baseLogin")
        .description("기본 로그인")
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER);
  }
}
