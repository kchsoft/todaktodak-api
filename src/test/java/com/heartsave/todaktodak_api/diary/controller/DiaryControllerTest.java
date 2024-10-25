package com.heartsave.todaktodak_api.diary.controller;

import static com.heartsave.todaktodak_api.diary.common.TestDiaryObjectFactory.getTestDiaryIndexProjections_2024_03_Data_Of_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryIndexResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import com.heartsave.todaktodak_api.diary.service.DiaryService;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
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

  @DisplayName("연월 일기 작성 현황 요청 성공")
  @WithMockTodakUser
  @Test
  void getManyOfDiaryYearMonthSuccess() throws Exception {
    // response
    List<DiaryIndexProjection> mockIndexes = getTestDiaryIndexProjections_2024_03_Data_Of_2();
    DiaryIndexResponse mockResponse =
        DiaryIndexResponse.builder().diaryIndexes(mockIndexes).build();

    // when
    when(diaryService.getIndex(any(TodakUser.class), any(YearMonth.class)))
        .thenReturn(mockResponse);

    // then
    MvcResult mvcResult =
        mockMvc
            .perform(get("/api/v1/diary/my").param("yearMonth", "2024-03"))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();

    assertThat(contentAsString).as("설정한 json 필드의 응답이 올바르지 않습니다").contains("diaryId", "date");

    assertThat(contentAsString)
        .as("설정된 diary index와 응답 index가 서로 다릅니다.")
        .contains(objectMapper.writeValueAsString(mockResponse.getDiaryIndexes().get(0)));

    assertThat(contentAsString)
        .as("설정된 diary index와 응답 index가 서로 다릅니다.")
        .contains(objectMapper.writeValueAsString(mockResponse.getDiaryIndexes().get(1)));
  }

  @DisplayName("연월 일기 작성 현황 0건 성공")
  @WithMockTodakUser
  @Test
  void getZeroOfDiaryYearMonthSuccess() throws Exception {
    // response
    DiaryIndexResponse mockResponse = new DiaryIndexResponse(new ArrayList<>());

    // when
    when(diaryService.getIndex(any(TodakUser.class), any(YearMonth.class)))
        .thenReturn(mockResponse);

    // then
    MvcResult mvcResult =
        mockMvc
            .perform(get("/api/v1/diary/my").param("yearMonth", "2024-01"))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    System.out.println("contentAsString = " + contentAsString);
    assertThat(contentAsString).as("응답에 null 이 있습니다.").doesNotContain("null", "Null", "NULL");
  }
}
