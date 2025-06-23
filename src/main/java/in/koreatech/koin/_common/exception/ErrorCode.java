package in.koreatech.koin._common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode {

    // ==================================================
    // User domain
    // ==================================================

    // 400 Bad Request
    USER_PHONE_NUMBER_NOT_MATCHED(BAD_REQUEST, "USER-400001", "전화번호가 일치하지 않습니다."),
    USER_EMAIL_NOT_MATCHED(BAD_REQUEST, "USER-400002", "이메일이 일치하지 않습니다."),
    USER_PASSWORD_NOT_MATCHED(BAD_REQUEST, "USER-400003", "비밀번호가 일치하지 않습니다."),
    USER_GENDER_NOT_VALID(BAD_REQUEST, "USER-400004", "잘못된 성별 인덱스입니다."),
    USER_REFRESH_TOKEN_NOT_VALID(BAD_REQUEST, "USER-400005", "refresh token이 존재하지 않습니다."),
    USER_REFRESH_TOKEN_NOT_MATCHED(BAD_REQUEST, "USER-400006", "refresh token이 일치하지 않습니다."),
    USER_RESET_TOKEN_NOT_VALID(BAD_REQUEST, "USER-400007", "Reset token이 존재하지 않습니다."),

    // 403 Forbidden
    USER_WITHDRAWN_USER(FORBIDDEN, "USER-403001", "탈퇴한 계정입니다."),
    USER_FORBIDDEN_USER_TYPE(FORBIDDEN, "USER-403002", "인가되지 않은 유저 타입입니다."),
    USER_FORBIDDEN_OWNER(FORBIDDEN, "USER-403003", "관리자 인증 대기중입니다."),
    USER_FORBIDDEN_STUDENT(FORBIDDEN, "USER-403004", "아우누리에서 인증메일을 확인해주세요."),
    USER_FORBIDDEN_ADMIN(FORBIDDEN, "USER-403005", "PL 인증 대기중입니다."),
    USER_FORBIDDEN_DEFAULT(FORBIDDEN, "USER-403006", "유효하지 않은 계정입니다."),
    USER_FORBIDDEN_REFRESH_TOKEN(FORBIDDEN, "USER-403007", "올바르지 않은 인증 토큰입니다."),

    // 404 Not Found
    USER_NOT_FOUND(NOT_FOUND, "USER-404001", "해당 사용자를 찾을 수 없습니다."),

    // 409 Conflict
    USER_DUPLICATION_LOGIN_ID(CONFLICT, "USER-409001", "이미 존재하는 로그인 아이디입니다."),
    USER_DUPLICATION_NICKNAME(CONFLICT, "USER-409002", "이미 존재하는 닉네임입니다."),
    USER_DUPLICATION_PHONE_NUMBER(CONFLICT, "USER-409003", "이미 존재하는 전화번호입니다."),
    USER_DUPLICATION_EMAIL(CONFLICT, "USER-409004", "이미 존재하는 이메일입니다."),

    // ==================================================
    // Verification domain
    // ==================================================

    // 400 Bad Request
    VERIFICATION_CODE_NOT_VALID(BAD_REQUEST, "VERIFICATION-400001", "인증 번호가 일치하지 않습니다."),

    // 403 Forbidden
    VERIFICATION_FORBIDDEN_API(FORBIDDEN, "VERIFICATION-403001", "인증 후 다시 시도해주십시오."),

    // 404 Not Found
    VERIFICATION_NOT_FOUND(NOT_FOUND, "VERIFICATION-404001", "인증 번호 전송 후 다시 시도해주십시오."),

    // 429 Too Many Requests
    VERIFICATION_TOO_MANY_REQUEST(TOO_MANY_REQUESTS, "VERIFICATION-429001", "하루 인증 횟수를 초과했습니다."),

    // TODO: 나머지 도메인 에러코드 구현

    ;

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String errorCode;

    @Getter
    private final String message;
}
