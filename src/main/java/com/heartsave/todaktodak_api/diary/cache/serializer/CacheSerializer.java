package com.heartsave.todaktodak_api.diary.cache.serializer;


public interface CacheSerializer<T> {

  String serialize(T value);

  T deserialize(String value);
}
