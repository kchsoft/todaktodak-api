package com.heartsave.todaktodak_api.diary.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DiaryReactionServiceTest {
  @Mock private DiaryReactionRepository mockReactionRepository;
  @Mock private MemberRepository mockMemberRepository;
  @Mock private PublicDiaryRepository mockPublicDiaryRepository;
  @InjectMocks private DiaryReactionService reactionService;

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
  @DisplayName("toggleReactionStatus - 반응 추가/삭제 토글 테스트")
  void toggleReactionStatus() {
    when(mockPublicDiaryRepository.getReferenceById(anyLong()))
        .thenReturn(mock(PublicDiaryEntity.class));
    when(mockMemberRepository.getReferenceById(anyLong())).thenReturn(mock(MemberEntity.class));

    PublicDiaryReactionRequest request =
        new PublicDiaryReactionRequest(diary.getId(), DiaryReactionType.LIKE);
    DiaryReactionEntity reactionEntity =
        DiaryReactionEntity.builder()
            .memberEntity(mockMemberRepository.getReferenceById(member.getId()))
            .publicDiaryEntity(mockPublicDiaryRepository.getReferenceById(publicDiary.getId()))
            .reactionType(DiaryReactionType.LIKE)
            .build();

    when(mockReactionRepository.hasReaction(any(), any(), any())).thenReturn(false);
    when(mockReactionRepository.save(any(DiaryReactionEntity.class))).thenReturn(reactionEntity);

    // 첫 번째 - 반응 추가
    reactionService.toggleReactionStatus(member.getId(), request);

    verify(mockReactionRepository, times(1))
        .hasReaction(anyLong(), anyLong(), any(DiaryReactionType.class));
    verify(mockReactionRepository, times(1)).save(any(DiaryReactionEntity.class));
    verify(mockReactionRepository, times(0))
        .deleteReaction(anyLong(), anyLong(), any(DiaryReactionType.class));

    // 두 번째 - 준비
    when(mockReactionRepository.hasReaction(any(), any(), any())).thenReturn(true);
    when(mockReactionRepository.deleteReaction(
            member.getId(), diary.getId(), DiaryReactionType.LIKE))
        .thenReturn(1);

    // 두 번재 - 반응 삭제

    reactionService.toggleReactionStatus(member.getId(), request);

    verify(mockReactionRepository, times(2))
        .hasReaction(anyLong(), anyLong(), any(DiaryReactionType.class));
    verify(mockReactionRepository, times(1)).save(any(DiaryReactionEntity.class));
    verify(mockReactionRepository, times(1))
        .deleteReaction(member.getId(), diary.getId(), DiaryReactionType.LIKE);
  }

  @Test
  @DisplayName("toggleReactionStatus - 다른 타입의 반응 추가 테스트")
  void toggleDifferentReactionTypes() {
    PublicDiaryReactionRequest likeRequest =
        new PublicDiaryReactionRequest(diary.getId(), DiaryReactionType.LIKE);
    PublicDiaryReactionRequest cheeringRequest =
        new PublicDiaryReactionRequest(diary.getId(), DiaryReactionType.CHEERING);

    when(mockReactionRepository.hasReaction(anyLong(), anyLong(), any(DiaryReactionType.class)))
        .thenReturn(false);

    reactionService.toggleReactionStatus(member.getId(), likeRequest);
    reactionService.toggleReactionStatus(member.getId(), cheeringRequest);

    verify(mockReactionRepository, times(2))
        .hasReaction(anyLong(), anyLong(), any(DiaryReactionType.class));
    verify(mockReactionRepository, times(2)).save(any(DiaryReactionEntity.class));
    verify(mockReactionRepository, times(0))
        .deleteReaction(anyLong(), anyLong(), any(DiaryReactionType.class));
  }
}
