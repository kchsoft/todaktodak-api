package com.heartsave.todaktodak_api.domain.ai.callback.integrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.config.integrate.BaseIntegrateTest;
import com.heartsave.todaktodak_api.config.util.WithMockTodakUser;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackBgmRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackCharacterRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackWebtoonRequest;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.domain.member.cache.CharacterCache;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@TestInstance(Lifecycle.PER_CLASS) // async test caution
public class AiCallbackIntegrateTest extends BaseIntegrateTest {

  @Autowired private MemberRepository memberRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private WebApplicationContext context;
  @Autowired private CharacterCache characterCache;

  private MockMvc clientMockMvc;
  private MockHttpServletResponse response;

  private MemberEntity member;
  private DiaryEntity diary;
  private String CALLBACK_WEBTOON_URL = "/api/v1/callback/ai/webtoon";
  private String CALLBACK_BGM_URL = "/api/v1/callback/ai/bgm";
  private String CALLBACK_CHARACTER_URL = "/api/v1/callback/ai/character";

  @BeforeAll
  void init() {
    member = BaseTestObject.createMember();
    memberRepository.save(member);
    clientMockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @BeforeEach
  void setup() throws Exception {
    String SSE_URL = "/api/v1/event";
    MvcResult mvcResult =
        clientMockMvc
            .perform(get(SSE_URL).accept(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();
    response = mvcResult.getResponse();
  }

  @Test
  @DisplayName("webtoon callback - bgm 대기")
  @WithMockTodakUser
  void webtoon_callback_wait() throws Exception {

    // given
    diary = BaseTestObject.createDiary_NoId_WithWebtoonUrl_ByMember(member);
    diaryRepository.save(diary);

    AiCallbackWebtoonRequest request =
        new AiCallbackWebtoonRequest(
            member.getId(), diary.getDiaryCreatedTime(), diary.getWebtoonImageUrl());

    // when & then
    mockMvc
        .perform(
            post((CALLBACK_WEBTOON_URL))
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(print());

    String sseResult = response.getContentAsString();
    assertThat(sseResult).doesNotContain("diary");
  }

  @Test
  @DisplayName("webtoon callback 완료 - 사용자 알림(bgm 완료)")
  @WithMockTodakUser
  void webtoon_callback_notification() throws Exception {

    // given
    diary = BaseTestObject.createDiary_NoId_WithWebtoonUrlAndBgmUrl_ByMember(member);
    diaryRepository.save(diary);

    AiCallbackWebtoonRequest request =
        new AiCallbackWebtoonRequest(
            member.getId(), diary.getDiaryCreatedTime(), diary.getWebtoonImageUrl());

    mockMvc
        .perform(
            post((CALLBACK_WEBTOON_URL))
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(print());

    String sseResult = response.getContentAsString();
    assertThat(sseResult).contains("diary");
  }

  @Test
  @DisplayName("bgm callback - webtoon 대기")
  @WithMockTodakUser
  void bgm_callback_wait() throws Exception {

    // given
    diary = BaseTestObject.createDiary_NoId_WithBgmUrl_ByMember(member);
    diaryRepository.save(diary);

    AiCallbackBgmRequest request =
        new AiCallbackBgmRequest(member.getId(), diary.getDiaryCreatedTime(), diary.getBgmUrl());

    // when & then
    mockMvc
        .perform(
            post((CALLBACK_BGM_URL))
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(print());

    String sseResult = response.getContentAsString();
    assertThat(sseResult).doesNotContain("diary");
  }

  @Test
  @DisplayName("bgm callback 완료 - 사용자 알림(webtoon 완료)")
  @WithMockTodakUser
  void bgm_callback_notification() throws Exception {

    // given
    diary = BaseTestObject.createDiary_NoId_WithWebtoonUrlAndBgmUrl_ByMember(member);
    diaryRepository.save(diary);

    AiCallbackBgmRequest request =
        new AiCallbackBgmRequest(member.getId(), diary.getDiaryCreatedTime(), diary.getBgmUrl());

    mockMvc
        .perform(
            post((CALLBACK_BGM_URL))
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(print());

    String sseResult = response.getContentAsString();
    assertThat(sseResult).contains("diary");
  }

  @Test
  @DisplayName("character callback 완료")
  @WithMockTodakUser
  void character_callback_notification() throws Exception {

    AiCallbackCharacterRequest request =
        new AiCallbackCharacterRequest(
            member.getId(),
            member.getCharacterInfo(),
            member.getCharacterStyle(),
            member.getCharacterSeed(),
            BaseTestObject.TEST_CHARACTER_URL);

    mockMvc
        .perform(
            post(CALLBACK_CHARACTER_URL)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andReturn();

    String sseResult = response.getContentAsString();
    assertThat(sseResult).contains("character");
  }

  @Test
  @DisplayName("character callback 실패 - 찾을 수 없는 회원")
  @WithMockTodakUser
  void character_callback_notification_not_found_member() throws Exception {

    AiCallbackCharacterRequest request =
        new AiCallbackCharacterRequest(
            9999L,
            member.getCharacterInfo(),
            member.getCharacterStyle(),
            member.getCharacterSeed(),
            BaseTestObject.TEST_CHARACTER_URL);

    mockMvc
        .perform(
            post(CALLBACK_CHARACTER_URL)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertThat(result.getResolvedException())
                    .isInstanceOf(MemberNotFoundException.class))
        .andReturn();

    String sseResult = response.getContentAsString();
    assertThat(sseResult).doesNotContain("character");
  }
}
