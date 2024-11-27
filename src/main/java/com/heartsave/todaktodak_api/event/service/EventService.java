package com.heartsave.todaktodak_api.event.service;

import com.heartsave.todaktodak_api.event.entity.EventEntity;

public interface EventService {
  void save(EventEntity event);

  void send(EventEntity event);
}
