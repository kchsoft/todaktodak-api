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

import com.heartsave.todaktodak_api.common.converter.InstantConverter;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
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

  @Mock private TodakUser mockUser;

  @Mock private MySharedDiaryPreviewProjection mockProjection;

  private final Long memberId = 1L;
  private final Long publicDiaryId = 1L;
  private final String webtoonUrl = "webtoonUrl";
  private final String preSigned_webtoonUrl = "preSigned_webtoonUrl";
  private final String bgmUrl = "bgmUrl";
  private final String preSigned_bgmUrl = "preSigned_bgmUrl";

  @BeforeEach
  void setUp() {
    when(mockUser.getId()).thenReturn(memberId);
  }

  @Test
  @DisplayName("성공적으로 페이지네이션 정보를 가져온다")
  void getPagination_Success() {
    List<MySharedDiaryPreviewProjection> previews = new ArrayList<>();
    previews.add(mockProjection);

    when(mockProjection.getWebtoonImageUrl()).thenReturn(webtoonUrl);
    when(s3FileStorageManager.preSignedFirstWebtoonUrlFrom(anyString()))
        .thenReturn(preSigned_webtoonUrl);
    when(mySharedDiaryRepository.findNextPreviews(anyLong(), anyLong(), any(PageRequest.class)))
        .thenReturn(previews);

    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPagination(mockUser.getId(), publicDiaryId);

    assertThat(response).as("페이지네이션 응답이 null이 아니어야 합니다").isNotNull();
    assertThat(response.sharedDiaries()).as("페이지네이션 응답의 미리보기 목록은 1개의 항목을 포함해야 합니다").hasSize(1);
    assertThat(response.isEnd()).as("페이지네이션 정보가 있으면 isEnd 조건은 false 이어야 합니다.").isFalse();
    verify(s3FileStorageManager).preSignedFirstWebtoonUrlFrom(webtoonUrl);
    verify(mockProjection).replaceWebtoonImageUrl(preSigned_webtoonUrl);
  }

  @Test
  @DisplayName("페이지네이션의 publicDiaryId가 0일 때 최신 ID를 조회한다")
  void getPagination_WithZeroPublicDiaryId() {
    Long diaryId = 5L;
    when(mySharedDiaryRepository.findLatestId(memberId)).thenReturn(Optional.of(5L));
    List<MySharedDiaryPreviewProjection> previews = new ArrayList<>();
    previews.add(mockProjection);

    when(mockProjection.getWebtoonImageUrl()).thenReturn(webtoonUrl);
    when(mySharedDiaryRepository.findNextPreviews(
            eq(memberId), eq(diaryId + 1), any(PageRequest.class)))
        .thenReturn(previews);

    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPagination(mockUser.getId(), 0L);

    assertThat(response).as("publicDiaryId가 0일 때의 페이지네이션 응답이 null이 아니어야 합니다").isNotNull();
    assertThat(response.sharedDiaries())
        .as("publicDiaryId가 0일 때의 미리보기 목록은 1개의 항목을 포함해야 합니다")
        .hasSize(1);
    verify(s3FileStorageManager).preSignedFirstWebtoonUrlFrom(webtoonUrl);
  }

  @Test
  @DisplayName("페이지네이션 요청시 더 이상 공개된 일기가 없을 때 빈 객체 반환")
  void getPagination_ThrowsException_WhenNoDiaryFound() {
    when(mySharedDiaryRepository.findLatestId(memberId)).thenReturn(Optional.empty());
    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPagination(mockUser.getId(), 0L);
    assertThat(response.sharedDiaries().size()).as("공개된 일기가 없을 때 빈 객체가 반환 되어야 합니다.").isEqualTo(0);
    assertThat(response.isEnd()).as("공개된 일기가 없을 때 isEnd 조건이 true이어야합니다.").isTrue();

    response = mySharedDiaryService.getPagination(mockUser.getId(), 999999L);
    assertThat(response.sharedDiaries().size()).as("공개된 일기가 없을 때 빈 객체가 반환 되어야 합니다.").isEqualTo(0);
    assertThat(response.isEnd()).as("공개된 일기가 없을 때 isEnd 조건이 true이어야합니다.").isTrue();

    verify(mockProjection, times(0)).replaceWebtoonImageUrl(preSigned_webtoonUrl);
  }

  @Test
  @DisplayName("나의 공개 일기 상세를 성공적으로 조회한다")
  void getDiary_Success() {
    Instant requestDate = Instant.now();
    MySharedDiaryContentOnlyProjection contentOnly = mock(MySharedDiaryContentOnlyProjection.class);
    DiaryReactionCountProjection reactionCount = mock(DiaryReactionCountProjection.class);
    List<DiaryReactionType> memberReactions =
        List.of(DiaryReactionType.LIKE, DiaryReactionType.EMPATHIZE);
    Long diaryId = 2L;

    when(contentOnly.getDiaryId()).thenReturn(diaryId);
    when(contentOnly.getWebtoonImageUrls()).thenReturn(List.of(webtoonUrl));
    when(contentOnly.getBgmUrl()).thenReturn(bgmUrl);

    when(mySharedDiaryRepository.findContentOnly(
            memberId, InstantConverter.toLocalDate(requestDate)))
        .thenReturn(Optional.of(contentOnly));
    when(reactionRepository.countEachByDiaryId(diaryId)).thenReturn(reactionCount);
    when(reactionRepository.findMemberReaction(memberId, diaryId)).thenReturn(memberReactions);

    when(s3FileStorageManager.preSignedWebtoonUrlFrom(any()))
        .thenReturn(List.of(preSigned_webtoonUrl));
    when(s3FileStorageManager.preSignedBgmUrlFrom(anyString())).thenReturn(preSigned_bgmUrl);

    MySharedDiaryResponse response = mySharedDiaryService.getDiary(mockUser.getId(), requestDate);

    assertThat(response).as("조회된 응답이 null이 아니어야 합니다").isNotNull();

    assertThat(response.getMyReaction())
        .as("사용자의 리액션 목록이 정확히 조회되어야 합니다")
        .containsExactly(DiaryReactionType.LIKE, DiaryReactionType.EMPATHIZE);

    verify(s3FileStorageManager).preSignedWebtoonUrlFrom(List.of(webtoonUrl));
    verify(s3FileStorageManager).preSignedBgmUrlFrom(bgmUrl);
    verify(contentOnly).replaceBgmUrl(preSigned_bgmUrl);
    verify(contentOnly).replaceWebtoonImageUrls(List.of(preSigned_webtoonUrl));
  }

  @Test
  @DisplayName("존재하지 않는 날짜의 공유된 일기 조회시 예외를 던진다")
  void getDiary_ThrowsException_WhenDiaryNotFound() {
    // Given
    Instant requestDate = Instant.now();
    when(mySharedDiaryRepository.findContentOnly(
            memberId, InstantConverter.toLocalDate(requestDate)))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> mySharedDiaryService.getDiary(mockUser.getId(), requestDate))
        .as("존재하지 않는 날짜의 일기 조회시 PublicDiaryNotFoundException이 발생해야 합니다")
        .isInstanceOf(PublicDiaryNotFoundException.class);
  }
}
