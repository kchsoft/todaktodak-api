package com.heartsave.todaktodak_api.member.service;

import com.heartsave.todaktodak_api.member.dto.LoginIdCheckReq;
import com.heartsave.todaktodak_api.member.dto.NicknameCheckReq;

public interface MemberService {
  boolean isDuplicatedNickname(LoginIdCheckReq dto);

  boolean isDuplicatedLoginId(NicknameCheckReq dto);
}
