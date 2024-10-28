package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PublicDiaryService {
  public void write(TodakUser principal, String publicContent) {}
}
