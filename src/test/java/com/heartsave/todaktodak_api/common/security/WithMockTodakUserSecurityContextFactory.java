package com.heartsave.todaktodak_api.common.security;

import com.heartsave.todaktodak_api.common.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockTodakUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockTodakUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockTodakUser annotation) {
    String username = annotation.username();
    long id = annotation.id();
    String role = annotation.role();

    TodakUser user = TodakUser.of(id, username, role, Map.of());

    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(
            user, "password", List.of(new SimpleGrantedAuthority(role)));
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(token);
    return context;
  }
}
