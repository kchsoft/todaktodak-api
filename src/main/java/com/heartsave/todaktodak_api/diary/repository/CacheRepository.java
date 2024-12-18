package com.heartsave.todaktodak_api.diary.repository;

public interface CacheRepository<T> {
  String serialize(T value);

  T deserialize(String value);
}
