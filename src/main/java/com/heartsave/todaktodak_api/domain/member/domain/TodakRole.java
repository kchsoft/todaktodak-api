package com.heartsave.todaktodak_api.domain.member.domain;

import org.springframework.security.core.GrantedAuthority;

public enum TodakRole implements GrantedAuthority {
  ROLE_TEMP,
  ROLE_USER,
  ROLE_ADMIN;

  @Override
  public String getAuthority() {
    return this.name();
  }
}
