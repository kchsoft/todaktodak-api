package com.heartsave.todaktodak_api.common.exception;

import java.util.HashMap;
import java.util.Map;

public class ErrorField {
  private final Map<String, Object> fields;

  protected ErrorField() {
    this.fields = new HashMap<>();
  }

  public ErrorField add(String key, Object value) {
    fields.put(key, value);
    return this;
  }

  public Map<String, Object> get() {
    return fields;
  }

  public Object get(String key) {
    return fields.get(key);
  }
}
