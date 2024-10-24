package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.ai.dto.AiContentResponse;
import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.exception.DiaryDailyWritingLimitExceedException;
import com.heartsave.todaktodak_api.diary.exception.DiaryDeleteNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiaryService {

  private final AiService aiService;
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;

  public DiaryWriteResponse write(TodakUser principal, DiaryWriteRequest request) {
    DiaryEntity diary = createDiaryEntity(principal, request);
    Long memberId = diary.getMemberEntity().getId();
    LocalDateTime diaryCreatedDate = diary.getDiaryCreatedAt();

    if (diaryRepository.existsByDate(memberId, diaryCreatedDate)) {
      throw new DiaryDailyWritingLimitExceedException(
          DiaryErrorSpec.DAILY_WRITING_LIMIT_EXCEED, memberId);
    }

    log.info("AI 컨텐츠 생성 요청을 시작합니다.");
    AiContentResponse response = aiService.callAiContent(diary);
    log.info("AI 컨텐츠 생성 요청을 마쳤습니다.");

    log.info("DB에 일기 저장을 요청합니다.");
    diary.addAiContent(response);
    diaryRepository.save(diary);
    log.info("DB에 일기를 저장하였습니다.");
    return DiaryWriteResponse.builder().aiComment(response.getAiComment()).build();
  }

  public void delete(TodakUser principal, Long diaryId) {
    Long memberId = principal.getId();

    log.info("DB에 일기를 삭제를 요청합니다.");
    if (0 == diaryRepository.deleteByIds(memberId, diaryId))
      throw new DiaryDeleteNotFoundException(DiaryErrorSpec.DELETE_NOT_FOUND, memberId, diaryId);
    log.info("DB에서 일기를 삭제했습니다.");

    // TODO :  s3에서 webtoon,bgm,comment 삭제 요청
    return;
  }

  private DiaryEntity createDiaryEntity(TodakUser principal, DiaryWriteRequest request) {
    Long memberId = principal.getId();
    MemberEntity member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
    return DiaryEntity.builder()
        .memberEntity(member)
        .emotion(request.getEmotion())
        .content(request.getContent())
        .diaryCreatedAt((request.getDate()))
        .build();
  }
}
