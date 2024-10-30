package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.dto.PublicDiaryViewDetail;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public record PublicDiaryViewDetailResponse(List<PublicDiaryViewDetail> diaries, Long after) {}
