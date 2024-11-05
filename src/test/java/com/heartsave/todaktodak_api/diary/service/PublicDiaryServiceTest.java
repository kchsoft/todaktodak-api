package com.heartsave.todaktodak_api.diary.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.LocalDate;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PublicDiaryServiceTest {
  @Mock private DiaryRepository mockDiaryRepository;
  @Mock private PublicDiaryRepository mockPublicDiaryRepository;
  @Mock private DiaryReactionRepository mockDiaryReactionRepository;
  @Mock private S3FileStorageService mocksS3FileStorageService;
  @InjectMocks private PublicDiaryService publicDiaryService;

  private TodakUser principal;
  private MemberEntity member;
  private DiaryEntity diary;
  private final String PUBLIC_CONTENT = "테스트 공개 일기 내용";

  @BeforeEach
  void setup() {
    member = BaseTestEntity.createMember();
    diary = BaseTestEntity.createDiaryWithMember(member);

    principal = mock(TodakUser.class);
    when(principal.getId()).thenReturn(member.getId());
  }

  @Test
  @DisplayName("공개 일기 작성 성공")
  void write_Success() {
    when(mockDiaryRepository.findById(anyLong())).thenReturn(Optional.of(diary));
    PublicDiaryEntity publicDiary =
        PublicDiaryEntity.builder()
            .memberEntity(member)
            .diaryEntity(diary)
            .publicContent(PUBLIC_CONTENT)
            .build();

    publicDiaryService.write(principal, PUBLIC_CONTENT, diary.getId());

    verify(mockPublicDiaryRepository, times(1)).save(any(PublicDiaryEntity.class));

    assertThat(publicDiary)
        .as("생성된 공개 일기가 올바른 정보를 포함하고 있어야 합니다")
        .satisfies(
            pd -> {
              assertThat(pd.getPublicContent())
                  .as("공개 일기 내용이 입력된 내용과 일치해야 합니다")
                  .isEqualTo(PUBLIC_CONTENT);
              assertThat(pd.getDiaryEntity()).as("공개 일기의 원본 일기가 올바르게 설정되어야 합니다").isEqualTo(diary);
              assertThat(pd.getMemberEntity()).as("공개 일기의 작성자가 올바르게 설정되어야 합니다").isEqualTo(member);
            });
  }

  @Test
  @DisplayName("공개 일기 작성 실패 - 일기를 찾을 수 없음")
  void write_Fail_DiaryNotFound() {
    Long nonExistentDiaryId = Long.MAX_VALUE;
    when(mockDiaryRepository.findById(nonExistentDiaryId)).thenReturn(Optional.empty());

    DiaryNotFoundException exception =
        assertThrows(
            DiaryNotFoundException.class,
            () -> publicDiaryService.write(principal, PUBLIC_CONTENT, nonExistentDiaryId));

    assertThat(exception.getErrorSpec())
        .as("존재하지 않는 일기에 대한 접근 시 DIARY_NOT_FOUND 에러가 발생해야 합니다")
        .isEqualTo(DiaryErrorSpec.DIARY_NOT_FOUND);

    verify(mockPublicDiaryRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("toggleReactionStatus - 반응 추가/삭제 토글 테스트")
  void toggleReactionStatus() {
    PublicDiaryReactionRequest request =
        new PublicDiaryReactionRequest(diary.getId(), DiaryReactionType.LIKE);
    DiaryReactionEntity reactionEntity =
        DiaryReactionEntity.builder()
            .memberEntity(MemberEntity.createById(member.getId()))
            .diaryEntity(DiaryEntity.createById(diary.getId()))
            .reactionType(DiaryReactionType.LIKE)
            .build();

    when(mockDiaryReactionRepository.save(any(DiaryReactionEntity.class)))
        .thenReturn(reactionEntity);

    // 첫 번째 - 반응 추가
    publicDiaryService.toggleReactionStatus(principal, request);

    verify(mockDiaryReactionRepository, times(1)).save(any(DiaryReactionEntity.class));
    verify(mockDiaryReactionRepository, times(0))
        .deleteReaction(anyLong(), anyLong(), any(DiaryReactionType.class));

    // 두 번째 - 준비
    when(mockDiaryReactionRepository.save(any(DiaryReactionEntity.class)))
        .thenThrow(new DataIntegrityViolationException("Duplicate entry"));
    when(mockDiaryReactionRepository.deleteReaction(
            member.getId(), diary.getId(), DiaryReactionType.LIKE))
        .thenReturn(1);

    // 두 번재 - 반응 삭제
    publicDiaryService.toggleReactionStatus(principal, request);

    verify(mockDiaryReactionRepository, times(2)).save(any(DiaryReactionEntity.class));
    verify(mockDiaryReactionRepository, times(1))
        .deleteReaction(member.getId(), diary.getId(), DiaryReactionType.LIKE);
  }

  @Test
  @DisplayName("toggleReactionStatus - 다른 타입의 반응 추가 테스트")
  void toggleDifferentReactionTypes() {
    PublicDiaryReactionRequest likeRequest =
        new PublicDiaryReactionRequest(diary.getId(), DiaryReactionType.LIKE);
    PublicDiaryReactionRequest cheeringRequest =
        new PublicDiaryReactionRequest(diary.getId(), DiaryReactionType.CHEERING);

    DiaryReactionEntity likeReaction =
        DiaryReactionEntity.builder()
            .memberEntity(MemberEntity.createById(member.getId()))
            .diaryEntity(DiaryEntity.createById(diary.getId()))
            .reactionType(DiaryReactionType.LIKE)
            .build();

    DiaryReactionEntity cheeringReaction =
        DiaryReactionEntity.builder()
            .memberEntity(MemberEntity.createById(member.getId()))
            .diaryEntity(DiaryEntity.createById(diary.getId()))
            .reactionType(DiaryReactionType.CHEERING)
            .build();

    when(mockDiaryReactionRepository.save(any(DiaryReactionEntity.class)))
        .thenReturn(likeReaction)
        .thenReturn(cheeringReaction);

    publicDiaryService.toggleReactionStatus(principal, likeRequest);
    publicDiaryService.toggleReactionStatus(principal, cheeringRequest);

    verify(mockDiaryReactionRepository, times(2)).save(any(DiaryReactionEntity.class));
    verify(mockDiaryReactionRepository, times(0))
        .deleteReaction(anyLong(), anyLong(), any(DiaryReactionType.class));
  }

  @Test
  @DisplayName("getPublicDiaryPaginationResponse - 공개 일기 조회 성공")
  void getPublicDiaryPaginationResponse_Success() {
    Long publicDiaryId = 1L;
    Long memberId = principal.getId();

    // Projection mock 데이터 준비
    PublicDiaryContentOnlyProjection content = mock(PublicDiaryContentOnlyProjection.class);
    when(content.getPublicDiaryId()).thenReturn(publicDiaryId);
    when(content.getDiaryId()).thenReturn(diary.getId());
    when(content.getWebtoonImageUrls()).thenReturn(List.of("webtoon/image.jpg"));
    when(content.getCharacterImageUrl()).thenReturn("character/image.jpg");
    when(content.getBgmUrl()).thenReturn("bgm/music.mp3");
    when(content.getNickname()).thenReturn("nickname");
    when(content.getPublicContent()).thenReturn("content");
    when(content.getDate()).thenReturn(LocalDate.now());

    // S3 URL 생성 mock
    List<String> mockWebtoonUrls = List.of("presigned-webtoon-url");
    when(mocksS3FileStorageService.preSignedWebtoonUrlFrom(any())).thenReturn(mockWebtoonUrls);
    when(mocksS3FileStorageService.preSignedCharacterImageUrlFrom(any()))
        .thenReturn("presigned-character-url");
    when(mocksS3FileStorageService.preSignedBgmUrlFrom(any())).thenReturn("presigned-bgm-url");

    // Repository mock 설정
    when(mockPublicDiaryRepository.findNextContentOnlyById(anyLong(), any(PageRequest.class)))
        .thenReturn(List.of(content));

    // 반응 정보 mock
    DiaryReactionCountProjection reactionCount = mock(DiaryReactionCountProjection.class);
    when(mockDiaryReactionRepository.countEachByDiaryId(diary.getId())).thenReturn(reactionCount);
    when(mockDiaryReactionRepository.findMemberReaction(memberId, diary.getId()))
        .thenReturn(List.of(DiaryReactionType.LIKE));

    // when
    PublicDiaryPaginationResponse response =
        publicDiaryService.getPublicDiaryPagination(principal, publicDiaryId);

    // then
    assertThat(response).as("응답이 null이 아니어야 합니다").isNotNull();

    List<PublicDiary> publicDairies = response.getDiaries();
    assertThat(publicDairies).as("조회된 일기 목록이 비어있지 않아야 합니다").isNotNull();
    assertThat(publicDairies.size()).as("조회된 일기 목록이 비어있지 않아야 합니다").isGreaterThan(0);

    PublicDiary publicDiary = publicDairies.get(0);
    assertThat(publicDiary)
        .as("조회된 일기의 상세 정보가 올바르게 매핑되어야 합니다")
        .satisfies(
            diary -> {
              assertThat(diary.getPublicDiaryId()).isEqualTo(publicDiaryId);
              assertThat(diary.getDiaryId()).isEqualTo(this.diary.getId());
              assertThat(diary.getCharacterImageUrl()).isEqualTo("character/image.jpg");
              assertThat(diary.getWebtoonUrls().getFirst()).isEqualTo("webtoon/image.jpg");
              assertThat(diary.getWebtoonUrls().size()).isEqualTo(1);
              assertThat(diary.getBgmUrl()).isEqualTo("bgm/music.mp3");
              assertThat(diary.getNickname()).isEqualTo("nickname");
              assertThat(diary.getPublicContent()).isEqualTo("content");
              assertThat(diary.getMyReaction().getFirst()).isEqualTo(DiaryReactionType.LIKE);
            });

    // 메서드 호출 검증
    verify(mockPublicDiaryRepository).findNextContentOnlyById(anyLong(), any(PageRequest.class));
    verify(mockDiaryReactionRepository).countEachByDiaryId(diary.getId());
    verify(mockDiaryReactionRepository).findMemberReaction(memberId, diary.getId());
    verify(mocksS3FileStorageService).preSignedWebtoonUrlFrom(any());
    verify(mocksS3FileStorageService).preSignedCharacterImageUrlFrom(any());
    verify(mocksS3FileStorageService).preSignedBgmUrlFrom(any());
    verify(content).replaceBgmUrl(any());
    verify(content).replaceCharacterImageUrl(any());
    verify(content).replaceCharacterImageUrl(any());
  }

  @Test
  @DisplayName("getPublicDiaryPaginationResponse - 최신 일기 조회 (publicDiaryId = 0)")
  void getPublicDiaryPaginationResponse_LatestDiary() {
    // given
    Long latestId = 100L;
    when(mockPublicDiaryRepository.findLatestId()).thenReturn(Optional.of(latestId));
    when(mockPublicDiaryRepository.findNextContentOnlyById(
            eq(latestId + 1), any(PageRequest.class)))
        .thenReturn(List.of());

    // when
    PublicDiaryPaginationResponse response =
        publicDiaryService.getPublicDiaryPagination(principal, 0L);

    // then
    verify(mockPublicDiaryRepository).findLatestId();
    verify(mockPublicDiaryRepository)
        .findNextContentOnlyById(eq(latestId + 1), any(PageRequest.class));
  }

  @Test
  @DisplayName("getPublicDiaryPaginationResponse - 조회 결과가 없는 경우")
  void getPublicDiaryPaginationResponse_EmptyResult() {
    Long publicDiaryId = 999L;
    when(mockPublicDiaryRepository.findNextContentOnlyById(anyLong(), any(PageRequest.class)))
        .thenReturn(List.of());

    PublicDiaryPaginationResponse response =
        publicDiaryService.getPublicDiaryPagination(principal, publicDiaryId);

    assertThat(response.getDiaries().size()).as("조회 결과가 없는 경우 빈 목록이 반환되어야 합니다").isEqualTo(0);
  }
}
