package com.heartsave.todaktodak_api.diary.integrate;

import static com.heartsave.todaktodak_api.common.BaseTestObject.createDiaryNoIdWithMember;
import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.config.BaseIntegrateTest;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.service.PublicDiaryCacheService;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.time.Instant;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MySharedDiaryIntegrateTest extends BaseIntegrateTest {
  @Autowired private PublicDiaryRepository publicDiaryRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private MemberRepository memberRepository;

  @Autowired private S3FileStorageManager s3FileStorageManager;
  @Autowired private PublicDiaryCacheService publicDiaryCacheService;

  private MemberEntity member;
  private final int diaryCnt = 20;
  private List<DiaryEntity> diaryList;
  private List<PublicDiaryEntity> publicDiaryList;
  private final String preSigned_webtoonUrl = "preSigned_webtoonUrl";
  private final String preSigned_bgmUrl = "preSigned_bgmUrl";
  private final String DEFAULT_URL = "/api/v1/diary/my/shared";

  @BeforeAll
  void init() {
    // set member
    member = BaseTestObject.createMember();
    memberRepository.save(member);

    // set 20 diary
    diaryList = new ArrayList<>();
    for (int i = 0; i < diaryCnt; i++) {
      diaryList.add(createDiaryNoIdWithMember(member));
    }
    diaryList = diaryRepository.saveAll(diaryList);

    // set 20 public diary with urls
    publicDiaryList = new ArrayList<>();
    for (int i = 0; i < diaryList.size(); i++) {
      publicDiaryRepository.save(
          PublicDiaryEntity.builder()
              .diaryEntity(diaryList.get(i))
              .memberEntity(member)
              .publicContent("public-content" + (i + 1))
              .build());
    }
    publicDiaryList = publicDiaryRepository.findAll();

    // mock S3 pre-signed URLs
    when(s3FileStorageManager.preSignedFirstWebtoonUrlFrom(anyString()))
        .thenReturn(preSigned_webtoonUrl);
    when(s3FileStorageManager.preSignedWebtoonUrlFrom(any()))
        .thenReturn(List.of(preSigned_webtoonUrl));
    when(s3FileStorageManager.preSignedBgmUrlFrom(anyString())).thenReturn(preSigned_bgmUrl);
  }

  @Nested
  @DisplayName("나의 공개 일기 조회 테스트")
  class MySharedDiary_GetPagination_Test {

    @ParameterizedTest
    @DisplayName("일기 목록 조회 성공")
    @WithMockTodakUser
    @CsvSource({"19,18,7", "16,15,4", "10,9,0", "1,0,0"})
    void getPagination_Success(Integer target, Integer responseFirst, Integer responseLast)
        throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      PublicDiaryEntity requestParam = publicDiaryList.get(target);
      mockMvc
          .perform(
              get(DEFAULT_URL)
                  .param("after", requestParam.getId().toString())
                  .param("date", requestParam.getCreatedTime().toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.sharedDiaries[0].publicDiaryId")
                  .value(publicDiaryList.get(responseFirst).getId()))
          .andExpect(
              jsonPath("$.sharedDiaries[-1].publicDiaryId")
                  .value(publicDiaryList.get(responseLast).getId()))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 페이지 조회 성공 - 파라미터 없음")
    @WithMockTodakUser
    void Recent_getPagination_NoParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(get(DEFAULT_URL).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.sharedDiaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.sharedDiaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - MY_SHARED_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 페이지 조회 성공 - 기본 파라미터")
    @WithMockTodakUser
    void Recent_getPagination_DefaultParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());
      // When & Then
      mockMvc
          .perform(
              get(DEFAULT_URL)
                  .param("after", "0")
                  .param("date", "1970-01-01T00:00:00Z")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.sharedDiaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.sharedDiaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - MY_SHARED_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 페이지 조회 성공 - after 파라미터만")
    @WithMockTodakUser
    void Recent_getPagination_AfterParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(get(DEFAULT_URL).param("after", "0").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.sharedDiaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.sharedDiaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - MY_SHARED_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("최신 페이지 조회 성공 - date 파라미터만")
    @WithMockTodakUser
    void Recent_getPagination_DateParameter_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());

      // When & Then
      mockMvc
          .perform(
              get(DEFAULT_URL)
                  .param("date", "1970-01-01T00:00:00Z")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.sharedDiaries[0].publicDiaryId").value(publicDiaryList.getLast().getId()))
          .andExpect(
              jsonPath("$.sharedDiaries[-1].publicDiaryId")
                  .value(publicDiaryList.getLast().getId() - MY_SHARED_DIARY_PAGE_SIZE + 1))
          .andDo(print());
    }

    @Test
    @DisplayName("공개 일기 0개 조회 성공")
    @WithMockTodakUser
    void Zero_getPagination_Success() throws Exception {
      // Given
      when(publicDiaryCacheService.getContentReactionCounts(any())).thenReturn(new ArrayList<>());
      publicDiaryRepository.deleteAll();
      entityManager.flush();
      entityManager.clear();

      // When & Then
      mockMvc
          .perform(get(DEFAULT_URL).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isEnd").value(true))
          .andDo(print());
    }
  }

  @Nested
  @DisplayName("나의 공개 일기 상세 조회 테스트")
  class MySharedDiary_GetDetail_Test {

    @ParameterizedTest
    @DisplayName("상세 조회 성공")
    @WithMockTodakUser
    @CsvSource({"0", "3", "9", "10", "17"})
    void getDetail_Success(Integer target) throws Exception {
      // Given
      PublicDiaryEntity publicDiary = publicDiaryList.get(target);

      // When & Then
      mockMvc
          .perform(
              get(DEFAULT_URL + "/detail")
                  .param("date", publicDiary.getCreatedTime().toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.publicDiaryId").value(publicDiary.getId()))
          .andExpect(jsonPath("$.publicContent").value(publicDiary.getPublicContent()))
          .andDo(print());
    }

    @Test
    @DisplayName("상세 조회 실패 - 존재하지 않는 날짜")
    @WithMockTodakUser
    void getDetail_Fail_NotFound() throws Exception {
      // Given
      Instant nonExistentDate = Instant.parse("2000-01-01T00:00:00Z");

      // When & Then
      mockMvc
          .perform(
              get(DEFAULT_URL + "/detail")
                  .param("date", nonExistentDate.toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(PublicDiaryNotFoundException.class))
          .andDo(print());
    }
  }

  @Nested
  @DisplayName("나의 공개 일기 삭제 테스트")
  class MySharedDiary_Delete_Test {

    @Test
    @DisplayName("삭제 성공")
    @WithMockTodakUser
    void delete_Success() throws Exception {
      // Given
      PublicDiaryEntity targetDiary = publicDiaryList.getFirst();

      // When & Then
      mockMvc
          .perform(delete(DEFAULT_URL + "/{publicDiaryId}", targetDiary.getId()))
          .andExpect(status().isNoContent())
          .andDo(print());

      // Verify diary is deleted
      assertThat(publicDiaryRepository.findById(targetDiary.getId())).isEmpty();
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 ID")
    @WithMockTodakUser
    void delete_Fail_NotFound() throws Exception {
      // Given
      Long nonExistentId = 99999L;

      // When & Then
      mockMvc
          .perform(
              delete(DEFAULT_URL + "/{publicDiaryId}", nonExistentId)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(PublicDiaryNotFoundException.class))
          .andDo(print());
    }

    @Test
    @DisplayName("삭제 실패 - 다른 사용자의 일기")
    @WithMockTodakUser
    void delete_Fail_OtherUserDiary() throws Exception {
      // Given
      MemberEntity otherMember = BaseTestObject.createMemberNoId();
      memberRepository.save(otherMember);

      DiaryEntity otherDiary = createDiaryNoIdWithMember(otherMember);
      diaryRepository.save(otherDiary);

      PublicDiaryEntity otherPublicDiary =
          PublicDiaryEntity.builder().diaryEntity(otherDiary).memberEntity(otherMember).build();
      publicDiaryRepository.save(otherPublicDiary);

      // When & Then
      mockMvc
          .perform(
              delete(DEFAULT_URL + "/{publicDiaryId}", otherPublicDiary.getId())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(
              result ->
                  assertThat(result.getResolvedException())
                      .isInstanceOf(PublicDiaryNotFoundException.class))
          .andDo(print());
    }
  }
}
