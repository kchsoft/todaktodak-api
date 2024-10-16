package com.heartsave.todaktodak_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginIdCheckRequest(
    @NotBlank(message = "Blank data") @Size(max = 30, message = "Exceed size") String loginId) {}
