package in.koreatech.koin._common.exception.errorcode;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    PHONE_NUMBER_NOT_MATCHED(BAD_REQUEST, "전화번호가 일치하지 않습니다."),
    EMAIL_NOT_MATCHED(BAD_REQUEST, "이메일이 일치하지 않습니다."),
    PASSWORD_NOT_MATCHED(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    GENDER_NOT_VALID(BAD_REQUEST, "잘못된 성별 인덱스입니다."),
    REFRESH_TOKEN_NOT_FOUND(BAD_REQUEST, "refresh token이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_MATCHED(BAD_REQUEST, "refresh token이 일치하지 않습니다."),
    RESET_TOKEN_NOT_VALID(BAD_REQUEST, "Reset token이 존재하지 않습니다."),

    // 403 Forbidden
    WITHDRAWN_USER(FORBIDDEN, "탈퇴한 계정입니다."),
    USER_TYPE_FORBIDDEN(FORBIDDEN, "인가되지 않은 유저 타입입니다."),
    OWNER_FORBIDDEN(FORBIDDEN, "관리자 인증 대기중입니다."),
    STUDENT_FORBIDDEN(FORBIDDEN, "아우누리에서 인증메일을 확인해주세요."),
    ADMIN_FORBIDDEN(FORBIDDEN, "PL 인증 대기중입니다."),
    ACCOUNT_FORBIDDEN(FORBIDDEN, "유효하지 않은 계정입니다."),
    REFRESH_TOKEN_NOT_VALID(FORBIDDEN, "올바르지 않은 인증 토큰입니다."),

    // 404 Not Found
    USER_NOT_FOUND(NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),

    // 409 Conflict
    LOGIN_ID_CONFLICT(CONFLICT, "이미 존재하는 로그인 아이디입니다."),
    NICKNAME_CONFLICT(CONFLICT, "이미 존재하는 닉네임입니다."),
    PHONE_NUMBER_CONFLICT(CONFLICT, "이미 존재하는 전화번호입니다."),
    EMAIL_CONFLICT(CONFLICT, "이미 존재하는 이메일입니다."),

    // 400 Bad Request
    VERIFICATION_CODE_NOT_MATCHED(BAD_REQUEST, "인증 번호가 일치하지 않습니다."),
    // 403 Forbidden
    VERIFICATION_NOT_VALID(FORBIDDEN, "인증 후 다시 시도해주십시오."),
    // 404 Not Found
    VERIFICATION_NOT_FOUND(NOT_FOUND, "인증 번호 전송 후 다시 시도해주십시오."),
    // 429 Too Many Requests
    VERIFICATION_TOO_MANY_REQUESTS(TOO_MANY_REQUESTS, "하루 인증 횟수를 초과했습니다."),
    ;

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String message;
}
