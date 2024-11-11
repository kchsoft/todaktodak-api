package com.heartsave.todaktodak_api.ai.client.domain;

import jakarta.validation.constraints.NotBlank;

public record AiComment(@NotBlank String comment) {}
