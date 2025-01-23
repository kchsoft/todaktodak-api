package com.heartsave.todaktodak_api.diary.integrate;

import static com.heartsave.todaktodak_api.common.BaseTestObject.createDiaryNoIdWithMember;
import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.config.BaseIntegrateTest;
import com.heartsave.todaktodak_api.domain.diary.dto.request.PublicDiaryWriteRequest;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.domain.diary.exception.PublicDiaryExistException;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.service.PublicDiaryCacheService;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicDiaryIntegrateTest extends BaseIntegrateTest {
  @Autowired PublicDiaryRepository publicDiaryRepository;
  @Autowired DiaryRepository diaryRepository;
  @Autowired MemberRepository memberRepository;

  @Autowired PublicDiaryCacheService publicDiaryCacheService;

  private DiaryEntity diary;
  private MemberEntity member;

  private int diaryCnt = 10;
  private List<DiaryEntity> diaryList;
  private List<PublicDiaryEntity> publicDiaryList;

  @BeforeAll
  void init() {
    // set member
    member = BaseTestObject.createMember();
    memberRepository.save(member);

    // set 10 diary
    diaryList = new ArrayList<>();
    for (int i = 0; i < diaryCnt; i++) {
      diaryList.add(createDiaryNoIdWithMember(member));
    }
    diaryRepository.saveAll(diaryList);

    // set main test diary
    diary = diaryList.getFirst();

    // set 10 public diary
    for (int i = 0; i < diaryList.size(); i++) {
      publicDiaryRepository.save(
          PublicDiaryEntity.builder()
              .diaryEntity(diaryList.get(i))
              .memberEntity(member)
              .publicContent("public-content" + (i + 1))
              .build());
    }
    publicDiaryList = publicDiaryRepository.findAll();
  }

  @Nested
  @DisplayName("공개 일기 작성 테스트")
  class PublicDiary_Write_Test {

    @Test
    @DisplayName("공개 일기 작성 성공")
    @WithMockTodakUser
    void write_Success() throws Exception {

      // given
      PublicDiaryEntity first = publicDiaryList.getFirst();
      publicDiaryRepository.deleteById(first.getId());
      entityManager.flush();
      entityManager.clear();

      PublicDiaryWriteRequest request =
          new PublicDiaryWriteRequest(first.getDiaryEntity().getId(), "public-content");

      // when & then
      mockMvc
          .perform(
              post("/api/v1/diary/public")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNoContent())
          .andReturn();
    }

    @Test
    @DisplayName("공개 일기 작성 실패 - 중복된 공개 일기 작성")
    @WithMockTodakUser
    void write_Fail_Duplicated_Public_Diary() throws Exception {

      // given
      PublicDiaryWriteRequest request =
          new PublicDiaryWriteRequest(diary.getId(), "public-content");

      // when & then
      mockMvc
          .perform(
              post("/api/v1/diary/public")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(PublicDiaryExistException.class))
          .andDo(print())
          .andReturn();
    }

    @Test
    @DisplayName("공개 일기 작성 실패 - 없는 diaryId에 대한 공개 일기 작성")
    @WithMockTodakUser
    void write_Fail_No_Diary() throws Exception {
      Long notExistDiaryId = 99999L;

      PublicDiaryWriteRequest request =
          new PublicDiaryWriteRequest(notExistDiaryId, "public-content");
      mockMvc
          .perform(
              post("/api/v1/diary/public")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(DiaryNotFoundException.class))
          .andDo(print())
          .andReturn();
    }
  }

  @Nested
  @DisplayName("공개 일기 조회 테스트")
  class PublicDiary_Read_Test {

    @ParameterizedTest
    @DisplayName("공개 일기 조회 성공 - RDBMS")
    @WithMockTodakUser
    @CsvSource({"9,8,4", "6,5,1", "4,3,0", "1,0,0"}) // diaryCnt is 10
    void getPagination_Success(Integer target, Integer responseFirst, Integer responseLast)
        throws Exception {
      // no cache
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // query parameter
      PublicDiaryEntity requestParam = publicDiaryList.get(target);
      MvcResult mvcResult =
          mockMvc
              .perform(
                  get("/api/v1/diary/public")
                      .param("after", requestParam.getId().toString())
                      .param("date", requestParam.getCreatedTime().toString())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(
                  jsonPath("$.diaries[0].publicDiaryId")
                      .value(publicDiaryList.get(responseFirst).getId()))
              .andExpect(
                  jsonPath("$.diaries[-1].publicDiaryId")
                      .value(publicDiaryList.get(responseLast).getId()))
              .andDo(print())
              .andReturn();
    }

    @Test
    @DisplayName("최신 공개 일기 조회 성공 - 파라미터 없음")
    @WithMockTodakUser
    void Recent_getPagination_NoParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(get("/api/v1/diary/public").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.diaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.diaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - PUBLIC_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 공개 일기 조회 성공 - 기본 파라미터")
    @WithMockTodakUser
    void Recent_getPagination_DefaultParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(
              get("/api/v1/diary/public")
                  .param("after", "0")
                  .param("date", "1970-01-01T00:00:00Z")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.diaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.diaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - PUBLIC_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 공개 일기 조회 성공 - after 파라미터만")
    @WithMockTodakUser
    void Recent_getPagination_AfterParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(
              get("/api/v1/diary/public")
                  .param("after", "0")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.diaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.diaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - PUBLIC_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 공개 일기 조회 성공 - date 파라미터만")
    @WithMockTodakUser
    void Recent_getPagination_DateParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(
              get("/api/v1/diary/public")
                  .param("date", "1970-01-01T00:00:00Z")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.diaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.diaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - PUBLIC_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("공개 일기 0개 조회 성공 - RDBMS")
    @WithMockTodakUser
    void Zero_getPagination_Success() throws Exception {
      // no cache
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());
      // no public diary
      publicDiaryRepository.deleteAll();

      mockMvc
          .perform(get("/api/v1/diary/public").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isEnd").value(true))
          .andDo(print())
          .andReturn();
    }
  }
}
