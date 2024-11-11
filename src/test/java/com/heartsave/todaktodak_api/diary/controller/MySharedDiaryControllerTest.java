package com.heartsave.todaktodak_api.diary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.service.MySharedDiaryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MySharedDiaryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MySharedDiaryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MySharedDiaryService mockMySharedDiaryService;

  @MockBean private TodakUser mockUser;

  @Mock private MySharedDiaryResponse diaryResponse;
  private MySharedDiaryPaginationResponse paginationResponse;

  @BeforeEach
  void setUp() {
    List<MySharedDiaryPreviewProjection> previews = createProjections();
    paginationResponse = MySharedDiaryPaginationResponse.of(previews);
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
    when(mockMySharedDiaryService.getPagination(anyLong(), anyLong()))
        .thenReturn(paginationResponse);
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
    verify(mockMySharedDiaryService, times(1)).getPagination(anyLong(), anyLong());
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
    when(mockMySharedDiaryService.getPagination(anyLong(), anyLong()))
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
    when(mockMySharedDiaryService.getPagination(anyLong(), anyLong()))
        .thenReturn(paginationResponse);

    mockMvc
        .perform(get("/api/v1/diary/my/shared").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sharedDiaries").exists());
  }

  @Test
  @WithMockTodakUser
  @DisplayName("특정 날짜의 공개된 일기 상세 조회 성공")
  void getMySharedDiary_Success() throws Exception {
    when(diaryResponse.getPublicDiaryId()).thenReturn(1L);
    when(diaryResponse.getWebtoonImageUrls())
        .thenReturn(
            List.of(
                "https://s3-webtoon-url/1",
                "https://s3-webtoon-url/2",
                "https://s3-webtoon-url/3",
                "https://s3-webtoon-url/4"));
    when(diaryResponse.getPublicContent()).thenReturn("public-content");
    when(diaryResponse.getBgmUrl()).thenReturn("https://s3-bgm-url");
    when(diaryResponse.getMyReaction())
        .thenReturn(List.of(DiaryReactionType.LIKE, DiaryReactionType.CHEERING));
    when(diaryResponse.getReactionCount())
        .thenReturn(
            new DiaryReactionCountProjection() {
              @Override
              public Long getLikes() {
                return 1L;
              }

              @Override
              public Long getSurprised() {
                return 0L;
              }

              @Override
              public Long getEmpathize() {
                return 0L;
              }

              @Override
              public Long getCheering() {
                return 1L;
              }
            });
    LocalDateTime now = LocalDateTime.now();
    when(diaryResponse.getDiaryCreatedDate()).thenReturn(now.toLocalDate());
    when(mockMySharedDiaryService.getDiary(anyLong(), any(LocalDate.class)))
        .thenReturn(diaryResponse);

    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/api/v1/diary/my/shared/detail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("date", now.toString()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicDiaryId").value(diaryResponse.getPublicDiaryId()))
            .andExpect(jsonPath("$.publicContent").value(diaryResponse.getPublicContent()))
            .andExpect(jsonPath("$.bgmUrl").value(diaryResponse.getBgmUrl()))
            .andExpect(
                jsonPath("$.diaryCreatedDate")
                    .value(diaryResponse.getDiaryCreatedDate().toString()))
            .andReturn();

    String contentAsString = mvcResult.getResponse().getContentAsString();
    List<String> webtoonImageUrls = diaryResponse.getWebtoonImageUrls();
    assertThat(contentAsString)
        .as("모든 웹툰 이미지 URL이 응답에 포함되어야 합니다")
        .contains(
            webtoonImageUrls.get(0),
            webtoonImageUrls.get(1),
            webtoonImageUrls.get(2),
            webtoonImageUrls.get(3));

    verify(mockMySharedDiaryService, times(1)).getDiary(anyLong(), any(LocalDate.class));
  }

  @Test
  @WithMockTodakUser
  @DisplayName("미래 날짜로 요청시 실패")
  void getMySharedDiary_WithFutureDate_BadRequest() throws Exception {
    LocalDate futureDate = LocalDate.now().plusDays(1);

    mockMvc
        .perform(
            get("/api/v1/diary/my/shared/detail")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", futureDate.toString()))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockTodakUser
  @DisplayName("날짜 파라미터 누락시 실패")
  void getMySharedDiary_WithoutDateParam_BadRequest() throws Exception {
    mockMvc
        .perform(get("/api/v1/diary/my/shared/detail").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockTodakUser
  @DisplayName("잘못된 날짜 형식으로 요청시 실패")
  void getMySharedDiary_WithInvalidDateFormat_BadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/diary/my/shared/detail")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2024-13-45"))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockTodakUser
  @DisplayName("존재하지 않는 날짜의 일기 조회시 404 응답")
  void getMySharedDiary_WithNonExistentDate_NotFound() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    when(mockMySharedDiaryService.getDiary(anyLong(), any(LocalDate.class)))
        .thenThrow(
            new PublicDiaryNotFoundException(
                PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, now.toLocalDate()));

    mockMvc
        .perform(
            get("/api/v1/diary/my/shared/detail")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", now.toString()))
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}
