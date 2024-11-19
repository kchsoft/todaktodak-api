package com.heartsave.todaktodak_api.diary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.service.DiaryReactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DiaryReactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DiaryReactionControllerTest {

  @MockBean private DiaryReactionService mockReactionService;
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  @DisplayName("정상적인 반응 토글 요청 시 성공")
  @WithMockTodakUser
  void toggleReaction_Success() throws Exception {
    Long diaryId = 1L;
    DiaryReactionType reactionType = DiaryReactionType.LIKE;
    PublicDiaryReactionRequest request = new PublicDiaryReactionRequest(diaryId, reactionType);

    doNothing()
        .when(mockReactionService)
        .toggleReactionStatus(anyLong(), any(PublicDiaryReactionRequest.class));

    mockMvc
        .perform(
            post("/api/v1/diary/public/reaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("반응 토글 실패 - 잘못된 일기 ID")
  @WithMockTodakUser
  void toggleReaction_Fail_InvalidDiaryId() throws Exception {
    Long invalidDiaryId = 0L;
    DiaryReactionType reactionType = DiaryReactionType.LIKE;
    PublicDiaryReactionRequest request =
        new PublicDiaryReactionRequest(invalidDiaryId, reactionType);

    mockMvc
        .perform(
            post("/api/v1/diary/public/reaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("반응 토글 실패 - 반응 타입이 null")
  @WithMockTodakUser
  void toggleReaction_Fail_NullReactionType() throws Exception {
    Long diaryId = 1L;
    DiaryReactionType reactionType = null;
    PublicDiaryReactionRequest request = new PublicDiaryReactionRequest(diaryId, reactionType);

    mockMvc
        .perform(
            post("/api/v1/diary/public/reaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("반응 토글 실패 - 잘못된 요청 형식")
  @WithMockTodakUser
  void toggleReaction_Fail_InvalidRequestFormat() throws Exception {
    String invalidRequest = "{\"publicDiaryId\": \"invalid\", \"reactionType\": \"like\"}";

    mockMvc
        .perform(
            post("/api/v1/diary/public/reaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
        .andExpect(status().isBadRequest());
  }
}
