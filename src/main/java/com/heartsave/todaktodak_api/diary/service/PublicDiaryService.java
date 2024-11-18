package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIdsProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryExistException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PublicDiaryService {
  private final DiaryRepository diaryRepository;
  private final PublicDiaryRepository publicDiaryRepository;
  private final DiaryReactionRepository diaryReactionRepository;
  private final MemberRepository memberRepository;
  private final S3FileStorageManager s3FileStorageManager;

  @Transactional(readOnly = true)
  public PublicDiaryPaginationResponse getPublicDiaryPagination(Long memberId, Long publicDiaryId) {
    List<PublicDiaryContentOnlyProjection> diaryContents = fetchDiaryContents(publicDiaryId);
    replaceWithPreSignedUrls(diaryContents);
    return createPaginationResponse(diaryContents, memberId);
  }

  private List<PublicDiaryContentOnlyProjection> fetchDiaryContents(Long publicDiaryId) {
    log.info("공개 일기 content 정보를 조회합니다.");
    if (publicDiaryId == 0) publicDiaryId = getFirstPaginationId(); // 공개 일기 조회 API 첫 호출
    return publicDiaryRepository.findNextContentOnlyById(
        publicDiaryId, PageRequest.of(0, 5)); // 현재 ID 제외, 다음 ID 포함 5개 조회
  }

  private Long getFirstPaginationId() {
    return publicDiaryRepository.findLatestId().map(id -> id + 1).orElse(1L);
  }

  private void replaceWithPreSignedUrls(List<PublicDiaryContentOnlyProjection> diaryContents) {
    log.info("공개 일기 content url을 pre-signed url로 변경합니다.");
    for (PublicDiaryContentOnlyProjection content : diaryContents) {
      content.replaceWebtoonImageUrls(
          s3FileStorageManager.preSignedWebtoonUrlFrom(content.getWebtoonImageUrls()));
      content.replaceCharacterImageUrl(
          s3FileStorageManager.preSignedCharacterImageUrlFrom(content.getCharacterImageUrl()));
      content.replaceBgmUrl(s3FileStorageManager.preSignedBgmUrlFrom(content.getBgmUrl()));
    }
  }

  private PublicDiaryPaginationResponse createPaginationResponse(
      List<PublicDiaryContentOnlyProjection> diaryContents, Long memberId) {
    PublicDiaryPaginationResponse response = new PublicDiaryPaginationResponse();
    log.info("공개 일기 Reaction 정보를 조회합니다.");
    diaryContents.stream()
        .map(
            content -> {
              DiaryReactionCountProjection reactionCount = fetchReactionCount(content.getDiaryId());
              List<DiaryReactionType> memberReactions =
                  fetchMemberReactions(memberId, content.getDiaryId());
              return PublicDiary.of(content, reactionCount, memberReactions);
            })
        .forEach(response::addPublicDiary);
    return response;
  }

  private DiaryReactionCountProjection fetchReactionCount(Long diaryId) {
    return diaryReactionRepository.countEachByDiaryId(diaryId);
  }

  private List<DiaryReactionType> fetchMemberReactions(Long memberId, Long diaryId) {
    return diaryReactionRepository.findMemberReaction(memberId, diaryId);
  }

  public void write(Long memberId, Long diaryId, String publicContent) {
    DiaryIdsProjection ids =
        diaryRepository
            .findIdsById(diaryId)
            .orElseThrow(
                () ->
                    new DiaryNotFoundException(DiaryErrorSpec.DIARY_NOT_FOUND, memberId, diaryId));

    if (ids.getPublicDiaryId() != null) {
      throw new PublicDiaryExistException(
          PublicDiaryErrorSpec.PUBLIC_DIARY_EXIST, memberId, ids.getPublicDiaryId());
    }

    DiaryEntity diaryRef = diaryRepository.getReferenceById(diaryId);
    MemberEntity memberRef = memberRepository.getReferenceById(memberId);

    PublicDiaryEntity publicDiary =
        PublicDiaryEntity.builder()
            .diaryEntity(diaryRef)
            .memberEntity(memberRef)
            .publicContent(publicContent)
            .build();
    publicDiaryRepository.save(publicDiary);
  }

  public void toggleReactionStatus(Long memberId, PublicDiaryReactionRequest request) {
    Long diaryId = request.diaryId();
    DiaryReactionType reactionType = request.reactionType();
    DiaryReactionEntity reactionEntity = getDiaryReactionEntity(memberId, diaryId, reactionType);
    if (!diaryReactionRepository.hasReaction(memberId, diaryId, reactionType)) {
      diaryReactionRepository.save(reactionEntity);
    } else {
      diaryReactionRepository.deleteReaction(memberId, diaryId, reactionType);
    }
  }

  private DiaryReactionEntity getDiaryReactionEntity(
      Long memberId, Long diaryId, DiaryReactionType reactionType) {
    MemberEntity memberRef = memberRepository.getReferenceById(memberId);
    DiaryEntity diaryRef = diaryRepository.getReferenceById(diaryId);
    DiaryReactionEntity reactionEntity =
        DiaryReactionEntity.builder()
            .memberEntity(memberRef)
            .diaryEntity(diaryRef)
            .reactionType(reactionType)
            .build();
    return reactionEntity;
  }
}
