package com.heartsave.todaktodak_api.member.controller;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.member.dto.response.CharacterTemporaryImageResponse;
import com.heartsave.todaktodak_api.member.service.CharacterService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 캐릭터", description = "회원 캐릭터 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member/character")
public class CharacterController {
  private final CharacterService characterService;

  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "기존 캐릭터 이미지 경로 응답"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패")
      })
  @GetMapping
  public ResponseEntity<CharacterTemporaryImageResponse> getPastCharacterImage(
      @AuthenticationPrincipal TodakUser principal) {
    return ResponseEntity.ok(characterService.getPastCharacterImage(principal));
  }
}
