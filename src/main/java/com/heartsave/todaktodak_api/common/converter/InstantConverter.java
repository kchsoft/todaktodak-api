package com.heartsave.todaktodak_api.common.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class InstantConverter {
  public static LocalDate toLocalDate(Instant instant) {
    return instant.atZone(ZoneId.of("UTC")).toLocalDate();
  }

  public static LocalDateTime toLocalDateTime(Instant instant) {
    return instant.atZone(ZoneId.of("UTC")).toLocalDateTime();
  }

  public static Instant toMonthStartDateTime(Instant yearMonth) {
    return yearMonth
        .atZone(ZoneOffset.UTC)
        .withDayOfMonth(1)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant();
  }

  public static Instant toMonthEndDateTime(Instant yearMonth) {
    return yearMonth
        .atZone(ZoneOffset.UTC)
        .withDayOfMonth(1)
        .plusMonths(1)
        .minusNanos(1)
        .toInstant();
  }
}
