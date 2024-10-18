package com.heartsave.todaktodak_api.member.domain;

import org.springframework.security.core.GrantedAuthority;

public enum MemberRole implements GrantedAuthority {
  TEMP,
  USER;

  @Override
  public String getAuthority() {
    return this.name();
  }
}
