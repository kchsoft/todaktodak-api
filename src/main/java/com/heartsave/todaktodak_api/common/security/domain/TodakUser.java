package com.heartsave.todaktodak_api.common.security.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class TodakUser implements UserDetails, OAuth2User, Serializable {

  private final Long id;
  private final String username;
  private final String role;
  private final Map<String, Object> attributes;
  private String password;

  // 인증 성공 후 컨트롤러 진입 전에 삭제
  public void removePassword() {
    password = "";
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(role));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getName() {
    return this.username;
  }

  @Override
  public String toString() {
    return """
ID: %s, USERNAME: %s, ROLE: %s"""
        .formatted(id, username, role);
  }
}
