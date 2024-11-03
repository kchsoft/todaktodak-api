package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.dto.MySharedDiaryPreview;
import java.util.List;

public record MySharedDiaryPaginationResponse(
    List<MySharedDiaryPreview> sharedDiaries, Long after) {}
