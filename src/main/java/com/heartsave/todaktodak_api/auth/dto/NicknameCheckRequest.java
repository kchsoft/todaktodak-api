package com.heartsave.todaktodak_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameCheckRequest(
    @NotBlank(message = "Blank data") @Size(max = 50, message = "Exceed size") String nickname) {}
