package com.heartsave.todaktodak_api.domain.ai.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AiComment(@NotBlank @JsonProperty("content") String comment) {}
