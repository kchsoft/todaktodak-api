package com.heartsave.todaktodak_api.auth.annotation;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TodakUserIdResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(TodakUserId.class)
        && parameter.getParameterType().equals(Long.class);
  }

  @Override
  public Long resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return Optional.ofNullable(authentication)
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getPrincipal)
        .filter(principal -> principal instanceof TodakUser)
        .map(TodakUser.class::cast)
        .map(TodakUser::getId)
        .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("인증 정보를 찾을 수 없습니다."));
  }
}
