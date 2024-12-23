package com.heartsave.todaktodak_api.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class MySharedDiaryServiceTest {

  @InjectMocks private MySharedDiaryService mySharedDiaryService;

  @Mock private MySharedDiaryRepository mySharedDiaryRepository;
  @Mock private DiaryReactionRepository reactionRepository;
  @Mock private S3FileStorageManager s3FileStorageManager;

  @Mock private MySharedDiaryPreviewProjection mockProjection;
  @Mock private DiaryPageIndexFactory mockIndexFactory;

  private MemberEntity member;
  private DiaryEntity diary;
  private PublicDiaryEntity mockPublicDiary;
  private final String webtoonUrl = "webtoonUrl";
  private final String preSigned_webtoonUrl = "preSigned_webtoonUrl";
  private final String bgmUrl = "bgmUrl";
  private final String preSigned_bgmUrl = "preSigned_bgmUrl";
  private final Instant createdTime = Instant.now();

  @BeforeEach
  void setUp() {
    member = BaseTestObject.createMember();
    diary = BaseTestObject.createDiary();
    mockPublicDiary = mock(PublicDiaryEntity.class);
  }

  @Test
  @DisplayName("성공적으로 페이지네이션 정보를 가져온다")
  void getPagination_Success() {
    List<MySharedDiaryPreviewProjection> previews = new ArrayList<>();
    previews.add(mockProjection);

    when(mockProjection.getWebtoonImageUrl()).thenReturn(webtoonUrl);
    when(s3FileStorageManager.preSignedFirstWebtoonUrlFrom(anyString()))
        .thenReturn(preSigned_webtoonUrl);
    when(mySharedDiaryRepository.findNextPreviews(
            anyLong(), any(DiaryPageIndex.class), any(PageRequest.class)))
        .thenReturn(previews);
    DiaryPageRequest request =
        new DiaryPageRequest(mockPublicDiary.getId(), mockPublicDiary.getCreatedTime());
    when(mockIndexFactory.createFrom(request, member.getId()))
        .thenReturn(mock(DiaryPageIndex.class));

    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPage(member.getId(), request);

    assertThat(response).as("페이지네이션 응답이 null이 아니어야 합니다").isNotNull();
    assertThat(response.sharedDiaries()).as("페이지네이션 응답의 미리보기 목록은 1개의 항목을 포함해야 합니다").hasSize(1);
    assertThat(response.isEnd()).as("페이지네이션 정보가 있으면 isEnd 조건은 false 이어야 합니다.").isFalse();
    verify(s3FileStorageManager).preSignedFirstWebtoonUrlFrom(webtoonUrl);
    verify(mockProjection).replaceWebtoonImageUrl(preSigned_webtoonUrl);
  }

  @Test
  @DisplayName("페이지네이션의 publicDiaryId가 0일 때 최신 ID를 조회한다")
  void getPagination_WithZeroPublicDiaryId() {

    List<MySharedDiaryPreviewProjection> previews = new ArrayList<>();
    previews.add(mockProjection);

    when(mockProjection.getWebtoonImageUrl()).thenReturn(webtoonUrl);
    when(mySharedDiaryRepository.findNextPreviews(
            eq(member.getId()), any(DiaryPageIndex.class), any(PageRequest.class)))
        .thenReturn(previews);
    DiaryPageRequest request =
        new DiaryPageRequest(mockPublicDiary.getId(), mockPublicDiary.getCreatedTime());
    when(mockIndexFactory.createFrom(request, member.getId()))
        .thenReturn(mock(DiaryPageIndex.class));

    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPage(member.getId(), request);

    assertThat(response).as("publicDiaryId가 0일 때의 페이지네이션 응답이 null이 아니어야 합니다").isNotNull();
    assertThat(response.sharedDiaries())
        .as("publicDiaryId가 0일 때의 미리보기 목록은 1개의 항목을 포함해야 합니다")
        .hasSize(1);
    verify(s3FileStorageManager).preSignedFirstWebtoonUrlFrom(webtoonUrl);
  }

  @Test
  @DisplayName("페이지네이션 요청시 더 이상 공개된 일기가 없을 때 빈 객체 반환")
  void getPagination_ThrowsException_WhenNoDiaryFound() {
    DiaryPageRequest request =
        new DiaryPageRequest(mockPublicDiary.getId(), mockPublicDiary.getCreatedTime());
    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPage(member.getId(), request);
    assertThat(response.sharedDiaries().size()).as("공개된 일기가 없을 때 빈 객체가 반환 되어야 합니다.").isEqualTo(0);
    assertThat(response.isEnd()).as("공개된 일기가 없을 때 isEnd 조건이 true이어야합니다.").isTrue();

    response = mySharedDiaryService.getPage(member.getId(), request);
    assertThat(response.sharedDiaries().size()).as("공개된 일기가 없을 때 빈 객체가 반환 되어야 합니다.").isEqualTo(0);
    assertThat(response.isEnd()).as("공개된 일기가 없을 때 isEnd 조건이 true이어야합니다.").isTrue();

    verify(mockProjection, times(0)).replaceWebtoonImageUrl(preSigned_webtoonUrl);
  }

  //  @Test
  //  @DisplayName("나의 공개 일기 상세를 성공적으로 조회한다")
  //  void getDiary_Success() {
  //    Instant requestDate = Instant.now();
  //    MySharedDiaryContentProjection contentOnly = mock(MySharedDiaryContentProjection.class);
  //    DiaryReactionCountProjection reactionCount = mock(DiaryReactionCountProjection.class);
  //    List<DiaryReactionType> memberReactions =
  //        List.of(DiaryReactionType.LIKE, DiaryReactionType.EMPATHIZE);
  //    Long diaryId = 2L;
  //
  //    when(contentOnly.getDiaryId()).thenReturn(diaryId);
  //    when(contentOnly.getWebtoonImageUrls()).thenReturn(List.of(webtoonUrl));
  //    when(contentOnly.getBgmUrl()).thenReturn(bgmUrl);
  //
  //    when(mySharedDiaryRepository.findContent(member.getId(), requestDate))
  //        .thenReturn(Optional.of(contentOnly));
  //    when(reactionRepository.countEachByPublicDiaryId(anyLong())).thenReturn(reactionCount);
  //    when(reactionRepository.findMemberReactions(member.getId(), diaryId))
  //        .thenReturn(memberReactions);
  //
  //    when(s3FileStorageManager.preSignedWebtoonUrlFrom(any()))
  //        .thenReturn(List.of(preSigned_webtoonUrl));
  //    when(s3FileStorageManager.preSignedBgmUrlFrom(anyString())).thenReturn(preSigned_bgmUrl);
  //
  //        MySharedDiaryResponse response = mySharedDiaryService.getDiary(member.getId(),
  //     requestDate);
  //
  //        assertThat(response).as("조회된 응답이 null이 아니어야 합니다").isNotNull();
  //
  //        assertThat(response.getMyReaction())
  //            .as("사용자의 리액션 목록이 정확히 조회되어야 합니다")
  //            .containsExactly(DiaryReactionType.LIKE, DiaryReactionType.EMPATHIZE);
  //
  //    verify(s3FileStorageManager).preSignedWebtoonUrlFrom(List.of(webtoonUrl));
  //    verify(s3FileStorageManager).preSignedBgmUrlFrom(bgmUrl);
  //    verify(contentOnly).replaceBgmUrl(preSigned_bgmUrl);
  //    verify(contentOnly).replaceWebtoonImageUrls(List.of(preSigned_webtoonUrl));
  //  }

  @Test
  @DisplayName("존재하지 않는 날짜의 공유된 일기 조회시 예외를 던진다")
  void getDiary_ThrowsException_WhenDiaryNotFound() {
    // Given
    Instant requestDate = Instant.now();
    when(mySharedDiaryRepository.findContent(member.getId(), requestDate))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> mySharedDiaryService.getDiary(member.getId(), requestDate))
        .as("존재하지 않는 날짜의 일기 조회시 PublicDiaryNotFoundException이 발생해야 합니다")
        .isInstanceOf(PublicDiaryNotFoundException.class);
  }
}
