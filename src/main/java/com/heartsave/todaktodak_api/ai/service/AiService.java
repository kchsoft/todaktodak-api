package com.heartsave.todaktodak_api.ai.service;

import com.heartsave.todaktodak_api.ai.dto.request.AiCharacterRequest;
import com.heartsave.todaktodak_api.ai.dto.request.AiContentRequest;
import com.heartsave.todaktodak_api.ai.dto.response.AiContentResponse;
import com.heartsave.todaktodak_api.ai.exception.AiException;
import com.heartsave.todaktodak_api.common.exception.errorspec.AiErrorSpec;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiService {

  private final WebClient webClient;

  public AiContentResponse callAiContent(DiaryEntity diary) {
    //    AiContentRequest request = getAiContentRequest(diary);
    //    callWebtoon(request);
    //    callBgm(request);
    //    String comment = callComment(request);
    return AiContentResponse.builder().aiComment("aiComment").build();
  }

  private AiContentRequest getAiContentRequest(DiaryEntity diary) {
    return AiContentRequest.builder()
        .id(diary.getMemberEntity().getId())
        .content(diary.getContent())
        .emotion(diary.getEmotion())
        .build();
  }

  private void callWebtoon(AiContentRequest request) {
    webClient
        .post()
        .uri("/webtoon")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("Webtoon 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("Webtoon 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private void callBgm(AiContentRequest request) {
    webClient
        .post()
        .uri("/bgm")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("BGM 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("BGM 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private String callComment(AiContentRequest request) {
    return webClient
        .post()
        .uri("/comment")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class)
        .doOnSuccess(result -> log.info("AI 코멘트 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("AI 코멘트 생성 요청에 오류가 발생했습니다."))
        .block();
  }

  public void callCharacter(MultipartFile image, AiCharacterRequest request) {
    webClient
        .post()
        .uri("/character")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(createMultipartBody(image, request)))
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(result -> log.info("캐릭터 생성 요청을 성공적으로 보냈습니다."))
        .doOnError(error -> log.error("캐릭터 생성 요청에 오류가 발생했습니다.", error))
        .subscribe();
  }

  private MultiValueMap<String, HttpEntity<?>> createMultipartBody(
      MultipartFile image, AiCharacterRequest request) {
    MultiValueMap<String, HttpEntity<?>> multipartBody = new LinkedMultiValueMap<>();

    // 요청 json
    multipartBody.add("memberId", new HttpEntity<>(request.memberId()));
    multipartBody.add("characterStyle", new HttpEntity<>(request.characterStyle()));

    // 이미지
    try {
      ByteArrayResource imageResource =
          new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
              return image.getOriginalFilename();
            }
          };
      multipartBody.add("userImage", new HttpEntity<>(imageResource));
    } catch (Exception e) {
      throw new AiException(AiErrorSpec.IMAGE_PROCESS_FAIL, image.getOriginalFilename());
    }
    return multipartBody;
  }
}
