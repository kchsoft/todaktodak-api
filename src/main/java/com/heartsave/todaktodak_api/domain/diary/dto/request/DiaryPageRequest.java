package com.heartsave.todaktodak_api.domain.diary.dto.request;

import java.time.Instant;

public record DiaryPageRequest(Long publicDiaryId, Instant createdTime) {}
