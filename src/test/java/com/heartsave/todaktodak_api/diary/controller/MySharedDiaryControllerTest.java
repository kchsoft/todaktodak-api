package com.heartsave.todaktodak_api.diary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.service.MySharedDiaryService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.filter.OncePerRequestFilter;

@WebMvcTest(
    controllers = MySharedDiaryController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {OncePerRequestFilter.class})
    })
@AutoConfigureMockMvc(addFilters = false)
public class MySharedDiaryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MySharedDiaryService mockMySharedDiaryService;

  @MockBean private TodakUser mockUser;

  private MySharedDiaryPaginationResponse response;

  @BeforeEach
  void setUp() {
    List<MySharedDiaryPreviewProjection> previews = createProjections();
    response = MySharedDiaryPaginationResponse.of(previews);
    when(mockUser.getId()).thenReturn(1L);
  }

  private List<MySharedDiaryPreviewProjection> createProjections() {
    List<MySharedDiaryPreviewProjection> projections = new ArrayList<>();
    for (int i = 5; i > 0; i--) {
      MySharedDiaryPreviewProjection view =
          new MySharedDiaryPreviewProjection(i + 0L, "url_" + i, LocalDate.now().minusDays(6L + i));
      projections.add(view);
    }
    return projections;
  }

  @Test
  @WithMockTodakUser
  @DisplayName("공개된 일기 목록 조회 성공")
  void getMySharedDiaryPreviews_Success() throws Exception {
    when(mockMySharedDiaryService.getPagination(any(TodakUser.class), anyLong()))
        .thenReturn(response);
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/v1/diary/my/shared")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("after", "5"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sharedDiaries").exists())
            .andReturn();
    verify(mockMySharedDiaryService, times(1)).getPagination(any(TodakUser.class), anyLong());
  }

  @Test
  @WithMockTodakUser
  @DisplayName("잘못된 after 파라미터로 요청시 실패")
  void getMySharedDiaryPreviews_WithInvalidAfterParam_BadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/diary/my/shared")
                .contentType(MediaType.APPLICATION_JSON)
                .param("after", "-1"))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockTodakUser
  @DisplayName("빈 목록 조회 성공")
  void getMySharedDiaryPreviews_EmptyList_Success() throws Exception {
    when(mockMySharedDiaryService.getPagination(any(TodakUser.class), anyLong()))
        .thenReturn(MySharedDiaryPaginationResponse.of(List.of()));

    mockMvc
        .perform(
            get("/api/v1/diary/my/shared")
                .contentType(MediaType.APPLICATION_JSON)
                .param("after", "0"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sharedDiaries").isArray())
        .andExpect(jsonPath("$.sharedDiaries").isEmpty())
        .andExpect(jsonPath("$.after").value(1));
  }

  @Test
  @WithMockTodakUser
  @DisplayName("after 파라미터 없이 요청시 기본값 0으로 성공")
  void getMySharedDiaryPreviews_WithoutAfterParam_Success() throws Exception {
    when(mockMySharedDiaryService.getPagination(any(TodakUser.class), anyLong()))
        .thenReturn(response);

    mockMvc
        .perform(get("/api/v1/diary/my/shared").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sharedDiaries").exists());
  }
}
