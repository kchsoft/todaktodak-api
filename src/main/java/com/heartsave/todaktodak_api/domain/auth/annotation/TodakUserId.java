package com.heartsave.todaktodak_api.domain.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(
    expression = "#this == 'anonymousUser' ? null : id",
    errorOnInvalidType = true)
public @interface TodakUserId {}
