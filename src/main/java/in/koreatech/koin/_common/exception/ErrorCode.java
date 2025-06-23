package in.koreatech.koin._common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode {

    // ==================================================
    // User domain
    // ==================================================

    // 400 Bad Request
    USER_GENDER_NOT_VALID(HttpStatus.BAD_REQUEST, "USER-400001", "잘못된 성별 인덱스입니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404001", "해당 사용자를 찾을 수 없습니다."),

    // 409 Conflict
    USER_DUPLICATION_LOGIN_ID(HttpStatus.CONFLICT, "USER-409001", "이미 사용 중인 로그인 아이디입니다."),
    USER_DUPLICATION_NICKNAME(HttpStatus.CONFLICT, "USER-409002", "이미 사용 중인 닉네임입니다."),
    USER_DUPLICATION_PHONE_NUMBER(HttpStatus.CONFLICT, "USER-409003", "이미 사용 중인 전화번호입니다."),
    USER_DUPLICATION_EMAIL(HttpStatus.CONFLICT, "USER-409004", "이미 사용 중인 전화번호입니다."),

    // ==================================================
    // Verification domain
    // ==================================================

    // 400 Bad Request
    VERIFICATION_CODE_NOT_VALID(HttpStatus.BAD_REQUEST, "VERIFICATION-400001", "인증 번호가 일치하지 않습니다."),

    // 403 Forbidden
    VERIFICATION_FORBIDDEN_API(HttpStatus.FORBIDDEN, "VERIFICATION-403001", "인증 후 다시 시도해주십시오."),

    // 404 Not Found
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "VERIFICATION-404001", "인증 번호 전송 후 다시 시도해주십시오."),

    // 429 Too Many Requests
    VERIFICATION_TOO_MANY_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "VERIFICATION-429001", "하루 인증 횟수를 초과했습니다."),

    // TODO: 나머지 도메인 에러코드 구현

    ;

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String errorCode;

    @Getter
    private final String message;
}
