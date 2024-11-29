package com.heartsave.todaktodak_api.diary.dto.request;


import java.time.Instant;

public record PublicDiaryPageRequest(Long publicDiaryId, Instant createdTime) {}
