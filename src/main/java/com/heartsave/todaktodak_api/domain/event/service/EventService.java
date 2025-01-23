package com.heartsave.todaktodak_api.domain.event.service;

import com.heartsave.todaktodak_api.domain.event.entity.EventEntity;

public interface EventService {
  void save(EventEntity event);

  void send(EventEntity event);
}
