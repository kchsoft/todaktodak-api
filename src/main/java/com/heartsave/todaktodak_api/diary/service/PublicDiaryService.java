package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPageResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIdsProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryExistException;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
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
  private final DiaryPageIndexFactory pageIndexFactory;
  private final DiaryReactionRepository reactionRepository;
  private final MemberRepository memberRepository;
  private final S3FileStorageManager s3FileStorageManager;
  private final PublicDiaryCacheService publicDiaryCacheService;

  @Transactional(readOnly = true)
  public PublicDiaryPageResponse getPagination(Long memberId, DiaryPageRequest request) {
    DiaryPageIndex pageIndex = pageIndexFactory.createFrom(request);
    List<PublicDiaryContentProjection> contentProjections = fetchContents(pageIndex);
    replaceWithPreSignedUrls(contentProjections);
    return createPageResponse(contentProjections, memberId);
  }

  private List<PublicDiaryContentProjection> fetchContents(DiaryPageIndex pageIndex) {
    log.info("공개 일기 content 정보를 조회합니다.");
    return Optional.ofNullable(publicDiaryCacheService.getContents(pageIndex))
        .filter(contents -> !contents.isEmpty())
        .orElseGet(
            () -> {
              log.info("공개 일기 Cache Miss");
              List<PublicDiaryContentProjection> dbContents =
                  publicDiaryRepository.findNextContents(pageIndex, PageRequest.of(0, 5));
              publicDiaryCacheService.saveContents(pageIndex, dbContents);
              return dbContents;
            });
  }

  private void replaceWithPreSignedUrls(List<PublicDiaryContentProjection> contentProjections) {
    log.info("공개 일기 content url을 pre-signed url로 변경합니다.");
    for (PublicDiaryContentProjection content : contentProjections) {
      content.replaceWebtoonImageUrls(
          s3FileStorageManager.preSignedWebtoonUrlFrom(content.getWebtoonImageUrls()));
      content.replaceCharacterImageUrl(
          s3FileStorageManager.preSignedCharacterImageUrlFrom(content.getCharacterImageUrl()));
      content.replaceBgmUrl(s3FileStorageManager.preSignedBgmUrlFrom(content.getBgmUrl()));
    }
  }

  private PublicDiaryPageResponse createPageResponse(
      List<PublicDiaryContentProjection> contentProjection, Long memberId) {
    PublicDiaryPageResponse response = new PublicDiaryPageResponse();
    log.info("공개 일기 Reaction 정보를 조회합니다.");
    contentProjection.stream()
        .map(
            content -> {
              DiaryReactionCount reactionCount = fetchReactionCount(content.getPublicDiaryId());
              List<DiaryReactionType> memberReactions =
                  fetchMemberReactions(memberId, content.getPublicDiaryId());
              return PublicDiary.of(content, reactionCount, memberReactions);
            })
        .forEach(response::addPublicDiary);
    return response;
  }

  private DiaryReactionCount fetchReactionCount(Long publicDiaryId) {
    return publicDiaryCacheService
        .getReactionCount(publicDiaryId)
        .orElseGet(
            () -> {
              DiaryReactionCount reactionCount =
                  DiaryReactionCount.from(
                      reactionRepository.countEachByPublicDiaryId(publicDiaryId));
              publicDiaryCacheService.saveReactionCount(publicDiaryId, reactionCount);
              return reactionCount;
            });
  }

  private List<DiaryReactionType> fetchMemberReactions(Long memberId, Long publicDiaryId) {
    return Optional.ofNullable(publicDiaryCacheService.getMemberReactions(memberId, publicDiaryId))
        .orElseGet(
            () -> {
              List<DiaryReactionType> reactions =
                  reactionRepository.findMemberReactions(memberId, publicDiaryId);
              publicDiaryCacheService.saveMemberReactions(memberId, publicDiaryId, reactions);
              return reactions;
            });
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
}
