package com.heartsave.todaktodak_api.diary.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import java.time.Duration;
import java.time.Instant;

@Documented
@Constraint(validatedBy = TimeToleranceValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeWithinTolerance {
  String message() default "Time must be within allowed tolerance range";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

class TimeToleranceValidator implements ConstraintValidator<TimeWithinTolerance, Instant> {
  // 상수로 정의하여 고정된 값 사용
  private static final Duration FUTURE_TOLERANCE = Duration.ofSeconds(60);

  @Override
  public void initialize(TimeWithinTolerance annotation) {
    // 초기화가 필요 없어졌지만, 인터페이스 구현을 위해 메서드는 유지
  }

  @Override
  public boolean isValid(Instant value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // null 검증은 @NotNull이 처리
    }

    Instant now = Instant.now();
    Instant latestAllowed = now.plus(FUTURE_TOLERANCE);

    // 미래로 1분까지만 허용하고, 과거는 제한 없이 허용
    return !value.isAfter(latestAllowed);
  }
}
