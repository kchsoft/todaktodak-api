package com.heartsave.todaktodak_api.diary.dto;

import java.time.LocalDate;

public record MySharedDiaryPreview(
    Long publicDiaryId, String webtoonImageUrl, LocalDate createdDate) {}
