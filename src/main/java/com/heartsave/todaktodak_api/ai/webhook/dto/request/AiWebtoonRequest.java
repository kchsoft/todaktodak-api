package com.heartsave.todaktodak_api.ai.webhook.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AiWebtoonRequest(
    @Min(1L) Long memberId,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDate,
    @NotBlank String webtoonFolderUrl) {}
