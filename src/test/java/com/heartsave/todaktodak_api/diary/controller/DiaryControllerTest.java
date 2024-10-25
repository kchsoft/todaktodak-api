package com.heartsave.todaktodak_api.diary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.service.DiaryService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.filter.OncePerRequestFilter;

@WebMvcTest(
    controllers = DiaryController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {OncePerRequestFilter.class})
    })
@AutoConfigureMockMvc(addFilters = false)
public class DiaryControllerTest {

  @MockBean private DiaryService diaryService;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("일기 작성 요청 성공")
  @WithMockTodakUser
  void writeDiarySuccess() throws Exception {
    DiaryWriteRequest request =
        new DiaryWriteRequest(
            LocalDateTime.now(), DiaryEmotion.JOY, BaseTestEntity.DUMMY_STRING_CONTENT);

    final String AI_COMMENT = "this is test ai comment";

    when(diaryService.write(any(TodakUser.class), any(DiaryWriteRequest.class)))
        .thenReturn(DiaryWriteResponse.builder().aiComment(AI_COMMENT).build());
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/v1/diary/my")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andDo(print())
            .andReturn();

    verify(diaryService, times(1)).write(any(TodakUser.class), any(DiaryWriteRequest.class));
    MockHttpServletResponse response = mvcResult.getResponse();
    assertThat(response.getContentAsString()).contains(AI_COMMENT);
  }

  @ParameterizedTest
  @DisplayName("일기 삭제 요청 성공")
  @WithMockTodakUser
  @ValueSource(longs = {1L, Long.MAX_VALUE, 25234L})
  void deleteDiarySuccess(Long diaryId) throws Exception {
    doNothing().when(diaryService).delete(any(TodakUser.class), anyLong());
    MvcResult mvcResult =
        mockMvc
            .perform(delete("/api/v1/diary/my/" + diaryId))
            .andExpect(status().isNoContent())
            .andDo(print())
            .andReturn();
    verify(diaryService, times(1)).delete(any(TodakUser.class), anyLong());
  }

  @ParameterizedTest
  @DisplayName("일기 삭제 요청 실패 - 매개변수 검증 실패")
  @ValueSource(longs = {0L, -1L, -123124L})
  void deleteDiaryFailByParameterValidation(Long diaryId) throws Exception {
    doNothing().when(diaryService).delete(any(TodakUser.class), anyLong());
    MvcResult mvcResult =
        mockMvc
            .perform(delete("/api/v1/diary/my/" + diaryId))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andReturn();
    verify(diaryService, times(0)).delete(any(TodakUser.class), anyLong());
  }
}
