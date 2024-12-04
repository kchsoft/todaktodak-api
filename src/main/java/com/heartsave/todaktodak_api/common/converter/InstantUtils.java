package com.heartsave.todaktodak_api.common.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class InstantUtils {
  public static LocalDate toLocalDate(Instant instant) {
    return instant.atZone(ZoneId.of("UTC")).toLocalDate();
  }

  public static LocalDateTime toLocalDateTime(Instant instant) {
    return instant.atZone(ZoneId.of("UTC")).toLocalDateTime();
  }

  public static Instant toMonthStartAtZone(Instant yearMonth, String zoneId) {
    return yearMonth
        .atZone(ZoneId.of(zoneId))
        .withDayOfMonth(1)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant();
  }

  public static Instant toMonthEndAtZone(Instant yearMonth, String zoneId) {
    return yearMonth
        .atZone(ZoneId.of(zoneId))
        .withDayOfMonth(1)
        .plusMonths(1)
        .minusNanos(1)
        .toInstant();
  }

  public static Instant toDayStartAtZone(Instant instant, String zoneId) {
    return instant
        .atZone(ZoneId.of(zoneId))
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant();
  }

  public static Instant toDayEndAtZone(Instant instant, String zoneId) {
    return instant
        .atZone(ZoneId.of(zoneId))
        .truncatedTo(ChronoUnit.DAYS)
        .plusDays(1)
        .minusNanos(1)
        .toInstant();
  }
}
