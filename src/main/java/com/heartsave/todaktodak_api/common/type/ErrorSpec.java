package com.heartsave.todaktodak_api.common.type;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorSpec {
    DIARY_DAILY_WRITING_LIMIT_EXCEPTION(HttpStatus.BAD_REQUEST, "하루 일기 작성량을 초과하였습니다.");

    private HttpStatus status;
    private String message;

    ErrorSpec(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
