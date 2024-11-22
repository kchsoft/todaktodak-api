package com.heartsave.todaktodak_api.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.security.component.AccessDeniedHandlerImpl;
import com.heartsave.todaktodak_api.common.security.component.AuthenticationEntryPointImpl;
import com.heartsave.todaktodak_api.common.security.component.jwt.JwtAuthFilter;
import com.heartsave.todaktodak_api.common.security.component.jwt.JwtLogoutFilter;
import com.heartsave.todaktodak_api.common.security.component.jwt.JwtValidationFilter;
import com.heartsave.todaktodak_api.common.security.component.oauth2.OAuth2SuccessHandler;
import com.heartsave.todaktodak_api.common.security.component.oauth2.Oauth2FailureHandler;
import com.heartsave.todaktodak_api.common.security.component.oauth2.TodakOauth2UserDetailsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
  private final TodakOauth2UserDetailsService oauth2UserDetailsService;
  private final AuthenticationEntryPointImpl authenticationEntryPoint;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final ObjectMapper objectMapper;

  @Value("${todak.cors.allowed-origin}")
  private List<String> ALLOWED_ORIGINS;

  @Bean
  @Profile("!test")
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
        .anonymous(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            (eh) ->
                eh.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(new AccessDeniedHandlerImpl(objectMapper)))
        .httpBasic(AbstractHttpConfigurer::disable)
        .oauth2Login(
            (oauth2) ->
                oauth2
                    .userInfoEndpoint((config) -> config.userService(oauth2UserDetailsService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(new Oauth2FailureHandler(objectMapper)))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/api/v1/webhook/ai/**",
                        "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/diary/my")
                    .hasAnyRole("USER", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new JwtValidationFilter(authenticationEntryPoint), JwtAuthFilter.class)
        .addFilterBefore(
            new JwtAuthFilter(authenticationManager, objectMapper),
            UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new JwtLogoutFilter(), LogoutFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", getCorsConfiguration());
    return source;
  }

  private CorsConfiguration getCorsConfiguration() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowCredentials(true);
    configuration.setAllowedMethods(List.of("HEAD", "OPTION", "GET", "POST", "PATCH", "DELETE"));
    configuration.setAllowedOrigins(ALLOWED_ORIGINS);
    configuration.addAllowedHeader("*");
    return configuration;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/favicon.ico");
  }
}
