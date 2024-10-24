package com.heartsave.todaktodak_api.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(String username, String accessToken) {}
