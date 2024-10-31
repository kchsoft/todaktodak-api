package com.heartsave.todaktodak_api.auth.dto.response;

import lombok.Builder;

@Builder
public record TokenReissueResponse(String accessToken) {}
