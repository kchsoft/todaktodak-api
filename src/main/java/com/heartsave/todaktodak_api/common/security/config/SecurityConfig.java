package com.heartsave.todaktodak_api.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.security.component.jwt.JwtAuthFilter;
import com.heartsave.todaktodak_api.common.security.component.jwt.JwtLogoutFilter;
import com.heartsave.todaktodak_api.common.security.component.jwt.JwtValidationFilter;
import com.heartsave.todaktodak_api.common.security.component.oauth2.OAuth2SuccessHandler;
import com.heartsave.todaktodak_api.common.security.component.oauth2.Oauth2FailureHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AccessDeniedHandler accessDeniedHandler;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final Oauth2FailureHandler oauth2FailureHandler;
  private final UserDetailsService userDetailsService;
  private final DefaultOAuth2UserService oauth2UserDetailsService;
  private final JwtLogoutFilter jwtLogoutFilter;
  private final ObjectMapper objectMapper;

  @Value("${todak.cors.allowed-origin}")
  private List<String> ALLOWED_ORIGINS;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            (eh) ->
                eh.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .oauth2Login(
            (oauth2) ->
                oauth2
                    .userInfoEndpoint((config) -> config.userService(oauth2UserDetailsService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oauth2FailureHandler))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(
            new JwtValidationFilter(userDetailsService, objectMapper), JwtAuthFilter.class)
        .addFilterBefore(
            new JwtAuthFilter(authenticationManager, objectMapper),
            UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtLogoutFilter, LogoutFilter.class)
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
}
