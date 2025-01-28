package com.heartsave.todaktodak_api.domain.diary.service;

import com.heartsave.todaktodak_api.domain.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.domain.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class DiaryReactionService {
  private final DiaryReactionRepository reactionRepository;
  private final MemberRepository memberRepository;
  private final PublicDiaryRepository publicDiaryRepository;

  public void toggleReactionStatus(Long memberId, PublicDiaryReactionRequest request) {
    Long publicDiaryId = request.publicDiaryId();
    DiaryReactionType reactionType = request.reactionType();

    DiaryReactionEntity reactionEntity =
        createDiaryReactionEntity(memberId, publicDiaryId, reactionType);

    if (!reactionRepository.hasReaction(memberId, publicDiaryId, reactionType)) {
      reactionRepository.save(reactionEntity); // DataIntegrityViolationException 예외 주의
    } else {
      reactionRepository.deleteReaction(memberId, publicDiaryId, reactionType);
    }
  }

  private DiaryReactionEntity createDiaryReactionEntity(
      Long memberId, Long publicDiaryId, DiaryReactionType reactionType) {
    MemberEntity member = memberRepository.getReferenceById(memberId);
    PublicDiaryEntity publicDiary = publicDiaryRepository.getReferenceById(publicDiaryId);
    DiaryReactionEntity reactionEntity =
        DiaryReactionEntity.builder()
            .memberEntity(member)
            .publicDiaryEntity(publicDiary)
            .reactionType(reactionType)
            .build();
    return reactionEntity;
  }
}
