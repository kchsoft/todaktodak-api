package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPageResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIdsProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryExistException;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.List;
import java.util.Objects;
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

  @Transactional(readOnly = true)
  public PublicDiaryPageResponse getPagination(Long memberId, DiaryPageRequest request) {
    DiaryPageIndex pageIndex = pageIndexFactory.createFrom(request);
    List<PublicDiaryContentProjection> contentProjections = fetchContents(pageIndex);
    replaceWithPreSignedUrls(contentProjections);
    return createPageResponse(contentProjections, memberId);
  }

  private List<PublicDiaryContentProjection> fetchContents(DiaryPageIndex pageIndex) {
    log.info("공개 일기 content 정보를 조회합니다.");
    return publicDiaryRepository.findNextContents(
        pageIndex, PageRequest.of(0, 5)); // 현재 ID 제외, 다음 ID 포함 5개 조회
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
              DiaryReactionCountProjection reactionCount = fetchReactionCount(content.getDiaryId());
              List<DiaryReactionType> memberReactions =
                  fetchMemberReactions(memberId, content.getDiaryId());
              return PublicDiary.of(content, reactionCount, memberReactions);
            })
        .forEach(response::addPublicDiary);
    return response;
  }

  private DiaryReactionCountProjection fetchReactionCount(Long diaryId) {
    return reactionRepository.countEachByDiaryId(diaryId);
  }

  private List<DiaryReactionType> fetchMemberReactions(Long memberId, Long diaryId) {
    return reactionRepository.findMemberReaction(memberId, diaryId);
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

  public void delete(Long memberId, Long publicDiaryId) {
    PublicDiaryEntity publicDiary =
        publicDiaryRepository
            .findById(publicDiaryId)
            .orElseThrow(
                () ->
                    new PublicDiaryNotFoundException(
                        PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, publicDiaryId));

    if (!Objects.equals(memberId, publicDiary.getMemberEntity().getId())) {
      throw new PublicDiaryNotFoundException(
          PublicDiaryErrorSpec.PUBLIC_DIARY_DELETE_NOT_FOUND, publicDiaryId);
    }
    publicDiaryRepository.delete(publicDiary);
  }
}
