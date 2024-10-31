package com.heartsave.todaktodak_api.auth.service;

import com.heartsave.todaktodak_api.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.auth.exception.AuthException;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public void signUp(SignUpRequest dto) {
    if (isDuplicated(dto)) throw new AuthException(AuthErrorSpec.DUPLICATED_INFORMATION, dto);

    var newMember =
        MemberEntity.builder()
            .authType(AuthType.BASE)
            .email(dto.email())
            .nickname(dto.nickname())
            .loginId(dto.loginId())
            .password(passwordEncoder.encode(dto.password()))
            .role(TodakRole.ROLE_USER)
            .build();

    memberRepository.save(newMember);
  }

  private boolean isDuplicated(SignUpRequest dto) {
    return isDuplicatedEmail(dto.email())
        || isDuplicatedLoginId(dto.loginId())
        || isDuplicatedNickname(dto.nickname());
  }

  public boolean isDuplicatedLoginId(String loginId) {
    return memberRepository.findMemberEntityByLoginId(loginId).isPresent();
  }

  public boolean isDuplicatedNickname(String nickname) {
    return memberRepository.findMemberEntityByNickname(nickname).isPresent();
  }

  public boolean isDuplicatedEmail(String email) {
    return memberRepository.findMemberEntityByEmail(email).isPresent();
  }
}
