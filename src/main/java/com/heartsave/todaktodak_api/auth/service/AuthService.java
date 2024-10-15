package com.heartsave.todaktodak_api.auth.service;

import com.heartsave.todaktodak_api.auth.dto.LoginIdCheckReq;
import com.heartsave.todaktodak_api.auth.dto.NicknameCheckReq;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final MemberRepository memberRepository;

  public boolean isDuplicatedLoginId(LoginIdCheckReq dto) {
    String targetId = dto.loginId();
    return memberRepository.findMemberByLoginId(targetId).isPresent();
  }

  public boolean isDuplicatedNickname(NicknameCheckReq dto) {
    String targetNickname = dto.nickname();
    return memberRepository.findMemberByNickname(targetNickname).isPresent();
  }
}
