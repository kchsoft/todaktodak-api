package com.heartsave.todaktodak_api.member.domain;

import org.springframework.security.core.GrantedAuthority;

public enum MemberRole implements GrantedAuthority {
  ROLE_TEMP,
  ROLE_USER;

  @Override
  public String getAuthority() {
    return this.name();
  }
}
