package com.heartsave.todaktodak_api.ai.client.service;

import com.heartsave.todaktodak_api.ai.client.config.properties.AiServerProperties;
import com.heartsave.todaktodak_api.ai.client.domain.AiComment;
import com.heartsave.todaktodak_api.ai.client.dto.request.ClientAiCommentRequest;
import com.heartsave.todaktodak_api.ai.client.dto.request.ClientBgmRequest;
import com.heartsave.todaktodak_api.ai.client.dto.request.ClientCharacterRequest;
import com.heartsave.todaktodak_api.ai.client.dto.request.ClientWebtoonRequest;
import com.heartsave.todaktodak_api.ai.client.dto.response.AiDiaryContentResponse;
import com.heartsave.todaktodak_api.ai.exception.AiException;
import com.heartsave.todaktodak_api.common.exception.errorspec.AiErrorSpec;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiClientService {
  private final AiServerProperties aiServerProperties;
  private final WebClient webClient;

  public AiDiaryContentResponse callDiaryContent(DiaryEntity diary) {
    MemberEntity member = diary.getMemberEntity();
    callWebtoon(ClientWebtoonRequest.of(diary, member));
    callBgm(ClientBgmRequest.of(diary, member));
    AiComment aiComment = callComment(ClientAiCommentRequest.of((diary)));
    return AiDiaryContentResponse.builder().aiComment(aiComment.comment()).build();
  }

  private void callWebtoon(ClientWebtoonRequest request) {
    webClient
        .post()
        .uri(aiServerProperties.imageDomain() + "/webtoon")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("Webtoon 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("Webtoon 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private void callBgm(ClientBgmRequest request) {
    webClient
        .post()
        .uri(aiServerProperties.bgmDomain() + "/music-ai")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("BGM 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("BGM 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private AiComment callComment(ClientAiCommentRequest request) {
    return webClient
        .post()
        .uri(aiServerProperties.textDomain() + "/comment")
        .bodyValue(request)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(AiComment.class)
        .doOnSuccess(result -> log.info("AI 코멘트 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("AI 코멘트 생성 요청에 오류가 발생했습니다."))
        .block();
  }

  public void callCharacter(MultipartFile image, ClientCharacterRequest request) {
    webClient
        .post()
        .uri(aiServerProperties.imageDomain() + "/character")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(createMultipartBody(image, request)))
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("캐릭터 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("캐릭터 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private MultiValueMap<String, HttpEntity<?>> createMultipartBody(
      MultipartFile image, ClientCharacterRequest request) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    // 요청 json
    builder.part("memberId", request.memberId());
    builder.part("characterStyle", request.characterStyle());

    // 이미지
    try {
      ByteArrayResource imageResource =
          new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
              return image.getOriginalFilename();
            }
          };

      builder
          .part("userImage", imageResource)
          .filename(Objects.requireNonNull(image.getOriginalFilename()))
          .contentType(MediaType.parseMediaType(Objects.requireNonNull(image.getContentType())));

    } catch (IOException e) {
      throw new AiException(AiErrorSpec.IMAGE_PROCESS_FAIL, image.getOriginalFilename());
    }

    return builder.build();
  }
}
