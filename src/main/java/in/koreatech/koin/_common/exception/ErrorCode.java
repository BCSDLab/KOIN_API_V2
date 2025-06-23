package in.koreatech.koin._common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    USER_GENDER_NOT_VALID(HttpStatus.BAD_REQUEST, "USER-400001", "잘못된 성별 인덱스입니다."),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404001", "해당 사용자를 찾을 수 없습니다."),

    // 409
    USER_DUPLICATION_LOGIN_ID(HttpStatus.CONFLICT, "USER-409001", "이미 사용 중인 로그인 아이디입니다."),
    USER_DUPLICATION_NICKNAME(HttpStatus.CONFLICT, "USER-409002", "이미 사용 중인 닉네임입니다."),
    USER_DUPLICATION_PHONE_NUMBER(HttpStatus.CONFLICT, "USER-409003", "이미 사용 중인 전화번호입니다."),
    USER_DUPLICATION_EMAIL(HttpStatus.CONFLICT, "USER-409004", "이미 사용 중인 전화번호입니다."),
    ;

    private final HttpStatus httpStatusCode;

    @Getter
    private final String errorCode;

    @Getter
    private final String message;

    public Integer getHttpIntegerCode() {
        return httpStatusCode.value();
    }
}
