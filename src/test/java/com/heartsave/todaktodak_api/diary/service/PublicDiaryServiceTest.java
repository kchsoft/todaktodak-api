package com.heartsave.todaktodak_api.diary.service;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.DIARY.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.BaseTestObject;
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
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
class PublicDiaryServiceTest {
  @Mock private DiaryRepository mockDiaryRepository;
  @Mock private PublicDiaryRepository mockPublicDiaryRepository;
  @Mock private DiaryReactionRepository mockDiaryReactionRepository;
  @Mock private S3FileStorageManager mocksS3FileStorageManager;
  @Mock private MemberRepository mockMemberRepository;
  @Mock private DiaryPageIndexFactory mockIndexFactory;
  @InjectMocks private PublicDiaryService publicDiaryService;

  private MemberEntity member;
  private DiaryEntity diary;
  private PublicDiaryEntity publicDiary;
  private final String PUBLIC_CONTENT = "테스트 공개 일기 내용";

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
    diary = BaseTestObject.createDiaryWithMember(member);
    publicDiary =
        PublicDiaryEntity.builder()
            .memberEntity(member)
            .diaryEntity(diary)
            .publicContent("public-content")
            .build();
  }

  @Test
  @DisplayName("공개 일기 작성 성공")
  void write_Success() {
    DiaryIdsProjection mockIds = mock(DiaryIdsProjection.class);
    when(mockIds.getPublicDiaryId()).thenReturn(null);

    when(mockDiaryRepository.findIdsById(diary.getId())).thenReturn(Optional.of(mockIds));

    publicDiaryService.write(member.getId(), diary.getId(), PUBLIC_CONTENT);

    verify(mockDiaryRepository, times(1)).findIdsById(anyLong());
    verify(mockDiaryRepository, times(1)).getReferenceById(anyLong());
    verify(mockMemberRepository, times(1)).getReferenceById(anyLong());
    verify(mockPublicDiaryRepository, times(1)).save(any(PublicDiaryEntity.class));
  }

  @Test
  @DisplayName("공개 일기 작성 실패 - 일기를 찾을 수 없음")
  void write_Fail_DiaryNotFound() {
    Long nonExistentDiaryId = Long.MAX_VALUE;
    when(mockDiaryRepository.findIdsById(nonExistentDiaryId)).thenReturn(Optional.empty());

    DiaryNotFoundException exception =
        assertThrows(
            DiaryNotFoundException.class,
            () -> publicDiaryService.write(member.getId(), nonExistentDiaryId, PUBLIC_CONTENT));

    assertThat(exception.getErrorSpec())
        .as("존재하지 않는 일기에 대한 접근 시 DIARY_NOT_FOUND 에러가 발생해야 합니다")
        .isEqualTo(DiaryErrorSpec.DIARY_NOT_FOUND);

    verify(mockDiaryRepository, times(1)).findIdsById(anyLong());
    verify(mockPublicDiaryRepository, times(0)).save(any(PublicDiaryEntity.class));
  }

  @Test
  @DisplayName("공개 일기 작성 실패 - 이미 공개 일기가 있음.")
  void write_Fail_PublicDiaryAlreadyExist() {
    DiaryIdsProjection mockIds = mock(DiaryIdsProjection.class);
    when(mockIds.getPublicDiaryId()).thenReturn(100L);
    when(mockDiaryRepository.findIdsById(diary.getId())).thenReturn(Optional.of(mockIds));

    PublicDiaryExistException exception =
        assertThrows(
            PublicDiaryExistException.class,
            () -> publicDiaryService.write(member.getId(), diary.getId(), PUBLIC_CONTENT));

    assertThat(exception.getErrorSpec())
        .as("공개 일기가 이미 존재하고 있다는 예외가 발생해야 합니다.")
        .isEqualTo(PublicDiaryErrorSpec.PUBLIC_DIARY_EXIST);

    verify(mockDiaryRepository, times(1)).findIdsById(anyLong());
    verify(mockDiaryRepository, times(0)).getReferenceById(anyLong());
    verify(mockMemberRepository, times(0)).getReferenceById(anyLong());
    verify(mockPublicDiaryRepository, times(0)).save(any(PublicDiaryEntity.class));
  }

  @Test
  @DisplayName("getPublicDiaryPaginationResponse - 공개 일기 조회 성공")
  void getPublicDiaryPaginationResponse_Success() {
    // request 설정
    Long publicDiaryId = 1L;
    Instant createdTime = Instant.now();
    DiaryPageRequest request = new DiaryPageRequest(publicDiaryId, createdTime);
    Long memberId = member.getId();

    // Projection mock 데이터 준비
    PublicDiaryContentProjection content = mock(PublicDiaryContentProjection.class);
    when(content.getPublicDiaryId()).thenReturn(publicDiaryId);
    when(content.getDiaryId()).thenReturn(diary.getId());
    when(content.getWebtoonImageUrl()).thenReturn(List.of("webtoon/image.jpg"));
    when(content.getCharacterImageUrl()).thenReturn("character/image.jpg");
    when(content.getBgmUrl()).thenReturn("bgm/music.mp3");
    when(content.getNickname()).thenReturn("nickname");
    when(content.getPublicContent()).thenReturn("content");
    when(content.getDate()).thenReturn(Instant.now());

    // S3 URL 생성 mock
    List<String> mockWebtoonUrls = List.of("presigned-webtoon-url");
    when(mocksS3FileStorageManager.preSignedWebtoonUrlFrom(any())).thenReturn(mockWebtoonUrls);
    when(mocksS3FileStorageManager.preSignedCharacterImageUrlFrom(any()))
        .thenReturn("presigned-character-url");
    when(mocksS3FileStorageManager.preSignedBgmUrlFrom(any())).thenReturn("presigned-bgm-url");

    // Repository mock 설정
    when(mockPublicDiaryRepository.findNextContents(
            any(DiaryPageIndex.class), any(PageRequest.class)))
        .thenReturn(List.of(content));

    // 반응 정보 mock
    DiaryReactionCountProjection reactionCount = mock(DiaryReactionCountProjection.class);
    when(mockDiaryReactionRepository.countEachByPublicDiaryId(diary.getId()))
        .thenReturn(reactionCount);
    when(mockDiaryReactionRepository.findMemberReactions(memberId, diary.getId()))
        .thenReturn(List.of(DiaryReactionType.LIKE));

    when(mockIndexFactory.createFrom(request)).thenReturn(mock(DiaryPageIndex.class));

    // when
    PublicDiaryPageResponse response = publicDiaryService.getPagination(member.getId(), request);

    // then
    assertThat(response).as("응답이 null이 아니어야 합니다").isNotNull();
    assertThat(response.getIsEnd()).as("응답 데이터가 있기 때문에 isEnd 조건은 false 이어야 합니다.").isFalse();

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
              assertThat(diary.getWebtoonImageUrls().getFirst()).isEqualTo("webtoon/image.jpg");
              assertThat(diary.getWebtoonImageUrls().size()).isEqualTo(1);
              assertThat(diary.getBgmUrl()).isEqualTo("bgm/music.mp3");
              assertThat(diary.getNickname()).isEqualTo("nickname");
              assertThat(diary.getPublicContent()).isEqualTo("content");
              assertThat(diary.getMyReaction().getFirst()).isEqualTo(DiaryReactionType.LIKE);
            });

    // 메서드 호출 검증
    verify(mockPublicDiaryRepository)
        .findNextContents(any(DiaryPageIndex.class), any(PageRequest.class));
    verify(mockDiaryReactionRepository).countEachByPublicDiaryId(diary.getId());
    verify(mockDiaryReactionRepository).findMemberReactions(memberId, diary.getId());
    verify(mocksS3FileStorageManager).preSignedWebtoonUrlFrom(any());
    verify(mocksS3FileStorageManager).preSignedCharacterImageUrlFrom(any());
    verify(mocksS3FileStorageManager).preSignedBgmUrlFrom(any());
    verify(content).replaceBgmUrl(any());
    verify(content).replaceCharacterImageUrl(any());
    verify(content).replaceCharacterImageUrl(any());
  }

  @Test
  @DisplayName(
      "getPublicDiaryPaginationResponse - 최신 일기 조회 (publicDiaryId = 0,createdTime = Instant.EPOCH)")
  void getPublicDiaryPaginationResponse_LatestDiary() {
    DiaryPageRequest request = new DiaryPageRequest(PAGE_DEFAULT_ID, PAGE_DEFAULT_TIME);
    PublicDiaryContentProjection content = mock(PublicDiaryContentProjection.class);
    DiaryReactionCountProjection reactionCount = mock(DiaryReactionCountProjection.class);
    List<DiaryReactionType> reactionType = mock(List.class);
    when(mockPublicDiaryRepository.findNextContents(
            any(DiaryPageIndex.class), any(PageRequest.class)))
        .thenReturn(List.of(content));
    when(mockDiaryReactionRepository.countEachByPublicDiaryId(anyLong())).thenReturn(reactionCount);
    when(mockDiaryReactionRepository.findMemberReactions(anyLong(), anyLong()))
        .thenReturn(reactionType);
    when(mockIndexFactory.createFrom(request)).thenReturn(mock(DiaryPageIndex.class));

    PublicDiaryPageResponse response = publicDiaryService.getPagination(member.getId(), request);

    assertThat(response.getDiaries().size()).as("조회된 일기가 1개 있어야 한다").isEqualTo(1L);
    assertThat(response.getIsEnd()).as("다음 페이지 조회가 가능하므로 isEnd는 false여야 한다").isFalse();
    verify(mockPublicDiaryRepository)
        .findNextContents(any(DiaryPageIndex.class), any(PageRequest.class));
    verify(mockIndexFactory).createFrom(any(DiaryPageRequest.class));
  }

  @Test
  @DisplayName("getPublicDiaryPaginationResponse - 조회 결과가 없는 경우")
  void getPublicDiaryPaginationResponse_EmptyResult() {
    DiaryPageRequest request = new DiaryPageRequest(999L, Instant.now().plus(10L, ChronoUnit.DAYS));
    when(mockPublicDiaryRepository.findNextContents(
            any(DiaryPageIndex.class), any(PageRequest.class)))
        .thenReturn(List.of());
    when(mockIndexFactory.createFrom(request)).thenReturn(mock(DiaryPageIndex.class));

    PublicDiaryPageResponse response = publicDiaryService.getPagination(member.getId(), request);

    assertThat(response.getDiaries().size()).as("조회 결과가 없는 경우 빈 목록이 반환되어야 합니다").isEqualTo(0);
    assertThat(response.getIsEnd()).as("조회 결과가 없는 경우 isEnd 조건이 True가 돼야 합니다.").isTrue();
  }
}
