package com.heartsave.todaktodak_api.domain.ai.client.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.ai.AiErrorSpec;
import com.heartsave.todaktodak_api.domain.ai.client.config.properties.AiServerProperties;
import com.heartsave.todaktodak_api.domain.ai.client.domain.AiComment;
import com.heartsave.todaktodak_api.domain.ai.client.dto.request.AiClientBgmRequest;
import com.heartsave.todaktodak_api.domain.ai.client.dto.request.AiClientCharacterRequest;
import com.heartsave.todaktodak_api.domain.ai.client.dto.request.AiClientCommentRequest;
import com.heartsave.todaktodak_api.domain.ai.client.dto.request.AiClientWebtoonRequest;
import com.heartsave.todaktodak_api.domain.ai.client.dto.response.AiClientCommentResponse;
import com.heartsave.todaktodak_api.domain.ai.exception.AiException;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
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

  public AiClientCommentResponse callDiaryContent(DiaryEntity diary) {
    MemberEntity member = diary.getMemberEntity();
    callWebtoon(AiClientWebtoonRequest.of(diary, member));
    callBgm(AiClientBgmRequest.of(diary, member));
    AiComment aiComment = callComment(AiClientCommentRequest.of((diary)));
    return AiClientCommentResponse.builder().aiComment(aiComment.comment()).build();
  }

  private void callWebtoon(AiClientWebtoonRequest request) {
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

  private void callBgm(AiClientBgmRequest request) {
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

  private AiComment callComment(AiClientCommentRequest request) {
    return webClient
        .post()
        .uri(aiServerProperties.textDomain() + "/comment")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(AiComment.class)
        .doOnSuccess(result -> log.info("AI 코멘트 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("AI 코멘트 생성 요청에 오류가 발생했습니다."))
        .block();
  }

  public void callCharacter(MultipartFile image, AiClientCharacterRequest request) {
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
      MultipartFile image, AiClientCharacterRequest request) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    // 요청 json
    builder.part("memberId", request.getMemberId());
    builder.part("characterStyle", request.getCharacterStyle());
    builder.part("apiDomainUrl", request.getSeverDomainUrl());

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
