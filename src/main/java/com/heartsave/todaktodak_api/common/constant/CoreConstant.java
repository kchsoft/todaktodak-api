package com.heartsave.todaktodak_api.common.constant;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreConstant {
  public static class URL {
    public static final String DEFAULT_URL = "";
  }

  public static class TIME_FORMAT {
    public static final String ISO_DATETIME_WITH_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  }

  public static class DIARY {
    public static final Long PAGE_DEFAULT_ID = 0L;
    public static final Instant PAGE_DEFAULT_TIME = Instant.EPOCH;
  }
}
