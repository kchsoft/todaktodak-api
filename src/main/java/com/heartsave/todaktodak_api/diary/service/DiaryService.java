package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {

  private final AiService aiService;
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;

  public Long write(OAuth2User auth, DiaryWriteRequest request) {
    DiaryEntity diaryEntity = createDiaryEntity(auth, request);
    log.info("AI 컨텐츠 생성 요청을 시작합니다.");
    aiService.callAiContent(diaryEntity);
    log.info("AI 컨텐츠 생성 요청을 마쳤습니다.");

    log.info("DB에 일기 저장을 요청합니다.");
    return diaryRepository.save(diaryEntity).getId();
  }

  private DiaryEntity createDiaryEntity(OAuth2User auth, DiaryWriteRequest request) {
    MemberEntity member = memberRepository.findMemberByLoginId(auth.getName()).get();
    return DiaryEntity.builder()
        .memberEntity(member)
        .emotion(request.getEmotion())
        .content(request.getContent())
        .publicContent(request.getPublicContent())
        .isPublic(request.getIsPublic())
        .postCreatedAt(request.getDate())
        .build();
  }
}
