package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {

  private final AiService aiService;
  private final DiaryRepository diaryRepository;

  public Long write(DiaryWriteRequest request) {
    DiaryEntity diaryEntity =
        DiaryEntity.builder()
            .emotion(request.getEmotion())
            .content(request.getContent())
            .publicContent(request.getPublicContent())
            .isPublic(request.getIsPublic())
            .postCreatedAt(request.getDate())
            .build();
    callAiContent(diaryEntity);
    return diaryRepository.save(diaryEntity).getId();
  }

  private void callAiContent(DiaryEntity diaryEntity) {
    log.info("AI 컨텐츠 생성 요청을 시작합니다.");
    aiService.callWebtoon(
        diaryEntity.getMemberEntity().getId(), diaryEntity.getContent(), diaryEntity.getEmotion());
    aiService.callBgm(
        diaryEntity.getMemberEntity().getId(), diaryEntity.getContent(), diaryEntity.getEmotion());
    aiService.callComment(
        diaryEntity.getMemberEntity().getId(), diaryEntity.getContent(), diaryEntity.getEmotion());
    log.info("AI 컨텐츠 생성 요청을 마쳤습니다.");
  }
}
