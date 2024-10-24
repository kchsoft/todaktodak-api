package com.heartsave.todaktodak_api.auth.service;

import com.heartsave.todaktodak_api.auth.dto.request.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.response.NicknameCheckRequest;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
  private final MemberRepository memberRepository;

  public boolean isDuplicatedLoginId(LoginIdCheckRequest dto) {
    String targetId = dto.loginId();
    return memberRepository.findMemberByLoginId(targetId).isPresent();
  }

  public boolean isDuplicatedNickname(NicknameCheckRequest dto) {
    String targetNickname = dto.nickname();
    return memberRepository.findMemberByNickname(targetNickname).isPresent();
  }
}
