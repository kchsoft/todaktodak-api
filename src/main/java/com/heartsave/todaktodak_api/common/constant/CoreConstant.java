package com.heartsave.todaktodak_api.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreConstant {
  public static class URL {
    public static final String DEFAULT_URL = "";
    public static final String TEMP_CHARACTER_IMAGE_URL_PREFIX = "temp_";
  }

  public static class TIME_FORMAT {
    public static final String ISO_DATETIME_WITH_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  }

  public static class HEADER {
    public static final String TIME_ZONE_KEY = "Todak-Time-Zone";
    public static final String DEFAULT_TIME_ZONE = "UTC";
  }
}
