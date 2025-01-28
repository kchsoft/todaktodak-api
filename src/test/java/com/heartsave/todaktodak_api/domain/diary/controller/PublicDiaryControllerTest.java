package com.heartsave.todaktodak_api.domain.diary.controller;

import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.PAGE_DEFAULT_ID;
import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.PAGE_DEFAULT_TIME;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.config.util.WithMockTodakUser;
import com.heartsave.todaktodak_api.domain.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.domain.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.domain.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.domain.diary.dto.response.PublicDiaryPageResponse;
import com.heartsave.todaktodak_api.domain.diary.service.PublicDiaryService;
import java.time.Instant;
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

    doNothing().when(publicDiaryService).write(anyLong(), eq(diaryId), eq(content));

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
  @DisplayName("공개 일기 조회 성공")
  @WithMockTodakUser
  void getPublicDiaryPagination_Success() throws Exception {
    // request 설정
    Long publicDiaryId = 1L;
    Instant createdTime = Instant.now();
    DiaryPageRequest request = new DiaryPageRequest(publicDiaryId, createdTime);

    // response 설정
    PublicDiaryPageResponse response = new PublicDiaryPageResponse();
    PublicDiary publicDiary1 = mock(PublicDiary.class);
    PublicDiary publicDiary2 = mock(PublicDiary.class);
    response.addPublicDiary(publicDiary1);
    response.addPublicDiary(publicDiary2);

    // when
    when(publicDiaryService.getPagination(anyLong(), eq(request))).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/diary/public")
                .param("after", String.valueOf(publicDiaryId))
                .param("date", createdTime.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(response)))
        .andDo(print());
  }

  @Test
  @DisplayName("공개 일기 조회 성공 - after,date 파라미터 미입력시 기본값 (0L, Instant.EPOCH) 사용")
  @WithMockTodakUser
  void getPublicDiaryPagination_Success_DefaultParameter() throws Exception {
    PublicDiaryPageResponse response = new PublicDiaryPageResponse();
    DiaryPageRequest request = new DiaryPageRequest(PAGE_DEFAULT_ID, PAGE_DEFAULT_TIME);
    PublicDiary publicDiary = mock(PublicDiary.class);
    response.addPublicDiary(publicDiary);
    when(publicDiaryService.getPagination(anyLong(), eq(request))).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/diary/public"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(response)))
        .andDo(print());
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
                .param("date", Instant.now().toString()))
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
