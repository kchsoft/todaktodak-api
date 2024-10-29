package com.heartsave.todaktodak_api.diary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.service.PublicDiaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

@WebMvcTest(
    controllers = PublicDiaryController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {OncePerRequestFilter.class})
    })
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

    doNothing().when(publicDiaryService).write(any(TodakUser.class), eq(content), eq(diaryId));

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
}
