package com.heartsave.todaktodak_api.diary.integrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.config.BaseIntegrateTest;
import com.heartsave.todaktodak_api.domain.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.domain.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@TestInstance(Lifecycle.PER_CLASS)
public class DiaryReactionIntegrateTest extends BaseIntegrateTest {

  @Autowired DiaryReactionRepository reactionRepository;
  @Autowired MemberRepository memberRepository;
  @Autowired DiaryRepository diaryRepository;
  @Autowired PublicDiaryRepository publicDiaryRepository;

  private MemberEntity member;
  private DiaryEntity diary;
  private PublicDiaryEntity publicDiary;
  private String DEFAULT_URL = "/api/v1/diary/public/reaction";

  @BeforeAll
  void init() {
    member = BaseTestObject.createMember();
    member = memberRepository.save(member);
  }

  @BeforeEach
  void setup() {
    diary = BaseTestObject.createDiaryNoIdWithMember(member);
    diaryRepository.save(diary);

    publicDiary =
        PublicDiaryEntity.builder()
            .memberEntity(member)
            .diaryEntity(diary)
            .publicContent("public-content")
            .build();
    publicDiaryRepository.save(publicDiary);
  }

  @Test
  @DisplayName("공개 일기 반응 추가/삭제")
  @WithMockTodakUser
  void Reaction_Add_Delete_Test_Success() throws Exception {
    PublicDiaryReactionRequest request =
        new PublicDiaryReactionRequest(publicDiary.getId(), DiaryReactionType.LIKE);

    // add reaction
    mockMvc
        .perform(
            post(DEFAULT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(print());
    List<DiaryReactionType> reactions =
        reactionRepository.findMemberReactions(member.getId(), publicDiary.getId());
    assertThat(reactions.size()).isEqualTo(1);

    // delete reaction
    mockMvc
        .perform(
            post(DEFAULT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(print());
    reactions = reactionRepository.findMemberReactions(member.getId(), publicDiary.getId());
    assertThat(reactions.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("공개 일기 반응 2개 추가")
  @WithMockTodakUser
  void Reaction_2_Add_Test_Success() throws Exception {

    // add reaction
    PublicDiaryReactionRequest request =
        new PublicDiaryReactionRequest(publicDiary.getId(), DiaryReactionType.LIKE);
    mockMvc
        .perform(
            post(DEFAULT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(print());
    List<DiaryReactionType> reactions =
        reactionRepository.findMemberReactions(member.getId(), publicDiary.getId());
    assertThat(reactions.size()).isEqualTo(1);

    // add other reaction
    request = new PublicDiaryReactionRequest(publicDiary.getId(), DiaryReactionType.EMPATHIZE);
    mockMvc
        .perform(
            post(DEFAULT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(print());
    reactions = reactionRepository.findMemberReactions(member.getId(), publicDiary.getId());
    assertThat(reactions.size()).isEqualTo(2);
  }
}
