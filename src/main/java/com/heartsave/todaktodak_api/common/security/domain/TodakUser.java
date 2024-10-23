package com.heartsave.todaktodak_api.common.security.domain;

import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
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
public class TodakUser implements UserDetails, OAuth2User, Serializable {

  private final Long id;
  private final String username;
  private final TodakRole role;

  // TODO: 각 소셜 플랫폼 적용
  private final Map<String, Object> attributes;

  public static TodakUser from(MemberEntity entity) {
    return new TodakUser(entity.getId(), entity.getLoginId(), entity.getRole(), Map.of());
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getPassword() {
    return "";
  }

  // TODO: 각 소셜 플랫폼 적용
  @Override
  public String getUsername() {
    return "OAUTH2 USER NAME";
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
}
