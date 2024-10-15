package com.heartsave.todaktodak_api.member.service;

import com.heartsave.todaktodak_api.member.dto.LoginIdCheckReq;
import com.heartsave.todaktodak_api.member.dto.NicknameCheckReq;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Primary
@Service
public class MemberServiceImpl implements MemberService {
  private final MemberRepository memberRepository;

  @Override
  public boolean isDuplicatedNickname(LoginIdCheckReq dto) {
    String targetId = dto.loginId();
    return memberRepository.findMemberByLoginId(targetId).isPresent();
  }

  @Override
  public boolean isDuplicatedLoginId(NicknameCheckReq dto) {
    String targetNickname = dto.nickname();
    return memberRepository.findMemberByNickname(targetNickname).isPresent();
  }
}
