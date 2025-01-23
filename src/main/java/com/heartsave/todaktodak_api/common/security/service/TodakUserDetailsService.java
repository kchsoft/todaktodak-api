package com.heartsave.todaktodak_api.common.security.service;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodakUserDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    MemberEntity memberEntity =
        memberRepository
            .findMemberEntityByLoginId(username)
            .orElseThrow(() -> new UsernameNotFoundException("USER NOT FOUND"));
    return TodakUser.builder()
        .id(memberEntity.getId())
        .username(memberEntity.getLoginId())
        .password(memberEntity.getPassword())
        .role(memberEntity.getRole().name())
        .attributes(Map.of())
        .build();
  }
}
