package com.heartsave.todaktodak_api.ai.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AiComment(@NotBlank @JsonProperty("content") String comment) {}
