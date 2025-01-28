package com.heartsave.todaktodak_api.domain.member.controller;

import com.heartsave.todaktodak_api.domain.auth.annotation.TodakUserId;
import com.heartsave.todaktodak_api.domain.member.dto.response.CharacterImageResponse;
import com.heartsave.todaktodak_api.domain.member.dto.response.CharacterRegisterResponse;
import com.heartsave.todaktodak_api.domain.member.service.CharacterService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
  public ResponseEntity<CharacterImageResponse> getCharacterImage(@TodakUserId Long memberId) {
    return ResponseEntity.ok(characterService.getCharacterImage(memberId));
  }

  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "기존 캐릭터 이미지 경로 응답"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패")
      })
  @PostMapping
  public ResponseEntity<Void> createCharacterImage(
      @RequestParam("uploadImage") MultipartFile file,
      @RequestParam("characterStyle") String characterStyle,
      @TodakUserId Long memberId) {
    characterService.createCharacterImage(file, characterStyle, memberId);
    return ResponseEntity.noContent().build();
  }

  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "캐릭터 등록 완료 및 회원 권한 승격"),
        @ApiResponse(responseCode = "404", description = "회원 조회 실패")
      })
  @PostMapping("/register")
  public ResponseEntity<CharacterRegisterResponse> completeCharacterRegister(
      @TodakUserId Long memberId, HttpServletResponse response) {
    return ResponseEntity.ok(characterService.registerCharacterAndChangeRole(memberId, response));
  }
}
