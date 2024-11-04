package com.heartsave.todaktodak_api.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
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

  @Mock private S3FileStorageService s3FileStorageService;

  @Mock private TodakUser mockUser;

  @Mock private MySharedDiaryPreviewProjection mockProjection;

  private final Long memberId = 1L;
  private final Long publicDiaryId = 1L;
  private final String originalImageUrl = "original-url";
  private final String preSignedUrl = "pre-signed-url";

  @BeforeEach
  void setUp() {
    when(mockUser.getId()).thenReturn(memberId);
  }

  @Test
  @DisplayName("성공적으로 페이지네이션 정보를 가져온다")
  void getPagination_Success() {
    List<MySharedDiaryPreviewProjection> previews = new ArrayList<>();
    previews.add(mockProjection);

    when(mockProjection.getWebtoonImageUrl()).thenReturn(originalImageUrl);
    when(s3FileStorageService.preSignedFirstWebtoonUrlFrom(anyString())).thenReturn(preSignedUrl);
    when(mySharedDiaryRepository.findNextPreviews(anyLong(), anyLong(), any(PageRequest.class)))
        .thenReturn(previews);

    MySharedDiaryPaginationResponse response =
        mySharedDiaryService.getPagination(mockUser, publicDiaryId);

    assertThat(response).as("페이지네이션 응답이 null이 아니어야 합니다").isNotNull();

    assertThat(response.sharedDiaries()).as("페이지네이션 응답의 미리보기 목록은 1개의 항목을 포함해야 합니다").hasSize(1);
  }

  @Test
  @DisplayName("publicDiaryId가 0일 때 최신 ID를 조회한다")
  void getPagination_WithZeroPublicDiaryId() {
    Long diaryId = 5L;
    when(mySharedDiaryRepository.findLatestId(memberId)).thenReturn(Optional.of(5L));
    List<MySharedDiaryPreviewProjection> previews = new ArrayList<>();
    previews.add(mockProjection);

    when(mockProjection.getWebtoonImageUrl()).thenReturn(originalImageUrl);
    when(s3FileStorageService.preSignedFirstWebtoonUrlFrom(anyString())).thenReturn(preSignedUrl);
    when(mySharedDiaryRepository.findNextPreviews(
            eq(memberId), eq(diaryId + 1), any(PageRequest.class)))
        .thenReturn(previews);

    MySharedDiaryPaginationResponse response = mySharedDiaryService.getPagination(mockUser, 0L);

    assertThat(response).as("publicDiaryId가 0일 때의 페이지네이션 응답이 null이 아니어야 합니다").isNotNull();
    assertThat(response.sharedDiaries())
        .as("publicDiaryId가 0일 때의 미리보기 목록은 1개의 항목을 포함해야 합니다")
        .hasSize(1);
  }

  @Test
  @DisplayName("공개된 일기가 없을 때 예외를 던진다")
  void getPagination_ThrowsException_WhenNoDiaryFound() {
    // Given
    when(mySharedDiaryRepository.findLatestId(memberId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> mySharedDiaryService.getPagination(mockUser, 0L))
        .as("공개된 일기가 없을 때 PublicDiaryNotFoundException이 발생해야 합니다")
        .isInstanceOf(PublicDiaryNotFoundException.class);
  }
}
