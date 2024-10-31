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

@Getter
public class TodakUser implements UserDetails, OAuth2User, Serializable {

  private final Long id;
  private final String username;
  private final String role;
  private final Map<String, Object> attributes;
  private final String password;

  @Builder
  private TodakUser(
      Long id, String username, String role, Map<String, Object> attributes, String password) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.attributes = attributes;
    this.password = password;
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
