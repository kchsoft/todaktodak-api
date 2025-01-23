package com.heartsave.todaktodak_api.domain.event.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
  DIARY("diary"),
  CHARACTER("character");

  private final String type;
}
