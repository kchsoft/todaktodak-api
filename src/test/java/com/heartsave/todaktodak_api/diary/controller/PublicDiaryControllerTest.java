package com.heartsave.todaktodak_api.diary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PublicDiaryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PublicDiaryControllerTest {

  @MockBean private PublicDiaryService publicDiaryService;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("공개 일기 작성 성공")
  @WithMockTodakUser
  void writePublicContent_Success() throws Exception {
    Long diaryId = 1L;
    String content = "테스트 공개 일기 내용";
    PublicDiaryWriteRequest request = new PublicDiaryWriteRequest(diaryId, content);

    doNothing().when(publicDiaryService).write(anyLong(), eq(content), eq(diaryId));

    mockMvc
        .perform(
            post("/api/v1/diary/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("공개 일기 작성 실패 - 잘못된 diaryId")
  @WithMockTodakUser
  void writePublicContent_Fail_InvalidDiaryId() throws Exception {
    Long invalidDiaryId = 0L;
    String content = "테스트 공개 일기 내용";
    PublicDiaryWriteRequest request = new PublicDiaryWriteRequest(invalidDiaryId, content);

    mockMvc
        .perform(
            post("/api/v1/diary/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("정상적인 반응 토글 요청 시 성공")
  @WithMockTodakUser
  void toggleReaction_Success() throws Exception {
    Long diaryId = 1L;
    DiaryReactionType reactionType = DiaryReactionType.LIKE;
    PublicDiaryReactionRequest request = new PublicDiaryReactionRequest(diaryId, reactionType);

    doNothing()
        .when(publicDiaryService)
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
    String invalidRequest = "{\"diaryId\": \"invalid\", \"reactionType\": \"like\"}";

    mockMvc
        .perform(
            post("/api/v1/diary/public/reaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("공개 일기 조회 성공")
  @WithMockTodakUser
  void getPublicDiaryPagination_Success() throws Exception {
    Long publicDiaryId = 1L;
    PublicDiaryPaginationResponse response = new PublicDiaryPaginationResponse();
    PublicDiary publicDiary1 = mock(PublicDiary.class);
    PublicDiary publicDiary2 = mock(PublicDiary.class);
    response.addPublicDiary(publicDiary1);
    response.addPublicDiary(publicDiary2);
    when(publicDiaryService.getPublicDiaryPagination(anyLong(), eq(publicDiaryId)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/diary/public")
                .param("after", String.valueOf(publicDiaryId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(response)));
  }

  @Test
  @DisplayName("공개 일기 조회 성공 - after 파라미터 미입력시 기본값 0 사용")
  @WithMockTodakUser
  void getPublicDiaryPagination_Success_DefaultAfterParameter() throws Exception {
    PublicDiaryPaginationResponse response = new PublicDiaryPaginationResponse();
    when(publicDiaryService.getPublicDiaryPagination(anyLong(), eq(0L))).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/diary/public").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(response)));
  }

  @Test
  @DisplayName("공개 일기 조회 실패 - 음수 after 파라미터")
  @WithMockTodakUser
  void getPublicDiaryPagination_Fail_NegativeAfterParameter() throws Exception {
    // given
    Long invalidPublicDiaryId = -1L;

    // when & then
    mockMvc
        .perform(
            get("/api/v1/diary/public")
                .param("after", String.valueOf(invalidPublicDiaryId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("공개 일기 조회 실패 - 잘못된 형식의 after 파라미터")
  @WithMockTodakUser
  void getPublicDiaryPagination_Fail_InvalidAfterParameterFormat() throws Exception {
    // when & then
    mockMvc
        .perform(
            get("/api/v1/diary/public")
                .param("after", "invalid-id")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}
