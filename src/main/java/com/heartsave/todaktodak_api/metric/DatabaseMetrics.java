package com.heartsave.todaktodak_api.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMetrics {
  private final MeterRegistry registry;
  private final Timer postgresTimer;
  private final Timer redisTimer;

  public DatabaseMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.postgresTimer =
        Timer.builder("db.postgres.query.time")
            .description("PostgreSQL query execution time")
            .register(registry);
    this.redisTimer =
        Timer.builder("db.redis.query.time")
            .description("Redis query execution time")
            .register(registry);
  }

  public Timer getPostgresTimer() {
    return postgresTimer;
  }

  public Timer getRedisTimer() {
    return redisTimer;
  }
}
