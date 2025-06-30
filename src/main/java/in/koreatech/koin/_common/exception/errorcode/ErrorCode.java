package in.koreatech.koin._common.exception.errorcode;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    NOT_MATCHED_PHONE_NUMBER(BAD_REQUEST, "전화번호가 일치하지 않습니다."),
    NOT_MATCHED_EMAIL(BAD_REQUEST, "이메일이 일치하지 않습니다."),
    NOT_MATCHED_PASSWORD(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    NOT_VALID_GENDER_INDEX(BAD_REQUEST, "잘못된 성별 인덱스입니다."),
    NOT_FOUND_REFRESH_TOKEN(BAD_REQUEST, "refresh token이 존재하지 않습니다."),
    NOT_MATCHED_REFRESH_TOKEN(BAD_REQUEST, "refresh token이 일치하지 않습니다."),
    NOT_VALID_RESET_TOKEN(BAD_REQUEST, "Reset token이 존재하지 않습니다."),
    NOT_MATCHED_VERIFICATION_CODE(BAD_REQUEST, "인증 번호가 일치하지 않습니다."),
    INVALID_RECRUITMENT_PERIOD(BAD_REQUEST, "모집 마감일은 모집 시작일 이후여야 합니다."),
    MUST_BE_NULL_RECRUITMENT_PERIOD(BAD_REQUEST, "상시 모집일 경우, 모집 시작일과 종료일은 입력하면 안 됩니다."),
    REQUIRED_RECRUITMENT_PERIOD(BAD_GATEWAY, "상시 모집이 아닌 경우, 모집 시작일과 종료일은 필수입니다."),

    // 403 Forbidden
    WITHDRAWN_USER(FORBIDDEN, "탈퇴한 계정입니다."),
    FORBIDDEN_USER_TYPE(FORBIDDEN, "인가되지 않은 유저 타입입니다."),
    FORBIDDEN_OWNER(FORBIDDEN, "관리자 인증 대기중입니다."),
    FORBIDDEN_STUDENT(FORBIDDEN, "아우누리에서 인증메일을 확인해주세요."),
    FORBIDDEN_ADMIN(FORBIDDEN, "PL 인증 대기중입니다."),
    FORBIDDEN_ACCOUNT(FORBIDDEN, "유효하지 않은 계정입니다."),
    NOT_VALID_REFRESH_TOKEN(FORBIDDEN, "올바르지 않은 인증 토큰입니다."),
    FORBIDDEN_API(FORBIDDEN, "인증 후 다시 시도해주십시오."),

    // 404 Not Found
    NOT_FOUND_USER(NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    NOT_FOUND_CLUB(NOT_FOUND, "동아리가 존재하지 않습니다."),

    // 409 Conflict
    CONFLICT_LOGIN_ID(CONFLICT, "이미 존재하는 로그인 아이디입니다."),
    CONFLICT_NICKNAME(CONFLICT, "이미 존재하는 닉네임입니다."),
    CONFLICT_PHONE_NUMBER(CONFLICT, "이미 존재하는 전화번호입니다."),
    CONFLICT_EMAIL(CONFLICT, "이미 존재하는 이메일입니다."),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS_VERIFICATION(TOO_MANY_REQUESTS, "하루 인증 횟수를 초과했습니다."),
    ;

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String message;
}
